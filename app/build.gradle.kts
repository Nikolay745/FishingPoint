
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "com.fishingpoints"
    compileSdk {
        version = release(37)
    }


    defaultConfig {
        applicationId = "com.fishingpoints"
        minSdk = 31

        targetSdk = 37
        versionCode = 1
        versionName = "Версия: 3.10.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Загрузка из local.properties
        val weatherKey: String = project.findProperty("WEATHER_API_KEY") as? String
            ?: System.getenv("WEATHER_API_KEY")
            ?: "YOUR_DEFAULT_KEY"

        buildConfigField("String", "WEATHER_API_KEY", "\"$weatherKey\"")
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

}


dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    //    # Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    //    # Location
    implementation(libs.play.services.location)
    // Geocoder для получения названия места по координатам
    implementation(libs.play.services.maps)

    //    # Hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.compose.foundation.layout)
    ksp(libs.hilt.compiler)

    //    # Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // # Accompanist
    implementation(libs.accompanist.permissions)
    implementation(libs.accompanist.navigation.animation)

    //    # Coil
    implementation(libs.coil.compose)

    // --- navigation ---

    implementation(libs.androidx.hilt.navigation.compose)

    // Для JSON сериализации (если нужно хранить сложные объекты)
    implementation(libs.gson)

    // Для работы с JSON
    implementation(libs.kotlinx.serialization.json)

    // Для ViewModel
    implementation(libs.lifecycle.viewmodel.compose)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Графики
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)

    // Карты
    implementation(libs.osmdroid.android)

    testImplementation(libs.junit)

    // Compose tests
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

// Debug
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

// Android tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)


}