plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug{

            //main
            //buildConfigField("String", "BASE_URL", "\"https://log3r.up.railway.app\"");
            //dev
            buildConfigField("String", "BASE_URL", "\"https://log3r-dev.up.railway.app\"");
            //local
            //buildConfigField("String", "BASE_URL", "\"http://192.168.1.44:5000\"");


        }
        release {
            buildConfigField("String", "BASE_URL", "\"https://log3r.up.railway.app\"");

            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        android.buildFeatures.buildConfig = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.cardview)
    implementation("com.google.android.material:material:1.13.0-alpha01")
    implementation("org.opencv:opencv:4.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1") // Cambia la versión si es necesario
    implementation("androidx.core:core-ktx:1.10.1")
    implementation ("org.json:json:20210307") // Para trabajar con JSON
    implementation(libs.core.ktx)
    implementation(libs.androidx.junit.ktx)
    implementation(libs.androidx.runner)
    implementation(libs.androidx.leanback)
    implementation(libs.androidx.legacy.support.v4) // Cambia la versión si es necesario
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // OKhttp para peticiones y solicitudes
    implementation ("com.squareup.okhttp3:okhttp:4.9.2")
    // Kotlin Extensions for OkHttp
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.2")

    var camerax_version = "1.2.2"
    implementation ("androidx.camera:camera-core:${camerax_version}")
    implementation ("androidx.camera:camera-camera2:${camerax_version}")
    implementation ("androidx.camera:camera-lifecycle:${camerax_version}")
    implementation ("androidx.camera:camera-video:${camerax_version}")

    implementation ("androidx.camera:camera-view:${camerax_version}")
    implementation ("androidx.camera:camera-extensions:${camerax_version}")

    // Use this dependency to bundle the model with your app - ML KIT Google
    implementation ("com.google.mlkit:face-detection:16.1.6")
    implementation ("com.google.code.gson:gson:2.8.6")

    // tflite
    implementation ("org.tensorflow:tensorflow-lite:2.4.0")
    implementation ("org.tensorflow:tensorflow-lite-gpu:2.4.0")
    implementation ("org.tensorflow:tensorflow-lite-support:0.2.0")

    // json
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.google.mlkit:barcode-scanning:17.0.3")

    //gson
    implementation ("com.google.code.gson:gson:2.11.0")

    implementation ("androidx.core:core:1.6.0")


    //ML KIT Google
//    implementation ("com.google.android.gms:play-services-mlkit:17.0.0")

}