import org.gradle.internal.impldep.bsh.commands.dir

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

kotlin {
    jvmToolchain(11)
}

android {
    namespace = "com.example.macc"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.macc"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


 }



    android {
        buildFeatures {
            buildConfig = true
        }

        splits {
            abi {
                reset()
                include ("armeabi-v7a") // , 'x86', 'x86_64', 'arm64-v8a'
            }
    }

    buildTypes {

        /**
         * By default, Android Studio configures the release build type to enable code
         * shrinking, using minifyEnabled, and specifies the default ProGuard rules file.
         */

        getByName("release") {
            isMinifyEnabled = true // Enables code shrinking for the release build type.
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(fileTree("libs") {
        include("*.jar")
    })
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    implementation("androidx.constraintlayout:constraintlayout-compose-android:1.1.0-alpha13")
    implementation("androidx.camera:camera-lifecycle:1.3.2")
    implementation("com.google.android.gms:play-services-fitness:21.1.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    implementation("com.quickbirdstudios:opencv-contrib:3.4.15")
    implementation("org.tensorflow:tensorflow-lite-support:0.1.0")
    implementation("org.tensorflow:tensorflow-lite-metadata:0.1.0")

    implementation("androidx.camera:camera-camera2:1.3.2")
    implementation("androidx.camera:camera-core:1.3.2")
    implementation("androidx.camera:camera-view:1.3.2")

      implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")
    implementation ("com.github.yukuku:ambilwarna:2.0.1")

    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")
    implementation("com.google.android.libraries.places:places:3.4.0")

    implementation (project(":opencv"))
}