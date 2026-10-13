// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.sonarqube)
}

sonarqube {
    properties {
        property(
            "sonar.projectKey",
            System.getenv("SONAR_PROJECT_KEY_ANDROID") ?: "swp-25-26-groupe-a-android"
        )
        property("sonar.host.url", System.getenv("SONAR_HOST_URL"))
        property("sonar.token", System.getenv("SONAR_TOKEN"))
    }
}
