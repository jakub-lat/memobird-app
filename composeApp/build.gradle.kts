import org.jetbrains.compose.ExperimentalComposeLibrary

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    kotlin("plugin.serialization") version "1.9.21"
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
            implementation("com.github.bumptech.glide:compose:1.0.0-beta01")
            implementation("com.google.accompanist:accompanist-permissions:0.23.1")
            implementation("io.ktor:ktor-client-okhttp:2.3.7")
        }
        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:2.3.7")
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
            implementation("com.github.kittinunf.fuel:fuel:3.0.0-alpha1")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
            implementation("com.darkrockstudios:mpfilepicker:3.1.0")
            implementation("br.com.devsrsouza.compose.icons:tabler-icons:1.1.0")
            implementation("com.mohamedrejeb.richeditor:richeditor-compose:1.0.0-rc01")
            implementation("io.github.g0dkar:qrcode-kotlin:4.1.1")

            val voyagerVersion = "1.0.0"
            implementation("cafe.adriel.voyager:voyager-navigator:$voyagerVersion")
            implementation("cafe.adriel.voyager:voyager-transitions:$voyagerVersion")

//            implementation("io.coil-kt.coil3:coil:3.0.0-SNAPSHOT")
//            implementation("io.coil-kt.coil3:coil-network:3.0.0-SNAPSHOT")
            implementation("io.coil-kt.coil3:coil:3.0.0-alpha01")
            implementation("io.coil-kt.coil3:coil-compose:3.0.0-alpha01")
//            implementation("io.coil-kt.coil3:coil-network-ktor:3.0.0-alpha01")
        }
    }
}

android {
    namespace = "pl.jakubl.memobird"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "pl.jakubl.memobird"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildToolsVersion = "34.0.0"
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
}


compose.experimental {
    web.application {}
}