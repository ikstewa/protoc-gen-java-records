plugins {
  java
  id("com.google.protobuf") version "0.9.5"
}

dependencies {
  implementation("com.google.protobuf:protobuf-java:3.25.1") // or the latest version

  // BEGIN: FIXME: https://github.com/ikstewa/protoc-gen-java-records/issues/8
  implementation("org.jspecify:jspecify:1.0.0")
  implementation("com.google.guava:guava:33.5.0-jre")
  // END: FIXME

  testImplementation("org.junit.jupiter:junit-jupiter-api")
  testImplementation("com.google.truth.extensions:truth-java8-extension:1.4.5")
  testImplementation("com.google.truth:truth:1.4.4")
}

testing {
  suites {
    val test by getting(JvmTestSuite::class) { useJUnitJupiter("5.11.3") }
  }
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
