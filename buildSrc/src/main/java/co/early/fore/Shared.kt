package co.early.fore

import org.gradle.api.JavaVersion
import java.io.File
import java.util.*

object Shared {

    object Android {
        const val minSdk = 21  // 21+ enables multidex, preventing "Cannot fit requested classes in a single dex file" error
        const val compileSdk = 31
        const val targetSdk = 31
        val javaVersion = JavaVersion.VERSION_1_8
    }

    object Versions {
        // fore core package dependencies
        const val android_gradle_plugin = "7.1.3"
        const val kotlin_version = "1.6.20"
        const val kotlinx_coroutines_core = "1.6.1"
        // fore optional package dependencies
        const val kotlinx_coroutines_android = "1.6.1"
        const val androidx_lifecycle_common = "2.4.1"
        const val recyclerview = "1.2.1"
        const val apollo = "2.5.4"
        const val apollo3 = "3.2.2"
        const val retrofit = "2.9.0"
        const val okhttp = "4.9.3"
        const val compose = "1.1.1"
        // example app and test dependencies
        const val android_core = "1.7.0"
        const val annotation = "1.0.0"
        const val material = "1.5.0"
        const val appcompat = "1.4.1"
        const val coil = "1.4.0"
        const val androidxtest = "1.4.0"
        const val androidxjunit = "1.1.3"
        const val room_runtime = "2.4.2"
        const val room_compiler = "2.4.2"
        const val room_testing = "2.4.2"
        // 3.5.0-alpha03 is broken, see https://issuetracker.google.com/issues/204506297
        // 3.5.0-alpha06 is broken, see https://github.com/android/android-test/issues/1351
        const val espresso_core = "3.5.0-alpha05"
        const val butterknife = "10.2.0"
        const val mockito_core = "2.23.0"
        const val mockk = "1.11.0"
        const val junit = "4.12"
        const val hamcrest_library = "1.3"
        const val dexmaker_mockito = "2.28.1"
        const val robolectric = "4.4"
        const val gson = "2.8.5"
        const val constraintlayout = "2.1.3"
        const val ktor_client = "1.5.2"
        const val converter_gson = "2.9.0"
    }

    object BuildTypes {
        const val DEBUG = "debug"
        const val RELEASE = "release"
        const val DEFAULT = DEBUG
    }

    object Publish {
        const val LIB_VERSION_NAME = "1.5.9" //"x.x.x-SNAPSHOT"
        const val LIB_VERSION_CODE = 76
        const val LIB_GROUP = "co.early.fore"
        const val PROJ_NAME = "fore"
        const val LIB_DEVELOPER_ID = "erdo"
        const val LIB_DEVELOPER_NAME = "E Donovan"
        const val LIB_DEVELOPER_EMAIL = "eric@early.co"
        const val POM_URL = "https://erdo.github.io/android-fore/"
        const val POM_SCM_URL = "https://github.com/erdo/android-fore"
        const val POM_SCM_CONNECTION = "scm:git@github.com:erdo/android-fore.git"
        const val LICENCE_SHORT_NAME = "Apache-2.0"
        const val LICENCE_NAME = "The Apache Software License, Version 2.0"
        const val LICENCE_URL = "http://www.apache.org/licenses/LICENSE-2.0.txt"

        const val published_fore_version_for_examples = "1.5.4"
        const val use_published_version = false
    }

    object Secrets {

        private val secrets = readProperties(File("../secrets/secrets.properties"))

        val MAVEN_USER = (System.getenv("MAVEN_USER") ?: secrets.getProperty("MAVEN_USER")) ?: "MISSING"
        val MAVEN_PASSWORD = (System.getenv("MAVEN_PASSWORD") ?: secrets.getProperty("MAVEN_PASSWORD")) ?: "MISSING"
        val SONATYPE_STAGING_PROFILE_ID = (System.getenv("SONATYPE_STAGING_PROFILE_ID") ?: secrets.getProperty("SONATYPE_STAGING_PROFILE_ID")) ?: "MISSING"
        val SIGNING_KEY_ID = (System.getenv("SIGNING_KEY_ID") ?: secrets.getProperty("SIGNING_KEY_ID")) ?: "MISSING"
        val SIGNING_PASSWORD = (System.getenv("SIGNING_PASSWORD") ?: secrets.getProperty("SIGNING_PASSWORD")) ?: "MISSING"
        val SIGNING_KEY_RING_FILE = (System.getenv("SIGNING_KEY_RING_FILE") ?: secrets.getProperty("SIGNING_KEY_RING_FILE")) ?: "MISSING"
    }
}

fun readProperties(propertiesFile: File): Properties {
    return Properties().apply {
        try {
            propertiesFile.inputStream().use { fis ->
                load(fis)
            }
            println("[SECRETS LOADED]")
        } catch (exception: Exception) {
            println("WARNING $propertiesFile not found! \n")
            println("exception: $exception \n")
        }
    }
}
