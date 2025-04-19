//
// Copyright 2025 Ian Stewart
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
package io.ikstewa.grpc.protoc.javarecords;

import com.google.common.base.Strings;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DescriptorProtos.DescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto;
import com.google.protobuf.DescriptorProtos.FieldDescriptorProto.Type;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import com.salesforce.jprotoc.ProtoTypeMap;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.lang.model.element.Modifier;

/** Generator used to convert a Proto */
class RecordGenerator {

  private static final String OUTER_CLASS_SUFFIX = "Records";

  private final DescriptorProtos.FileDescriptorProto protoFile;
  private final String javaPackageName;
  private final boolean multipleClassFiles;

  public RecordGenerator(DescriptorProtos.FileDescriptorProto proto) {
    this.protoFile = proto;
    this.javaPackageName = extractPackageName(proto);
    this.multipleClassFiles =
        false; // TODO: https://github.com/ikstewa/protoc-gen-java-records/issues/15
  }

  public Collection<JavaFile> buildRecords() {
    final var recordTypes =
        this.protoFile.getMessageTypeList().stream().map(RecordGenerator::buildRecord).toList();

    if (this.multipleClassFiles) {
      throw new UnsupportedOperationException(
          "TODO: https://github.com/ikstewa/protoc-gen-java-records/issues/15");
    } else {
      // Generate a new outer class file which contains all the records for this proto
      final var outerClassname =
          ProtoTypeMap.getJavaOuterClassname(this.protoFile) + OUTER_CLASS_SUFFIX;

      final var outerType =
          TypeSpec.classBuilder(outerClassname)
              .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
              .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build())
              .addTypes(recordTypes)
              .build();

      return List.of(
          JavaFile.builder(this.javaPackageName, outerType).skipJavaLangImports(true).build());
    }
  }

  private static TypeSpec buildRecord(DescriptorProto message) {
    return TypeSpec.recordBuilder(message.getName())
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(org.jspecify.annotations.NullMarked.class)
        .recordConstructor(
            MethodSpec.compactConstructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameters(
                    message.getFieldList().stream().map(RecordGenerator::toParameterSpec).toList())
                .addCode(
                    message.getFieldList().stream()
                        .map(RecordGenerator::recordConstructorCode)
                        .filter(cb -> !cb.isEmpty())
                        .collect(CodeBlock.joining("")))
                .build())
        .build();
  }

  private static ParameterSpec toParameterSpec(FieldDescriptorProto field) {
    final var isOptional = field.hasProto3Optional() && field.getProto3Optional();
    final TypeName type =
        switch (field.getType()) {
          case TYPE_STRING -> ClassName.get(String.class);
          case TYPE_BOOL -> isOptional ? ClassName.get(Boolean.class) : TypeName.BOOLEAN;
          case TYPE_BYTES ->
              throw new UnsupportedOperationException("Unimplemented case: " + field.getType());
          case TYPE_DOUBLE -> isOptional ? ClassName.get(Double.class) : TypeName.DOUBLE;
          case TYPE_ENUM ->
              throw new UnsupportedOperationException("Unimplemented case: " + field.getType());
          case TYPE_FIXED32, TYPE_INT32, TYPE_SFIXED32, TYPE_SINT32, TYPE_UINT32 ->
              isOptional ? ClassName.get(Integer.class) : TypeName.INT;
          case TYPE_FIXED64, TYPE_INT64, TYPE_SFIXED64, TYPE_SINT64, TYPE_UINT64 ->
              isOptional ? ClassName.get(Long.class) : TypeName.LONG;
          case TYPE_FLOAT -> isOptional ? ClassName.get(Float.class) : TypeName.FLOAT;
          case TYPE_GROUP ->
              throw new UnsupportedOperationException("Unimplemented case: " + field.getType());
          case TYPE_MESSAGE ->
              throw new UnsupportedOperationException("Unimplemented case: " + field.getType());
        };
    final var paramBuilder = ParameterSpec.builder(type, field.getName());
    if (isOptional) {
      paramBuilder.addAnnotation(org.jspecify.annotations.Nullable.class);
    }
    return paramBuilder.build();
  }

  private static CodeBlock recordConstructorCode(FieldDescriptorProto field) {
    final var isOptional = field.hasProto3Optional() && field.getProto3Optional();
    // Special case for strings
    if (field.getType() == Type.TYPE_STRING) {
      final var empty =
          CodeBlock.builder().add("$T.emptyToNull($N)", Strings.class, field.getName()).build();
      if (isOptional) {
        return CodeBlock.builder().addStatement("$N = $L", field.getName(), empty).build();
      } else {
        return CodeBlock.builder()
            .addStatement("$N = $T.requireNonNull($L)", field.getName(), Objects.class, empty)
            .build();
      }
    } else {

    }
    if (!isOptional) {
      if (field.getType() == Type.TYPE_STRING) {
        return CodeBlock.builder()
            .addStatement(
                "$T.requireNonNull($T.emptyToNull($N))",
                Objects.class,
                Strings.class,
                field.getName())
            .build();
      } else {
        return CodeBlock.builder()
            .addStatement("$T.requireNonNull($N)", Objects.class, field.getName())
            .build();
      }
    } else {
      return CodeBlock.builder().build();
    }
  }

  private String extractPackageName(DescriptorProtos.FileDescriptorProto proto) {
    DescriptorProtos.FileOptions options = proto.getOptions();
    if (options != null) {
      String javaPackage = options.getJavaPackage();
      if (!Strings.isNullOrEmpty(javaPackage)) {
        return javaPackage;
      }
    }

    return Strings.nullToEmpty(proto.getPackage());
  }
}
