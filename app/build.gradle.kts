plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "com.example.capstonebangkitpawers"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.capstonebangkitpawers"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "WEB_CLIENT_ID", "\"885069307978-tg511r8r4bjrn5l7svdmnal9ar95h8jh.apps.googleusercontent.com\"")
        buildConfigField("String", "DATABASE_URL", "\"https://capstone-project-796f7-default-rtdb.asia-southeast1.firebasedatabase.app\"")

        resValue("string", "google_maps_api_key", project.hasProperty("google_maps_api_key").toString())
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

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        mlModelBinding = true
        buildConfig = true
    }
}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database.ktx)
    implementation(libs.firebase.storage.ktx)
    implementation(libs.play.services.maps)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation("androidx.datastore:datastore-preferences:1.0.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")
    implementation("androidx.activity:activity-ktx:1.7.2")

    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.2")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("androidx.recyclerview:recyclerview:1.3.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    implementation ("androidx.recyclerview:recyclerview:1.3.0")

    implementation("com.google.android.gms:play-services-location:21.3.0")

    implementation("androidx.credentials:credentials:1.5.0-beta01")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0-beta01")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    implementation ("androidx.camera:camera-camera2:1.1.0-beta02")
    implementation ("androidx.camera:camera-lifecycle:1.1.0-beta02")
    implementation ("androidx.camera:camera-view:1.1.0-beta02")

    implementation ("com.codesgood:justifiedtextview:1.1.0")
    implementation("org.tensorflow:tensorflow-lite:2.12.0")
    implementation("org.tensorflow:tensorflow-lite-support:0.4.3")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}