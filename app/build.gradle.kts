plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.beautysync_kotlin"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.beautysync_kotlin"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    //
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.storage.ktx)
    implementation(libs.firebase.messaging)
    implementation(libs.circleimageview)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.logging.interceptor)
    implementation(libs.mpandroidchart)
    implementation(libs.androidx.media3.exoplayer.v150)
    implementation(libs.androidx.media3.exoplayer.dash.v150)
    implementation(libs.androidx.media3.ui.v150)
    implementation(libs.compiler)
    implementation(libs.material)
    implementation(libs.glide)
    implementation(libs.imageslideshow)
    implementation(libs.stripe.android)
    implementation(libs.gson)
    implementation(libs.imagepicker)
    implementation(libs.primedatepicker)
    implementation(libs.primecalendar)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.activity)
    //
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}