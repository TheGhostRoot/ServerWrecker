plugins {
  `sf-project-conventions`
  alias(libs.plugins.blossom)
}

sourceSets {
  main {
    blossom {
      javaSources {
        property("version", rootProject.version.toString())
        property("description", rootProject.description)
        property("url", "https://soulfiremc.com")
        property("commit_hash", project.latestCommitHash())
        property("branch_name", project.branchName())
      }
    }
  }
}

idea {
  module {
    generatedSourceDirs.addAll(
      listOf(
        project.layout.buildDirectory
          .dir("generated/sources/blossom/main/java")
          .get().asFile,
      )
    )
  }
}
