import net.researchgate.release.ReleaseExtension

plugins {
  id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
  id("net.researchgate.release") version "3.1.0"
  id("com.diffplug.spotless") version "7.1.0"
}

configure<ReleaseExtension> { with(git) { requireBranch.set("master") } }

allprojects {
  apply(plugin = "com.diffplug.spotless")

  repositories {
    mavenLocal()
    mavenCentral()
  }

  spotless {
    format("misc") {
      target("*.md", ".gitignore")

      trimTrailingWhitespace()
      leadingTabsToSpaces()
      endWithNewline()
    }
    kotlinGradle {
      target("*.gradle.kts")
      ktfmt()
    }
  }
}

nexusPublishing {
  repositories {
    sonatype {
      // only for users registered in Sonatype after 24 Feb 2021
      nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
      snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))

      // defaults to project.properties["myNexusUsername"]
      username.set(findProperty("ossrhUsername") as? String ?: "unset")
      // defaults to project.properties["myNexusPassword"]
      password.set(findProperty("ossrhPassword") as? String ?: "unset")
    }
  }
}

// FIXME: Upgrading breaks generated code for repeated fields??
val protobufVersion by extra("3.22.5")
