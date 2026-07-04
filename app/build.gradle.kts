plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.carlosarancibia.playfit"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.carlosarancibia.playfit.android"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "0.1"

        buildConfigField("String", "SUPABASE_URL", "\"https://vhhnwjuwqbspvllvppnn.supabase.co\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"sb_publishable_In29cjV8e5T_obAhkwTZkw_ndu8R_ch\"")
        buildConfigField("String", "API_BASE_URL", "\"https://playfit-gold.vercel.app\"")
        buildConfigField("String", "AUTH_REDIRECT_URL", "\"playfit://auth-callback\"")
        buildConfigField("String", "BUILD_ENVIRONMENT", "\"production\"")
    }

    buildTypes {
        getByName("debug") {
            buildConfigField("String", "SUPABASE_URL", "\"http://10.0.2.2:54321\"")
            buildConfigField("String", "SUPABASE_ANON_KEY", "\"sb_publishable_ACJWlzQHlZjBrEguHvfOxg_3BJgxAaH\"")
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:3000\"")
            buildConfigField("String", "BUILD_ENVIRONMENT", "\"development\"")
        }
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Existing
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)

    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // DataStore
    implementation(libs.datastore.preferences)

    // WorkManager
    implementation(libs.work.runtime.ktx)

    // Ktor
    implementation(platform(libs.ktor.bom))
    implementation(libs.ktor.client.okhttp)

    // Supabase
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.auth)
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.realtime)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.turbine)
}
