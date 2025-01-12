import co.early.fore.Shared
import co.early.fore.Shared.BuildTypes

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
}


val appId = "foo.bar.example.foreadapterskt"

fun getTestBuildType(): String {
    return project.properties["testBuildType"] as String? ?: BuildTypes.DEFAULT
}

println("[$appId testBuildType:${getTestBuildType()}]")


android {

    compileOptions {
        sourceCompatibility = Shared.Android.javaVersion
        targetCompatibility = Shared.Android.javaVersion
    }

    compileSdk = Shared.Android.compileSdk

    defaultConfig {
        applicationId = appId
        minSdk = Shared.Android.minSdk
        targetSdk = Shared.Android.targetSdk
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testBuildType = getTestBuildType()
    }
    signingConfigs {
        create(BuildTypes.RELEASE) {
            // keytool -genkey -v -keystore debug.fake_keystore -storetype PKCS12 -alias android -storepass android -keypass android -keyalg RSA -keysize 2048 -validity 20000 -dname "cn=Unknown, ou=Unknown, o=Unknown, c=Unknown"
            storeFile = file("../keystore/debug.fake_keystore")
            storePassword = "android"
            keyAlias = "android"
            keyPassword = "android"
        }
    }
    buildTypes {
        getByName(BuildTypes.DEBUG) {
            isMinifyEnabled = false
        }
        getByName(BuildTypes.RELEASE) {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "../proguard-example-app.pro")
            signingConfig = signingConfigs.getByName(BuildTypes.RELEASE)
        }
    }
    buildFeatures {
        viewBinding = true
    }
    lint {
        abortOnError = true
        lintConfig = File(project.rootDir, "lint-example-apps.xml")
    }
}

repositories {
    mavenCentral()
    google()
}

dependencies {

    if (Shared.Publish.use_published_version) {
        implementation("co.early.fore:fore-kt-android-adapters:${Shared.Publish.published_fore_version_for_examples}")
    } else {
        implementation(project(":fore-kt-android-adapters"))
    }

    implementation(project(":helpers"))

    implementation("androidx.appcompat:appcompat:${Shared.Versions.appcompat}")
    implementation("androidx.recyclerview:recyclerview:${Shared.Versions.recyclerview}")
    implementation("androidx.constraintlayout:constraintlayout:${Shared.Versions.constraintlayout}")
    implementation("androidx.core:core-ktx:${Shared.Versions.android_core}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${Shared.Versions.androidx_lifecycle_common}")

    testImplementation("junit:junit:${Shared.Versions.junit}")
    testImplementation("io.mockk:mockk:${Shared.Versions.mockk}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Shared.Versions.kotlinx_coroutines_core}")

    androidTestImplementation("io.mockk:mockk-android:${Shared.Versions.mockk}")
    androidTestImplementation("androidx.test:core:${Shared.Versions.androidxtest}")
    androidTestImplementation("androidx.test:runner:${Shared.Versions.androidxtest}")
    androidTestImplementation("androidx.test:rules:${Shared.Versions.androidxtest}")
    androidTestImplementation("androidx.test.ext:junit-ktx:${Shared.Versions.androidxjunit}")
    androidTestImplementation("androidx.annotation:annotation:${Shared.Versions.annotation}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${Shared.Versions.espresso_core}") {
        exclude(group = "com.android.support", module = "support-annotations")
    }
}
