plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
}

kotlin {
    // Android target
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    
    // Desktop JVM target
    jvm("desktop") {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }
    
    // iOS targets (for future)
    // iosX64()
    // iosArm64()
    // iosSimulatorArm64()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                // Compose Multiplatform
                implementation(compose.ui)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.runtime)
                
                // Ktor Client
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.websockets)
                implementation(libs.ktor.client.logging)
                
                // Serialization
                implementation(libs.kotlinx.serialization.json)
                
                // Coroutines
                implementation(libs.kotlinx.coroutines.core)
                
                // Dependency Injection
                implementation(libs.koin.core)
                
                // Navigation
                implementation(libs.voyager.navigator)
                implementation(libs.voyager.transitions)
                implementation(libs.voyager.koin)
            }
        }
        
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        
        val androidMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.koin.android)
            }
        }
        
        val desktopMain by getting {
            dependencies {
                implementation(libs.ktor.client.cio)
                implementation(libs.compose.desktop)
            }
        }
        
        // iOS when ready
        // val iosMain by creating {
        //     dependsOn(commonMain)
        //     dependencies {
        //         implementation(libs.ktor.client.darwin)
        //     }
        // }
    }
}

android {
    namespace = "xyz.fkstrading.clients.shared"
    compileSdk = libs.versions.android.compile.sdk.get().toInt()
    
    defaultConfig {
        minSdk = libs.versions.android.min.sdk.get().toInt()
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
