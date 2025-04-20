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
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import com.salesforce.jprotoc.ProtoTypeMap;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.lang.model.element.Modifier;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/** Generator used to convert a Proto */
@NullMarked
class RecordGenerator {

  private static final ClassName STRING_TYPE = ClassName.get(String.class);

  private static final String OUTER_CLASS_SUFFIX = "Records";

  private final DescriptorProtos.FileDescriptorProto protoFile;
  private final boolean multipleClassFiles;
  private final ProtoTypeMap typeMap;

  public RecordGenerator(DescriptorProtos.FileDescriptorProto proto, ProtoTypeMap typeMap) {
    this.protoFile = proto;
    this.typeMap = typeMap;
    // TODO: https://github.com/ikstewa/protoc-gen-java-records/issues/15
    this.multipleClassFiles = false;
  }

  public Collection<JavaFile> buildRecords() {
    final var recordTypes =
        this.protoFile.getMessageTypeList().stream().map(this::buildRecord).toList();

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
          JavaFile.builder(extractPackageName(this.protoFile), outerType)
              .skipJavaLangImports(true)
              .build());
    }
  }

  private TypeSpec buildRecord(DescriptorProto message) {
    final var recordConstructorBuilder =
        MethodSpec.compactConstructorBuilder().addModifiers(Modifier.PUBLIC);

    // Build the record fields
    message.getFieldList().stream()
        .map(this::buildRecordField)
        .forEach(
            f -> {
              recordConstructorBuilder.addParameter(f.param());
              if (f.constructorCode() != null) {
                recordConstructorBuilder.addCode(f.constructorCode());
              }
            });

    final var recordBuilder =
        TypeSpec.recordBuilder(message.getName())
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(org.jspecify.annotations.NullMarked.class)
            .recordConstructor(recordConstructorBuilder.build());
    // Add nested messages as nested records
    message.getNestedTypeList().stream().map(this::buildRecord).forEach(recordBuilder::addType);
    return recordBuilder.build();
  }

  private record RecordField(ParameterSpec param, @Nullable CodeBlock constructorCode) {}

  private RecordField buildRecordField(FieldDescriptorProto field) {
    final var fieldType = mapFieldType(field);
    final var isOptional = isOptional(field);
    final var isRepeated = isRepeated(field);

    // Build record paramater
    final ParameterSpec param;
    final var paramBuilder = ParameterSpec.builder(fieldType, field.getName());
    if (isOptional) {
      paramBuilder.addAnnotation(org.jspecify.annotations.Nullable.class);
    }
    param = paramBuilder.build();

    // build optional constructor code block
    final @Nullable CodeBlock constructorCode;
    if (STRING_TYPE.equals(fieldType)) {
      // Special case for handling empty strings. This should be a configurable "option"...
      final var empty =
          CodeBlock.builder().add("$T.emptyToNull($N)", Strings.class, field.getName()).build();
      if (isOptional) {
        constructorCode =
            CodeBlock.builder().addStatement("$N = $L", field.getName(), empty).build();
      } else {
        constructorCode =
            CodeBlock.builder()
                .addStatement("$N = $T.requireNonNull($L)", field.getName(), Objects.class, empty)
                .build();
      }
    } else {
      if (!isOptional) {
        if (isRepeated) {
          constructorCode =
              CodeBlock.builder()
                  .beginControlFlow("if ($N == null)", field.getName())
                  .addStatement("$N = $T.of()", field.getName(), List.class)
                  .endControlFlow()
                  .build();
        } else {
          constructorCode =
              CodeBlock.builder()
                  .addStatement("$T.requireNonNull($N)", Objects.class, field.getName())
                  .build();
        }
      } else {
        constructorCode = null;
      }
    }

    return new RecordField(param, constructorCode);
  }

  private TypeName mapFieldType(FieldDescriptorProto field) {
    // Handle repeated fields
    if (isRepeated(field)) {
      return ParameterizedTypeName.get(
          ClassName.get(java.util.List.class), mapScalarType(field, true));
    }
    return mapScalarType(field, isOptional(field));
  }

  private TypeName mapScalarType(FieldDescriptorProto field, boolean useBoxedPrimitives) {
    final var type = field.getType();
    return switch (type) {
      case TYPE_STRING -> STRING_TYPE;
      case TYPE_BOOL -> useBoxedPrimitives ? ClassName.get(Boolean.class) : TypeName.BOOLEAN;
      case TYPE_BYTES -> ClassName.get("com.google.protobuf", "ByteString");
      case TYPE_DOUBLE -> useBoxedPrimitives ? ClassName.get(Double.class) : TypeName.DOUBLE;
      case TYPE_ENUM -> throw new UnsupportedOperationException("Unimplemented case: " + type);
      case TYPE_FIXED32, TYPE_INT32, TYPE_SFIXED32, TYPE_SINT32, TYPE_UINT32 ->
          useBoxedPrimitives ? ClassName.get(Integer.class) : TypeName.INT;
      case TYPE_FIXED64, TYPE_INT64, TYPE_SFIXED64, TYPE_SINT64, TYPE_UINT64 ->
          useBoxedPrimitives ? ClassName.get(Long.class) : TypeName.LONG;
      case TYPE_FLOAT -> useBoxedPrimitives ? ClassName.get(Float.class) : TypeName.FLOAT;
      case TYPE_GROUP -> throw new UnsupportedOperationException("Unimplemented case: " + type);
      case TYPE_MESSAGE -> findJavaClassName(field.getTypeName());
    };
  }

  private ClassName findJavaClassName(String protoTypeName) {
    final var javaTypeName = this.typeMap.toJavaTypeName(protoTypeName);

    // The outer class name used in the TypeMap does not have the special record naming. Remap.
    if (this.multipleClassFiles) {
      throw new UnsupportedOperationException(
          "TODO: https://github.com/ikstewa/protoc-gen-java-records/issues/15");
    } else {
      // FIXME: We can do better: https://github.com/ikstewa/protoc-gen-java-records/issues/38
      final var javaOuterClass = ProtoTypeMap.getJavaOuterClassname(this.protoFile);
      final var recordOuterClass = javaOuterClass + OUTER_CLASS_SUFFIX;
      if (javaTypeName.contains(javaOuterClass)) {
        final var recordTypeName = javaTypeName.replace(javaOuterClass, recordOuterClass);
        return ClassName.bestGuess(recordTypeName);
      } else {
        throw new UnsupportedOperationException(
            "TODO: https://github.com/ikstewa/protoc-gen-java-records/issues/38");
      }
    }
  }

  private static boolean isOptional(FieldDescriptorProto field) {
    return field.hasProto3Optional() && field.getProto3Optional();
  }

  private static boolean isRepeated(FieldDescriptorProto field) {
    return field.getLabel() == FieldDescriptorProto.Label.LABEL_REPEATED;
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
