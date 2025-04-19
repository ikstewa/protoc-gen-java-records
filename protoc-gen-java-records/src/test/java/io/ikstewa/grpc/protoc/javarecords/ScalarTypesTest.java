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
import io.test.protoc.ScalarTypesOuterClassRecords.OptionalScalarTypes;
import io.test.protoc.ScalarTypesOuterClassRecords.RepeatedScalarTypes;
import io.test.protoc.ScalarTypesOuterClassRecords.ScalarTypes;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ScalarTypesTest {

  @Test
  void basic_test() {
    Truth.assertThat(io.test.protoc.ScalarTypesOuterClassRecords.ScalarTypes.class).isNotNull();
    Truth.assertThat(io.test.protoc.ScalarTypesOuterClassRecords.ScalarTypes.class.isRecord())
        .isTrue();
    Truth.assertThat(
            new ScalarTypes(1.1d, 1.1f, 1, 1l, 2, 2l, 3, 3l, 4, 4l, 5, 5l, true, "string_value"))
        .isEqualTo(
            new ScalarTypes(1.1d, 1.1f, 1, 1l, 2, 2l, 3, 3l, 4, 4l, 5, 5l, true, "string_value"));
  }

  @Test
  void empty_string_fails_when_required() {
    Assertions.assertThrows(
        NullPointerException.class,
        () -> new ScalarTypes(1.1d, 1.1f, 1, 1l, 2, 2l, 3, 3l, 4, 4l, 5, 5l, true, ""));
  }

  @Test
  void optional_test() {
    Truth.assertThat(io.test.protoc.ScalarTypesOuterClassRecords.OptionalScalarTypes.class)
        .isNotNull();
    Truth.assertThat(
            io.test.protoc.ScalarTypesOuterClassRecords.OptionalScalarTypes.class.isRecord())
        .isTrue();
    Truth.assertThat(
            new OptionalScalarTypes(
                1.1d, 1.1f, 1, 1l, 2, 2l, 3, 3l, 4, 4l, 5, 5l, true, "string_value"))
        .isEqualTo(
            new OptionalScalarTypes(
                1.1d, 1.1f, 1, 1l, 2, 2l, 3, 3l, 4, 4l, 5, 5l, true, "string_value"));
    Truth.assertThat(
            new OptionalScalarTypes(
                null, null, null, null, null, null, null, null, null, null, null, null, null, null))
        .isEqualTo(
            new OptionalScalarTypes(
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                null));
  }

  @Test
  void empty_string_is_null_when_optional() {
    Truth.assertThat(
            new OptionalScalarTypes(
                null, null, null, null, null, null, null, null, null, null, null, null, null, ""))
        .isEqualTo(
            new OptionalScalarTypes(
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                null));
  }

  @Test
  void repeated_tests() {
    Truth.assertThat(io.test.protoc.ScalarTypesOuterClassRecords.RepeatedScalarTypes.class)
        .isNotNull();
    Truth.assertThat(
            io.test.protoc.ScalarTypesOuterClassRecords.RepeatedScalarTypes.class.isRecord())
        .isTrue();
    Truth.assertThat(
            new RepeatedScalarTypes(
                List.of(1.1d),
                List.of(1.1f),
                List.of(1),
                List.of(1l),
                List.of(2),
                List.of(2l),
                List.of(3),
                List.of(3l),
                List.of(4),
                List.of(4l),
                List.of(5),
                List.of(5l),
                List.of(true),
                List.of("string_value")))
        .isEqualTo(
            new RepeatedScalarTypes(
                List.of(1.1d),
                List.of(1.1f),
                List.of(1),
                List.of(1l),
                List.of(2),
                List.of(2l),
                List.of(3),
                List.of(3l),
                List.of(4),
                List.of(4l),
                List.of(5),
                List.of(5l),
                List.of(true),
                List.of("string_value")));
    Truth.assertThat(
            new RepeatedScalarTypes(
                null, null, null, null, null, null, null, null, null, null, null, null, null, null))
        .isEqualTo(
            new RepeatedScalarTypes(
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                null));
  }

  @Test
  void null_list_is_empty() {
    Truth.assertThat(
            new RepeatedScalarTypes(
                null, null, null, null, null, null, null, null, null, null, null, null, null, null))
        .isEqualTo(
            new RepeatedScalarTypes(
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of()));
  }
}
