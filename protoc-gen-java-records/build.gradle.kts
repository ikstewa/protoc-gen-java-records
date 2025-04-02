plugins {
  application
  `maven-publish`
  signing
  id("com.gradleup.shadow") version "8.3.6"
  id("com.google.protobuf") version "0.9.4"
}

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

application { mainClass = "io.ikstewa.grpc.protoc.javarecords.Main" }

testing {
  suites {
    // Configure the built-in test suite
    val test by
        getting(JvmTestSuite::class) {
          // Use JUnit Jupiter test framework
          useJUnitJupiter("5.11.3")
        }
  }
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

publishing {
  publications {
    create<MavenPublication>("protocPlugin") {
      from(components["shadow"])
      // pom {
      //   name.set("Flink GRPC Connector")
      //   description.set("Flink connectors for GRPC services.")
      //   url.set("https://github.com/ikstewa/flink-connector-grpc/")
      //   licenses {
      //       license {
      //           name.set("The Apache License, Version 2.0")
      //           url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
      //       }
      //   }
      //   developers {
      //       developer {
      //           id.set("ikstewa")
      //           name.set("Ian Stewart")
      //           url.set("https://github.com/ikstewa/")
      //       }
      //   }
      //   scm {
      //       url.set("https://github.com/ikstewa/flink-connector-grpc/")
      //       connection.set("scm:git:git://github.com/ikstewa/flink-connector-grpc/")
      //       developerConnection.set("scm:git:ssh://github.com/ikstewa/flink-connector-grpc/")
      //   }
      // }
    }
  }
}

signing {
  sign(publishing.publications["protocPlugin"])
}

// val protobufVersion: String by rootProject.extra
// protobuf {
//     protoc {
//         artifact = "com.google.protobuf:protoc:$protobufVersion"
//     }
//
//     // val pluginJar = file("${project.rootProject.rootDir}/protoc-gen-java-records/build" +
//     //   "/libs/protoc-gen-java-records-${project.version}-all.jar")
//
//     plugins {
//         create("java-records") {
//             // path = pluginJar.path
//             artifact = "io.github.ikstewa:protoc-gen-java-records:${project.version}:all@jar"
//         }
//     }
//     generateProtoTasks {
//         ofSourceSet("test").forEach {
//             it.plugins {
//                 create("java-records") {
//                 }
//             }
//         }
//     }
// }
