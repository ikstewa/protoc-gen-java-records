plugins {
  java
  id("com.google.protobuf") version "0.9.4"
}

dependencies {
  implementation("com.google.protobuf:protobuf-java:3.25.1") // or the latest version
}

spotless {
  java {
    targetExclude("**/build/generated/**")
    importOrder()
    removeUnusedImports()
    googleJavaFormat()

    licenseHeaderFile(rootProject.file("HEADER"))
  }
}

// --------------------------------------------------------------------------------
// Used to declare a sub-project dependency for the shadow jar on a local build
// External would use the maven formula as the "artifact" for the plugin
evaluationDependsOn(":protoc-gen-java-records") // Needed to load cross-project task

tasks.named("generateProto") {
  dependsOn(project(":protoc-gen-java-records").tasks.named("shadowJar"))
}

val localPluginJarPath =
    project(":protoc-gen-java-records").tasks.named("shadowJar").map {
      it.outputs.files.singleFile.path
    }

// End: local build
// --------------------------------------------------------------------------------

val protobufVersion: String by rootProject.extra

protobuf {
  protoc { artifact = "com.google.protobuf:protoc:$protobufVersion" }

  plugins {
    create("java-records") {
      path = localPluginJarPath.get()
      // Use below for a published version
      // artifact = "io.github.ikstewa:protoc-gen-java-records:${project.version}:all@jar"
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
