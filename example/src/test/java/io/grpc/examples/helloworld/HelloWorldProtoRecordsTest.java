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
package io.grpc.examples.helloworld;

import com.google.common.truth.Truth;
import org.junit.jupiter.api.Test;

class HelloWorldProtoRecordsTest {

  @Test
  void records_exist() {
    Truth.assertThat(io.grpc.examples.helloworld.HelloWorldProtoRecords.HelloRequest.class)
        .isNotNull();
    Truth.assertThat(
            io.grpc.examples.helloworld.HelloWorldProtoRecords.HelloRequest.class.isRecord())
        .isTrue();
    Truth.assertThat(io.grpc.examples.helloworld.HelloWorldProtoRecords.HelloReply.class)
        .isNotNull();
    Truth.assertThat(io.grpc.examples.helloworld.HelloWorldProtoRecords.HelloReply.class.isRecord())
        .isTrue();
  }
}
