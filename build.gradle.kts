// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

subprojects {
    configurations.all {
        resolutionStrategy {
            force("androidx.lifecycle:lifecycle-common-java8:2.8.7")
            force("androidx.lifecycle:lifecycle-common:2.8.7")
            force("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
            force("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
            force("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
            force("androidx.navigation:navigation-compose:2.8.7")
            force("androidx.navigation:navigation-common:2.8.7")
            force("androidx.navigation:navigation-runtime:2.8.7")
        }
    }
}