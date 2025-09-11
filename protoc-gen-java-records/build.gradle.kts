plugins {
  application
  jacoco
  `maven-publish`
  signing
  id("com.gradleup.shadow") version "8.3.6"
  id("com.google.protobuf") version "0.9.5"
}

java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

application { mainClass = "io.ikstewa.grpc.protoc.javarecords.Main" }

val protobufVersion: String by rootProject.extra

dependencies {
  implementation(platform("com.google.protobuf:protobuf-bom:$protobufVersion"))

  implementation("com.google.protobuf:protobuf-java-util")
  implementation("com.salesforce.servicelibs:jprotoc:1.2.2")
  implementation("com.palantir.javapoet:javapoet:0.7.0")

  // BEGIN: FIXME: https://github.com/ikstewa/protoc-gen-java-records/issues/8
  implementation("org.jspecify:jspecify:1.0.0")
  implementation("com.google.guava:guava:33.4.8-jre")
  // END: FIXME

  testImplementation("org.junit.jupiter:junit-jupiter-api")
  testImplementation("com.google.truth.extensions:truth-java8-extension:1.4.5")
  testImplementation("com.google.truth:truth:1.4.4")
}

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

tasks.test {
  finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
  // systemProperty("log4j2.configurationFile", "log4j2-test.xml")
  // systemProperty("sun.io.serialization.extendedDebugInfo", "true")
}

tasks.jacocoTestReport {
  dependsOn(tasks.test) // tests are required to run before generating the report
  reports {
    xml.required.set(true)
    csv.required.set(true)
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

signing { sign(publishing.publications["protocPlugin"]) }

tasks.named("generateTestProto") { dependsOn(tasks.named("shadowJar")) }

protobuf {
  protoc { artifact = "com.google.protobuf:protoc:$protobufVersion" }

  plugins {
    create("java-records") {
      path = tasks.named("shadowJar").map { it.outputs.files.singleFile.path }.get()
    }
  }
  generateProtoTasks {
    ofSourceSet("test").forEach {
      it.plugins {
        create("java-records") {
          // outputSubDir = "java"
        }
      }
    }
  }
}
