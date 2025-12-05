// Root build configuration for fks-clients (Kotlin Multiplatform)
plugins {
    kotlin("multiplatform") version "2.0.20" apply false
    kotlin("plugin.serialization") version "2.0.20" apply false
    kotlin("plugin.compose") version "2.0.20" apply false
    id("org.jetbrains.compose") version "1.8.0" apply false
    id("com.android.application") version "8.2.0" apply false
    id("com.android.library") version "8.2.0" apply false
}

// Configure Node.js to use system installation (prevents repository conflicts)
allprojects {
    plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
        extensions.getByType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().apply {
            download = false
        }
    }
}
