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

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import com.palantir.javapoet.JavaFile;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public final class Main {

  private Main() {}

  /**
   * Program entry point. It is expected a {@link CodeGeneratorRequest} will be read from stdin, and
   * a {@link CodeGeneratorResponse} will be written to stdout.
   *
   * @param args the program arguments
   * @throws IOException if an exception occurs while parsing input from stdin
   */
  public static void main(final String... args) throws IOException {
    safeGenerate(CodeGeneratorRequest.parseFrom(System.in), args).writeTo(System.out);
  }

  /**
   * Generate response from request while ensuring that any exceptions thrown during generation are
   * transformed in to an appropriate error response.
   *
   * @param request The generation request
   * @param args command-line args passed in from the generator plugin
   * @return The generation response
   */
  private static CodeGeneratorResponse safeGenerate(
      final CodeGeneratorRequest request, final String... args) {
    try {
      return generate(request, argsToOptions(args));
    } catch (final Throwable t) {
      final StringWriter sw = new StringWriter(1024);
      sw.append("Code generation failed: ");
      try (PrintWriter pw = new PrintWriter(sw)) {
        t.printStackTrace(pw);
      }
      return CodeGeneratorResponse.newBuilder().setError(sw.toString()).build();
    }
  }

  /**
   * Generate response from code generation request.
   *
   * @param request The code generation request
   * @param optionsMap options map supplied from plugins on the command line
   * @return The code generation response
   */
  private static CodeGeneratorResponse generate(
      final CodeGeneratorRequest request, final Map<String, String> optionsMap) {
    final CodeGeneratorResponse.Builder responseBuilder = CodeGeneratorResponse.newBuilder();

    // Add features
    responseBuilder.setSupportedFeatures(
        CodeGeneratorResponse.Feature.FEATURE_PROTO3_OPTIONAL.getNumber());

    // Generate debug files
    // responseBuilder.addFile(
    //     CodeGeneratorResponse.File.newBuilder()
    //         .setName("descriptor_dump")
    //         .setContentBytes(ByteString.copyFrom(request.toByteArray()))
    //         .build());
    // try {
    //   responseBuilder.addFile(
    //       CodeGeneratorResponse.File.newBuilder()
    //           .setName("descriptor_dump.json")
    //           .setContent(JsonFormat.printer().print(request))
    //           .build());
    // } catch (InvalidProtocolBufferException e) {
    //   throw new RuntimeException(e);
    // }

    // Generate record files
    for (final var protoFile : request.getProtoFileList()) {
      if (request.getFileToGenerateList().contains(protoFile.getName())) {
        responseBuilder.addAllFile(
            new RecordGenerator(protoFile)
                .buildRecords().stream().map(Main::toResponseFile).toList());
      }
    }

    return responseBuilder.build();
  }

  private static CodeGeneratorResponse.File toResponseFile(JavaFile javaFile) {
    final var fileName =
        javaFile.packageName().replace('.', '/') + '/' + javaFile.typeSpec().name() + ".java";
    return CodeGeneratorResponse.File.newBuilder()
        .setName(fileName)
        .setContent(javaFile.toString())
        .build();
  }

  /**
   * Helper method to turn command line arguments into option key/value pairs.
   *
   * @param args the arguments list, can be empty.
   * @return a (potentially empty) map of arguments.
   */
  private static Map<String, String> argsToOptions(final String... args) {
    final Map<String, String> optionsMap = new HashMap<>();
    for (String arg : args) {
      String[] kv = arg.split("=");
      if (kv.length != 2) {
        throw new IllegalArgumentException(
            "Command line argument must be of shape: key=value " + "(is: " + arg + ")");
      }
      optionsMap.put(kv[0], kv[1]);
    }
    return optionsMap;
  }
}
