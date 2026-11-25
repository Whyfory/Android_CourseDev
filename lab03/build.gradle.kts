plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.lab03"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.lab03"
        minSdk = 24
        targetSdk = 37
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation("androidx.activity:activity-ktx:1.8.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
}
