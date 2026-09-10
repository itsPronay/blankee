import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.google.gms.google.services) apply false
    alias(libs.plugins.google.firebase.crashlytics) apply false
}

val keystoreProperties =
    Properties().apply {
        val keystorePropertiesFile = rootProject.file("keystore.properties")
        if (keystorePropertiesFile.exists()) {
            load(FileInputStream(keystorePropertiesFile))
        }
    }

fun releaseKeystorePath(): String? =
    keystoreProperties.getProperty("storeFile") ?: System.getenv("ANDROID_KEYSTORE_PATH")

fun releaseSigningCredential(propertyName: String, envName: String): String? =
    keystoreProperties.getProperty(propertyName) ?: System.getenv(envName)

android {
    namespace = "com.pronaycoding.blankee"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.pronaycoding.blankee"
        minSdk = 24
        targetSdk = 36
        versionCode = 8
        versionName = "1.1.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("gplay") {
            dimension = "distribution"
            // Keeps the applicationId that's already registered on the Play Store.
        }
        create("fdroid") {
            dimension = "distribution"
            // Distinct applicationId so this differently-signed build doesn't collide
            // with the Play Store package's registered signing key on-device.
            applicationIdSuffix = ".fdroid"
        }
    }


    signingConfigs {
        create("release") {
            val storeFilePath = releaseKeystorePath()
            if (!storeFilePath.isNullOrBlank()) {
                storeFile = rootProject.file(storeFilePath)
                storePassword = releaseSigningCredential("storePassword", "ANDROID_KEYSTORE_PASSWORD")
                keyAlias = releaseSigningCredential("keyAlias", "ANDROID_KEY_ALIAS")
                keyPassword = releaseSigningCredential("keyPassword", "ANDROID_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
        }
        release {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            val releaseSigning = signingConfigs.getByName("release")
            signingConfig =
                if (releaseSigning.storeFile?.exists() == true) {
                    releaseSigning
                } else {
                    signingConfigs.getByName("debug")
                }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // Lint is currently hanging during `lintAnalyzeDebug` / `lintVitalAnalyzeRelease`.
    // Keep CI/builds unblocked while we investigate root cause.
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

// Applied unconditionally so IDE sync and mixed invocations (e.g.
// `assembleGplayRelease assembleFdroidRelease` in the same command) work.
// Both plugins generate a task per variant, including fdroid ones, which
// would otherwise fail looking for a google-services.json that only the
// gplay flavor ships. Disable those tasks for fdroid instead.
apply(plugin = "com.google.gms.google-services")
apply(plugin = "com.google.firebase.crashlytics")

tasks.configureEach {
    if (name.contains("Fdroid") && (name.contains("GoogleServices") || name.contains("Crashlytics"))) {
        enabled = false
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.media:media:1.7.0")
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose.android)

    // Crashlytics only compiled into the gplay flavor
    "gplayImplementation"(libs.firebase.crashlytics)

//    implementation(libs.androidx.material3.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // navigation
    implementation(libs.androidx.navigation.compose)

    // Exoplayer
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.dash)
    implementation(libs.androidx.media3.ui)

    // await
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // extra icons
    implementation(libs.androidx.compose.material.icons.extended)

    // Room Database (KSP avoids Kotlin 2.x + kapt processor issues)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)

    // Play Billing is a Google Play service — gplay only.
    // F-Droid won't accept it as-is; provide a no-op or alternative
    // purchase flow for the fdroid flavor if you need one.
    "gplayImplementation"(libs.billing)
    "gplayImplementation"(libs.billing.ktx)
}
