# protoc-gen-java-records

A protoc compiler plugin that generates Java records from protobuf message definitions. The goal is to use `.proto` files as a schema definition language for generating clean, immutable Java records — not necessarily tied to gRPC or any RPC framework.

## Project Structure

- `protoc-gen-java-records/` — The protoc plugin (main source)
  - `src/main/java/io/ikstewa/grpc/protoc/javarecords/Main.java` — Entry point, extends jprotoc `Generator`
  - `src/main/java/io/ikstewa/grpc/protoc/javarecords/RecordGenerator.java` — Core code generation logic using JavaPoet
  - `src/test/proto/` — Test proto files
  - `src/test/java/` — Tests that verify the generated records compile and behave correctly
- `example/` — Example consumer project demonstrating plugin usage

## Tech Stack

- **Java 21** (required for record support in JavaPoet)
- **Gradle 8.13** with Kotlin DSL
- **Palantir JavaPoet** (fork of Square's JavaPoet — chosen because it supports generating Java `record` types)
- **jprotoc** (Salesforce) — Scaffolding for protoc plugin development
- **Spotless** with Google Java Format
- **JUnit 5** + **Google Truth** for testing

## Building and Testing

```sh
./gradlew build        # Build and run all tests
./gradlew test         # Run tests only
./gradlew spotlessApply  # Format code
```

The plugin is packaged as a shadow JAR (required because protoc plugins run as standalone executables — protoc spawns them as subprocesses communicating via stdin/stdout).

## How Code Generation Works

1. `protoc` invokes the plugin, sending a `CodeGeneratorRequest` via stdin
2. `Main` parses the request, builds a `ProtoTypeMap`, and delegates to `RecordGenerator` per proto file
3. `RecordGenerator` uses JavaPoet to build record type specs from proto message descriptors
4. Generated records are wrapped in an outer class named `{OuterClassname}Records` (e.g., `SimpleMessageProtoRecords`)
5. The plugin responds with `CodeGeneratorResponse` containing the generated `.java` files

## Current Capabilities

- Scalar types (all proto3 primitives, strings, bytes)
- Optional fields (mapped to `@Nullable`)
- Repeated fields (mapped to `List<T>`)
- Nested message types (nested records)
- Null-safety annotations via jspecify (`@NullMarked`, `@Nullable`)
- `java_outer_classname` support
- `java_package` support

## Known Limitations and Open Issues

Key gaps tracked in GitHub issues:

- **#15** — Only `java_multiple_files = false` is supported
- **#17** — Missing: enums, oneof fields, map fields, Any
- **#38** — Cross-file type references need a custom ProtoTypeMap
- **#14** — `json_fieldnames` option (partially referenced but not fully wired)
- **#18** — `deprecated` field option not handled
- **#8** — Generated records leak runtime deps (Guava, jspecify) onto consumers; needs a runtime library or inlining
- **#96** — Maven Central publishing not yet complete
- **#12** — No javadoc generation
- **#7** — No protoc insertion point support
- **#5** — No RecordBuilder integration
- **#4** — No type mappers between records and protobuf generated types

## Conventions

- License: Apache 2.0 — all source files must include the license header (see `HEADER` file)
- Formatting: Google Java Format enforced via Spotless. Run `./gradlew spotlessApply` before committing.
- The `example/` subproject depends on the local shadow JAR build of the plugin, not a published artifact.
- Proto test files live in `protoc-gen-java-records/src/test/proto/` and corresponding Java tests in `src/test/java/`.
