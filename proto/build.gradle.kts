import com.google.protobuf.gradle.id

plugins {
  `sf-project-conventions`
  alias(libs.plugins.protobuf)
}

dependencies {
  // gRPC
  api(libs.bundles.grpc)

  // So everything compiles
  implementation(libs.javax.annotations)
}

protobuf {
  protoc {
    artifact = "com.google.protobuf:protoc:${libs.versions.protobuf.get()}"
  }
  plugins {
    id("grpc") {
      artifact = "io.grpc:protoc-gen-grpc-java:${libs.versions.grpc.get()}"
    }
  }
  generateProtoTasks {
    ofSourceSet("main").forEach {
      it.plugins {
        // Apply the "grpc" plugin whose spec is defined above, without options.
        id("grpc")
      }
    }
  }
}

tasks.withType<Checkstyle> {
  exclude("**/com/soulfiremc/grpc/generated**")
}

idea {
  module {
    generatedSourceDirs.addAll(
      listOf(
        file("${protobuf.generatedFilesBaseDir}/main/grpc"),
        file("${protobuf.generatedFilesBaseDir}/main/java")
      )
    )
  }
}
