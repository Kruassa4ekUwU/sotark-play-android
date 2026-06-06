plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace  = "com.sotark.play"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sotark.play"
        minSdk        = 26
        targetSdk     = 35
        versionCode   = 2
        versionName   = "1.1.0"
        buildConfigField("String", "BASE_URL",
            "\"https://sotark-play-server-production.up.railway.app/\"")
        buildConfigField("Boolean", "IS_BETA", "false")
    }

    signingConfigs {
        create("release") {
            storeFile     = file(System.getenv("KEYSTORE_PATH") ?: "sotark-play.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias      = System.getenv("KEY_ALIAS") ?: ""
            keyPassword   = System.getenv("KEY_PASSWORD") ?: ""
        }
    }

    flavorDimensions += "channel"
    productFlavors {
        create("stable") {
            dimension      = "channel"
            applicationId  = "com.sotark.play"
            versionNameSuffix = ""
            resValue("string", "channel_name", "Sotark Play")
            buildConfigField("Boolean", "IS_BETA", "false")
        }
        create("beta") {
            dimension      = "channel"
            applicationId  = "com.sotark.play.beta"
            versionNameSuffix = "-beta"
            resValue("string", "channel_name", "Sotark Play Beta")
            buildConfigField("Boolean", "IS_BETA", "true")
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled   = true
            isShrinkResources = true
            signingConfig     = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        compose     = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons)
    implementation(libs.androidx.navigation)
    implementation(libs.androidx.appcompat)

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    implementation(libs.coil.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.nav.compose)

    implementation(libs.kotlinx.coroutines)

    debugImplementation(libs.androidx.ui.tooling)
}
