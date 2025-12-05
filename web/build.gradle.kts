plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    kotlin("plugin.compose")
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
            webpackTask {
                mainOutputFileName = "fks-web-kmp.js"
                sourceMaps = true
            }
            distribution {
                outputDirectory.set(file("$projectDir/dist"))
            }
        }
        binaries.executable()
    }
    
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":shared"))
                // Compose dependencies are already in shared/commonMain
                // Additional web-specific dependencies can be added here if needed
            }
            resources.srcDirs("src/jsMain/resources")
        }
    }
}

// Handle duplicate resources (like index.html)
tasks.named<Copy>("jsProcessResources") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
