plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.jetbrains.androidApp"
    compileSdk = 33
    defaultConfig {
        applicationId = "com.jetbrains.androidApp"
        minSdk = 25
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        compose = true
    }


    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(project(":shared"))
    implementation("androidx.compose.ui:ui:1.2.1")
    implementation("androidx.compose.ui:ui-tooling:1.2.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.2.1")
    implementation("androidx.compose.foundation:foundation:1.2.1")
    implementation("androidx.compose.material3:material3:1.0.0-rc01")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.activity:activity-compose:1.6.0")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")
    implementation("androidx.compose.runtime:runtime-livedata:1.2.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    compileOnly("io.realm.kotlin:library-sync:1.4.0")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    implementation ("com.afollestad.material-dialogs:core:3.3.0")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("org.tensorflow:tensorflow-lite:2.4.0")
}