pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SourdoughCompanion"

// :engine is plain Kotlin (no Android) so scheduling and temperature maths can be unit-tested on the JVM.
include(":engine")
include(":app")
