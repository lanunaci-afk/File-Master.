
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.filemaster.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.filemaster.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Word DOCX
    implementation("org.apache.poi:poi-ooxml:5.3.0")

    // PowerPoint PPT и PPTX
    implementation("org.apache.poi:poi-scratchpad:5.3.0")
}
