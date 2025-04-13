plugins {
  java
  id("com.google.protobuf") version "0.9.4"
}

spotless {
  // chose the Google java formatter, version 1.9
  java {
    targetExclude("**/build/generated/**")
    importOrder()
    removeUnusedImports()
    googleJavaFormat()

    // and apply a license header
    licenseHeaderFile(rootProject.file("HEADER"))
  }
}

val protobufVersion: String by rootProject.extra

protobuf {
  protoc { artifact = "com.google.protobuf:protoc:$protobufVersion" }

  // val pluginJar = file("${project.rootProject.rootDir}/protoc-gen-java-records/build" +
  //   "/libs/protoc-gen-java-records-${project.version}-all.jar")

  plugins {
    create("java-records") {
      // path = pluginJar.path
      artifact = "io.github.ikstewa:protoc-gen-java-records:${project.version}:all@jar"
    }
  }
  generateProtoTasks {
    ofSourceSet("main").forEach {
      it.plugins {
        create("java-records") {
          // Properties here?
          // outputSubDir = "java"
        }
      }
    }
  }
}
