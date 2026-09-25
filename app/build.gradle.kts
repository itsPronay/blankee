import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.compose.compiler)
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
        versionCode = 9
        versionName = "1.1.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
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
            val releaseSigning = signingConfigs.findByName("release")
            signingConfig =
                if (releaseSigning?.storeFile?.exists() == true) {
                    releaseSigning
                } else {
                    signingConfigs.getByName("debug")
                }
            // AGP 8.3+ embeds git revision + build path; disable for reproducible builds
            vcsInfo.include = false
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

// PNG crunching is non-deterministic across aapt2 versions.
// cruncherEnabled was removed from the public AGP 8.x DSL so we access it via BaseExtension.
@Suppress("DEPRECATION")
extensions.configure<com.android.build.gradle.BaseExtension> {
    aaptOptions.cruncherEnabled = false
}

// baseline.prof / baseline.profm are non-deterministic across CPU architectures.
// Use configureEach (lazy API) rather than whenTaskAdded for reliable task disabling.
tasks.configureEach {
    if (name.contains("ArtProfile")) {
        enabled = false
    }
}
// Belt-and-suspenders: scrub any stale compiled baseline profile intermediates
// before packageRelease runs, so they never make it into the APK even if
// ArtProfile tasks ran in a previous incremental (non-clean) build.
tasks.configureEach {
    if (name == "packageRelease") {
        doFirst {
            fileTree(project.layout.buildDirectory) {
                include("**/dexopt/**")
            }.forEach { it.delete() }
        }
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
}
