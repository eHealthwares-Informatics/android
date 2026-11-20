import java.util.Properties

val localProperties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.sentry.android)
}

android {
    namespace = "com.ehealthwares.rxsoft"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ehealthwares.rxsoft"
        minSdk = 26
        targetSdk = 36
        versionCode = 17
        versionName = "0.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8000/api/\"")
        buildConfigField("String", "DEFAULT_WEB_CHANNEL_ID", "\"69bd061c11bf835d976c4e2f\"")
        buildConfigField("String", "PRINTER_URL", "\"http://10.0.2.2:8094\"")

        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"763036387173-jml5o3iauj2ms5269dqf7qq24gplh6mq.apps.googleusercontent.com\"",
        )
        val localProps = Properties().apply {
            val f = rootProject.file("local.properties")
            if (f.exists()) f.inputStream().use { load(it) }
        }
        manifestPlaceholders["FACEBOOK_APP_ID"] =
            (localProps.getProperty("FACEBOOK_APP_ID") as String?)
                ?: System.getenv("FACEBOOK_APP_ID") ?: "28296926366638041"
        manifestPlaceholders["FACEBOOK_CLIENT_TOKEN"] =
            (localProps.getProperty("FACEBOOK_CLIENT_TOKEN") as String?)
                ?: System.getenv("FACEBOOK_CLIENT_TOKEN") ?: ""
        resValue("string", "fb_login_protocol_scheme", "fb${manifestPlaceholders["FACEBOOK_APP_ID"]}")
        buildConfigField(
            "String",
            "FACEBOOK_APP_ID",
            "\"${manifestPlaceholders["FACEBOOK_APP_ID"]}\"",
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Release builds ship pointing at production; the emulator default
            // above is only for debug builds.
            buildConfigField("String", "API_BASE_URL", "\"https://api.ehealthwares.com\"")
        }
    }

    // Name the built APK with app-specific prefix, e.g. rxsoft-release-1.0.3.apk
    applicationVariants.all {
        val variantName = name
        val vName = versionName
        outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val ext = output.outputFile?.extension?.ifBlank { "apk" } ?: "apk"
            output.outputFileName = "rxsoft-$variantName-$vName.$ext"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// ── Sentry (same org/project as rxshop, prognocare, smsgateway) ──
// Must live in the app module: the plugin ignores the configuration when it is
// declared on the root project ("only supported for the app module" warning),
// which made release builds fail with "A project ID or slug is required".
sentry {
    org.set("ehealthwares")
    projectName.set("ehealthwares_android")
}

// ── Release signing (configured after android block for Kotlin DSL compatibility) ──
val releaseSigningConfig = android.signingConfigs.create("release")
releaseSigningConfig.setStoreFile(file(localProperties.getProperty("RELEASE_KEYSTORE_PATH") ?: ""))
releaseSigningConfig.setStorePassword(localProperties.getProperty("RELEASE_KEYSTORE_PASSWORD") ?: "")
releaseSigningConfig.setKeyAlias(localProperties.getProperty("RELEASE_KEY_ALIAS") ?: "release")
releaseSigningConfig.setKeyPassword(localProperties.getProperty("RELEASE_KEY_PASSWORD") ?: "")
android.buildTypes.getByName("release").setSigningConfig(releaseSigningConfig)

dependencies {
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.material)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    debugImplementation(libs.androidx.ui.tooling)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.moshi)
    ksp(libs.moshi.codegen)

    // Chat realtime (Conversation Engine socket)
    implementation(libs.socketio.client)

    // Data
    implementation(libs.androidx.datastore.preferences)

    // Room (offline SQLite cache)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Images
    implementation(libs.coil.compose)

    // Social sign-in (Google Credential Manager + Facebook Login)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.facebook.login)

    // Sentry
    implementation(libs.sentry.android)

    // Security
    implementation(libs.androidx.security.crypto)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockwebserver)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
