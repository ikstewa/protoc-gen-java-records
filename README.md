# protoc-gen-java-records

A [protoc](https://protobuf.dev/) compiler plugin that generates Java `record` types from protobuf message definitions.

Use `.proto` files as a schema definition language to generate clean, immutable Java records — no hand-writing required. This plugin is not tied to gRPC or any RPC framework; it simply turns proto messages into idiomatic Java records.

## Features

- All proto3 scalar types (int32, int64, string, bytes, bool, float, double, etc.)
- `optional` fields mapped to `@Nullable` with null-safety annotations ([jspecify](https://jspecify.dev/))
- `repeated` fields mapped to `List<T>`
- Nested message types as nested records
- `java_package` and `java_outer_classname` support

## Usage

Add the plugin to your Gradle build using the [protobuf-gradle-plugin](https://github.com/google/protobuf-gradle-plugin):

```kotlin
protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.6"
    }

    plugins {
        create("java-records") {
            artifact = "io.github.ikstewa:protoc-gen-java-records:${protocJavaRecordsVersion}:all@jar"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                create("java-records")
            }
        }
    }
}
```

See the [example](example/build.gradle.kts) project for a complete working setup.

### Runtime Dependencies

The generated records currently require these dependencies on the consumer's classpath:

```kotlin
implementation("org.jspecify:jspecify:1.0.0")
implementation("com.google.guava:guava:33.5.0-jre")
```

> **Note:** A lightweight runtime library to eliminate these transitive dependencies is planned ([#8](https://github.com/ikstewa/protoc-gen-java-records/issues/8)).

## Generated Code

Records are generated inside an outer class suffixed with `Records`. This follows the same naming conventions as the default Java protoc compiler ([reference](https://protobuf.dev/reference/java/java-generated/#invocation)).

For example, this proto:

```proto
syntax = "proto3";

option java_package = "io.ikstewa.grpc.protoc.test";
option java_outer_classname = "SimpleMessageProto";

message Simple {
    string name = 1;
}
```

Generates a record at `io.ikstewa.grpc.protoc.test.SimpleMessageProtoRecords.Simple`.

### Proto Naming Recommendations

Follow the [official protobuf Java naming guide](https://protobuf.dev/reference/java/java-proto-names/) for best results.

## Options

Plugin options can be set via the `protobuf-gradle-plugin`:

```kotlin
generateProtoTasks {
    ofSourceSet("main").forEach {
        it.plugins {
            create("java-records") {
                option("json_fieldnames")
            }
        }
    }
}
```

### `json_fieldnames` (default: disabled)

Generated record fields will use the JSON name determined by the compiler. This is typically lowerCamelCase, or the `json_name` option if specified. See: [protobuf JSON mapping](https://protobuf.dev/programming-guides/json/).

## Limitations

The following proto features are not yet supported:

- `java_multiple_files = true` ([#15](https://github.com/ikstewa/protoc-gen-java-records/issues/15))
- Enum types ([#17](https://github.com/ikstewa/protoc-gen-java-records/issues/17))
- Oneof fields ([#17](https://github.com/ikstewa/protoc-gen-java-records/issues/17))
- Map fields ([#17](https://github.com/ikstewa/protoc-gen-java-records/issues/17))
- `Any` type ([#17](https://github.com/ikstewa/protoc-gen-java-records/issues/17))
- Cross-file message references ([#38](https://github.com/ikstewa/protoc-gen-java-records/issues/38))
- `deprecated` option ([#18](https://github.com/ikstewa/protoc-gen-java-records/issues/18))

## Building from Source

Requires Java 21.

```sh
./gradlew build
```

## License

[Apache License 2.0](LICENSE)

## References

- [protobuf Java generated code reference](https://protobuf.dev/reference/java/java-generated/)
- [jprotoc](https://github.com/salesforce/grpc-java-contrib/tree/master/jprotoc/jprotoc) — protoc plugin scaffolding
- [Palantir JavaPoet](https://github.com/palantir/javapoet) — Java source code generation (with record support)
- [protoc-gen-java-optional](https://github.com/Fadelis/protoc-gen-java-optional) — similar protoc plugin for reference
