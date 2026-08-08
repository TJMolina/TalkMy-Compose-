plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.multiplatform)
}

android {
    namespace = "com.example.talkmy"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.talkmy"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
        create("profile") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // UI & Compose (Multiplatform as requested)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.icons)
    implementation(libs.compose.icons.extended)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.foundation)
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui.text)
    
    // AndroidX Core & Lifecycle
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    
    // Scraping & Network
    implementation(libs.jsoup)
    implementation(libs.nicehttp)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    
    // Persistence (Room)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    
    // Notes & Utils
    implementation(libs.markwon.core)
    implementation(libs.markwon.html)
    implementation(libs.fuzzywuzzy)
    implementation(libs.compose.preference)
    implementation(libs.mlkit.translate)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.kotlinx.collections.immutable)
    implementation(libs.kotlinx.io.core)

    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation(libs.compose.ui.tooling)
}