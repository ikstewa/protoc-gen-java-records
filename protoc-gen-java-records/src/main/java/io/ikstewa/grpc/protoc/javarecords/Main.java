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

import com.google.protobuf.compiler.PluginProtos;
import com.palantir.javapoet.JavaFile;
import com.salesforce.jprotoc.Generator;
import com.salesforce.jprotoc.GeneratorException;
import com.salesforce.jprotoc.ProtoTypeMap;
import com.salesforce.jprotoc.ProtocPlugin;
import java.util.Collections;
import java.util.List;

public final class Main extends Generator {

  private Main() {}

  public static void main(String[] args) {
    if (args.length == 0) {
      ProtocPlugin.generate(new Main());
      // ProtocPlugin.generate(new Main(), new com.salesforce.jprotoc.dump.DumpGenerator());
    } else {
      ProtocPlugin.debug(new Main(), args[0]);
    }
  }

  @Override
  protected List<PluginProtos.CodeGeneratorResponse.Feature> supportedFeatures() {
    return Collections.singletonList(
        PluginProtos.CodeGeneratorResponse.Feature.FEATURE_PROTO3_OPTIONAL);
  }

  @Override
  public List<PluginProtos.CodeGeneratorResponse.File> generateFiles(
      PluginProtos.CodeGeneratorRequest request) throws GeneratorException {

    final var typeMap = ProtoTypeMap.of(request.getProtoFileList());

    // Generate record files
    return request.getProtoFileList().stream()
        .filter(protoFile -> request.getFileToGenerateList().contains(protoFile.getName()))
        .map(pf -> new RecordGenerator(pf, typeMap))
        .flatMap(gen -> gen.buildRecords().stream().map(this::toResponseFile))
        .toList();
  }

  private PluginProtos.CodeGeneratorResponse.File toResponseFile(JavaFile javaFile) {
    final var fileName =
        javaFile.packageName().replace('.', '/') + '/' + javaFile.typeSpec().name() + ".java";
    return makeFile(fileName, javaFile.toString());
  }
}
