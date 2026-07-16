import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

fun requiredProperty(name: String): String =
    project.findProperty(name)?.toString()
        ?: localProperties.getProperty(name)
        ?: error("Missing required Gradle property: $name")

fun buildConfigString(value: String): String =
    "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""

val releaseKeystorePath = System.getenv("PLAYFIT_UPLOAD_STORE_FILE")
val releaseKeystorePassword = System.getenv("PLAYFIT_UPLOAD_STORE_PASSWORD")
val releaseKeyAlias = System.getenv("PLAYFIT_UPLOAD_KEY_ALIAS")
val releaseKeyPassword = System.getenv("PLAYFIT_UPLOAD_KEY_PASSWORD")
val hasReleaseSigning = listOf(
    releaseKeystorePath,
    releaseKeystorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
).all { !it.isNullOrBlank() }

android {
    namespace = "com.carlosarancibia.playfit"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.carlosarancibia.playfit.android"
        minSdk = 29
        targetSdk = 36
        versionCode = System.getenv("PLAYFIT_CI_BUILD_NUMBER")?.toIntOrNull() ?: 1
        versionName = "0.1"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SUPABASE_URL", buildConfigString(requiredProperty("PLAYFIT_SUPABASE_URL")))
        buildConfigField("String", "SUPABASE_ANON_KEY", buildConfigString(requiredProperty("PLAYFIT_SUPABASE_ANON_KEY")))
        buildConfigField("String", "API_BASE_URL", buildConfigString(requiredProperty("PLAYFIT_API_BASE_URL")))
        buildConfigField("String", "AUTH_REDIRECT_URL", "\"playfit://auth-callback\"")
        buildConfigField("String", "BUILD_ENVIRONMENT", "\"production\"")
    }

    buildTypes {
        getByName("debug") {
            buildConfigField("String", "SUPABASE_URL", buildConfigString(requiredProperty("PLAYFIT_DEBUG_SUPABASE_URL")))
            buildConfigField("String", "SUPABASE_ANON_KEY", buildConfigString(requiredProperty("PLAYFIT_DEBUG_SUPABASE_ANON_KEY")))
            buildConfigField("String", "API_BASE_URL", buildConfigString(requiredProperty("PLAYFIT_DEBUG_API_BASE_URL")))
            buildConfigField("String", "BUILD_ENVIRONMENT", "\"development\"")
        }
        getByName("release") {
            isMinifyEnabled = true
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.create("releaseUpload").apply {
                    storeFile = file(requireNotNull(releaseKeystorePath))
                    storePassword = releaseKeystorePassword
                    keyAlias = releaseKeyAlias
                    keyPassword = releaseKeyPassword
                }
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
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

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    // Existing
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material.icons.extended)
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
    ksp(libs.hilt.work.compiler)

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
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

ksp {
    arg("room.schemaLocation", "${projectDir}/schemas")
}
