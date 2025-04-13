# protoc-gen-java-records
Protoc codegen plugin for generating simple java records

Follow these recommendations for the best results: https://protobuf.dev/reference/java/java-proto-names/

## `java_multiple_files`

TODO: Currently only supports false: https://github.com/ikstewa/protoc-gen-java-records/issues/15

### Outer class

When multiple files is false, the records are generated under a single outer class file.

This follows the same naming conventions as the default java compiler: https://protobuf.dev/reference/java/java-generated/#invocation

To avoid naming conflicts the out class name is suffixed with `Records`, ex: `FooBarOuterClassRecords`

The following proto will generate a record at `foo.bar.SimpleMessageProtoRecords.Simple`:

```proto
syntax = "proto3";

option java_multiple_files = false;
option java_package = "io.ikstewa.grpc.protoc.test";
option java_outer_classname = "SimpleMessageProto";


message Simple {
  string name = 1;
}
```

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
* https://github.com/Fadelis/protoc-gen-java-optional/tree/master
* https://github.com/salesforce/grpc-java-contrib/tree/master/jprotoc/jprotoc

## TODO
Support java standard options: https://protobuf.dev/reference/java/java-proto-names/
* `option java_multiple_files = true;`
* `option java_outer_classname = "FileNameProto";`
* `option java_package = "com.google.package";`
