import co.early.fore.Shared

plugins {
    id("fore-android-plugin")
    kotlin("android")
}

android {
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Shared.Versions.kotlinx_coroutines_android}")
    implementation("androidx.appcompat:appcompat:${Shared.Versions.appcompat}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${Shared.Versions.androidx_lifecycle_common}")
    implementation("androidx.recyclerview:recyclerview:${Shared.Versions.recyclerview}")
}
