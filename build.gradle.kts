import java.util.Properties
import java.io.File

buildscript {
    repositories {
        jcenter()
        mavenCentral()
        google()
    }
    dependencies {
        classpath("com.jfrog.bintray.gradle:gradle-bintray-plugin:${co.early.fore.Shared.Versions.gradle_bintray_plugin}")
        classpath("com.android.tools.build:gradle:${co.early.fore.Shared.Versions.android_gradle_plugin}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${co.early.fore.Shared.Versions.kotlin_version}")
    }
}

plugins {
    id("idea")
}


//read in secrets file
val secrets = readProperties(File(project.rootDir, "secrets.properties"))

val AWS_ACCESS_KEY = System.getenv("AWS_ACCESS_KEY") ?: secrets.getProperty("AWS_ACCESS_KEY")
val AWS_SECRET_KEY = System.getenv("AWS_SECRET_KEY") ?: secrets.getProperty("AWS_SECRET_KEY")


allprojects {
    repositories {
        jcenter()
        mavenCentral()
        google()
        mavenLocal()

//  used to test rc builds - you won't have my key, but you can do something similar with your own amazon bucket
//        maven {
//            url "s3://com.ixpocket.repo.s3.eu-west-2.amazonaws.com/snapshots"
//            //url "s3://com.ixpocket.repo.s3.eu-west-2.amazonaws.com/releases"
//            credentials(AwsCredentials) {
//                accessKey AWS_ACCESS_KEY
//                secretKey AWS_SECRET_KEY
//            }
//        }

    }
}

ext.apply {

    // TODO remove these once we migrate bintraypublish.gradle to kts

    //LIB_VERSION_NAME="0.9.25-SNAPSHOT"
    set("LIB_VERSION_NAME", "1.3.2")
    set("LIB_VERSION_CODE", 46)

    set("REPO", "fore")
    set("LIB_GROUP", "co.early.fore")
    set("PROJ_NAME", "fore")
    set("LIB_DEVELOPER_ID", "erdo")
    set("LIB_DEVELOPER_NAME", "E Donovan")
    set("LIB_DEVELOPER_EMAIL", "eric@early.co")
    set("POM_PACKAGING", "aar")
    set("POM_URL", "https://erdo.github.io/android-fore/")
    set("POM_SCM_URL", "https://github.com/erdo/android-fore")
    set("POM_SCM_CONNECTION", "scm:git@github.com:erdo/android-fore.git")
    set("POM_SCM_DEV_CONNECTION", "scm:git@github.com:erdo/android-fore.git")

    set("LICENCE_SHORT_NAME", "Apache-2.0")
    set("LICENCE_NAME", "The Apache Software License, Version 2.0")
    set("LICENCE_URL", "http://www.apache.org/licenses/LICENSE-2.0.txt")

    set("BINTRAY_USER", System.getenv("BINTRAY_USER") ?: secrets.getProperty("BINTRAY_USER"))
    set("BINTRAY_API_KEY", System.getenv("BINTRAY_API_KEY") ?: secrets.getProperty("BINTRAY_API_KEY"))
}

tasks.create<Delete>("clean") {
    delete(rootProject.buildDir)
}

fun readProperties(propertiesFile: File): Properties {
    return Properties().apply {
        try {
            propertiesFile.inputStream().use { fis ->
                load(fis)
            }
        } catch (exception: Exception) {
            println("WARNING $propertiesFile not found! \n")
            println("exception: $exception \n")
        }
    }
}
