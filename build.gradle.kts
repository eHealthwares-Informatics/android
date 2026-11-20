plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false

    // Declared here, applied in the :app module (see app/build.gradle.kts, sentry {}).
    alias(libs.plugins.sentry.android) apply false
}
