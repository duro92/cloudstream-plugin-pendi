
buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
    dependencies {
        // WAJIB: classpath AGP + Kotlin Gradle Plugin
        classpath("com.android.tools.build:gradle:8.5.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.24")

        // Plugin Cloudstream via JitPack
        classpath("com.github.recloudstream:gradle:master-SNAPSHOT")
    }
}


import com.android.build.gradle.BaseExtension
import com.lagradost.cloudstream3.gradle.CloudstreamExtension

subprojects {
    apply(plugin = "com.android.library")
    apply(plugin = "kotlin-android")
    apply(plugin = "com.lagradost.cloudstream3.gradle")

    extensions.configure(CloudstreamExtension::class.java) {
        setRepo(System.getenv("GITHUB_REPOSITORY") ?: "user/repo")
    }

    extensions.configure<com.android.build.gradle.LibraryExtension>("android") {
        compileSdk = 34
        defaultConfig {
            minSdk = 21
            targetSdk = 33
            consumerProguardFiles("consumer-rules.pro")
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = freeCompilerArgs + listOf(
                "-Xno-call-assertions",
                "-Xno-param-assertions",
                "-Xno-receiver-assertions"
            )
        }
    }

    dependencies {
        val apk by configurations
        val implementation by configurations

        apk("com.lagradost:cloudstream3:pre-release")
        implementation(kotlin("stdlib"))
        implementation("org.jsoup:jsoup:1.16.2")
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
