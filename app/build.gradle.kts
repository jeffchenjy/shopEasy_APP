plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.example.shopping_app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.shopping_app"
        minSdk = 26
        targetSdk = 33
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.swiperefreshlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation ("com.google.android.material:material:1.11.0")
    implementation ("androidx.recyclerview:recyclerview-selection:1.1.0")
    implementation ("androidx.recyclerview:recyclerview:1.3.2")
    implementation ("pl.droidsonroids.gif:android-gif-drawable:1.2.22")
    implementation ("pub.devrel:easypermissions:3.0.0")

    // 將ImageURL設定給ImageView顯示
    implementation ("com.github.bumptech.glide:glide:4.16.0")

    implementation ("com.squareup.okhttp3:okhttp:4.10.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")

    //Animation Button
    //implementation ("com.airbnb.android:lottie:6.4.0")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    //Security Key Save Data
    implementation ("androidx.security:security-crypto:1.1.0-alpha03")
}