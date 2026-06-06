plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)        // Compose compiler; replaces composeOptions
    alias(libs.plugins.kotlin.serialization)  // required for @Serializable nav routes
    alias(libs.plugins.ksp)                   // required for Room
}

import java.util.Properties

val verName = "1.5.13"

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.skriv.app"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.skriv.app"
        minSdk = 31
        targetSdk = 36
        versionName = verName
        versionCode = verName.split(".").let {
            val major = it.getOrNull(0)?.toIntOrNull() ?: 1
            val minor = it.getOrNull(1)?.toIntOrNull() ?: 0
            val patch = it.getOrNull(2)?.toIntOrNull() ?: 0
            major * 10000 + minor * 100 + patch
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    signingConfigs {
        create("release") {
            val storePasswordProp = localProperties.getProperty("signing.storePassword")
            if (storePasswordProp != null) {
                storeFile = rootProject.file("release.keystore")
                storePassword = storePasswordProp
                keyAlias = localProperties.getProperty("signing.keyAlias")
                keyPassword = localProperties.getProperty("signing.keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false // set to false for simple personal use to avoid Proguard issues
            val releaseConfig = signingConfigs.findByName("release")
            if (releaseConfig?.storeFile?.exists() == true) {
                signingConfig = releaseConfig
            }
        }
    }
}


dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.animation)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.datastore.preferences)
    implementation(libs.coroutines.android)
    implementation(libs.core.ktx)
    implementation(libs.activity.compose)
    implementation(libs.window)
    implementation(libs.material3.adaptive)
    implementation(libs.serialization.json)
    debugImplementation(libs.compose.ui.tooling)  // required for @Preview rendering in Android Studio
}
