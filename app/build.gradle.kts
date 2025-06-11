plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.myvault"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myvault"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // BOM => herramientas generales de Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
// Herramientas de analíticas de uso
    implementation("com.google.firebase:firebase-analytics")
// Módulo Firebase Real Time
    implementation("com.google.firebase:firebase-database")
// Módulo Firebase Authentication
    implementation("com.google.firebase:firebase-auth")
// Servicios de Google
    implementation("com.google.android.gms:play-services-auth:20.0.1")

    implementation("com.google.firebase:firebase-storage")
    implementation ("com.android.volley:volley:1.2.1")
    implementation ("com.squareup.picasso:picasso:2.71828")
    implementation ("com.google.firebase:firebase-firestore:24.10.0")
    implementation ("com.google.android.flexbox:flexbox:3.0.0")
    implementation("com.google.firebase:firebase-storage")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("org.json:json:20240303")





}