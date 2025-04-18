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
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.TypeSpec;
import com.salesforce.jprotoc.ProtoTypeMap;
import java.util.Collection;
import java.util.List;
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
      // Generate a new outer class file which contains all the records for this proto file
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
        .recordConstructor(
            MethodSpec.compactConstructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameters(
                    message.getFieldList().stream().map(RecordGenerator::toParameterSpec).toList())
                // .addCode()
                .build())
        .build();
  }

  private static ParameterSpec toParameterSpec(FieldDescriptorProto field) {
    final var type =
        switch (field.getType()) {
          case TYPE_STRING -> String.class;
          default ->
              throw new UnsupportedOperationException("Type not implemented: " + field.getType());
        };
    return ParameterSpec.builder(ClassName.get(type), field.getName()).build();
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
