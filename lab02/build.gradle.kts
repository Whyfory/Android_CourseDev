plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.lab02"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.lab02"
        minSdk = 24
        targetSdk = 37
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
}
