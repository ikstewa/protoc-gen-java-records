# protoc-gen-java-records
Protoc codegen plugin for generating simple java records

## Options
The plugin behavior can be controlled using options. Example on how to set when using `protobuf-gradle-plugin`:

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
                create("java-records") {
                    option("json_fieldnames")
                }
            }
        }
    }
}
```

### ``json_fieldnames`` (default: disabled)
Generated record fields will use the json name determined by the compiler. This will typically be lowerCamerCase or the
`json_name` option is used if specified. See: https://protobuf.dev/programming-guides/json/

## Reference
* https://github.com/apple/servicetalk/tree/main/servicetalk-grpc-protoc
* https://github.com/apple/servicetalk/blob/main/servicetalk-examples/grpc/helloworld/build.gradle#L45
* https://github.com/palantir/javapoet

## TODO
Support java standard options: https://protobuf.dev/reference/java/java-proto-names/
* `option java_multiple_files = true;`
* `option java_outer_classname = "FileNameProto";`
* `option java_package = "com.google.package";`
