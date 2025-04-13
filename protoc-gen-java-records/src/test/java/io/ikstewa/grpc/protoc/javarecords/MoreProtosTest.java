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

import com.google.common.truth.Truth;
import io.test.protoc.AnotherThing;
import org.junit.jupiter.api.Test;

class MoreProtosTest {

  @Test
  void basic_test() {
    Truth.assertThat(io.test.protoc.AnotherThing.class).isNotNull();
    Truth.assertThat(io.test.protoc.AnotherThing.class.isRecord()).isTrue();
    Truth.assertThat(new AnotherThing("foo")).isEqualTo(new AnotherThing("foo"));
  }
}
