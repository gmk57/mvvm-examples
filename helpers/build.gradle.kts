import co.early.fore.Shared

plugins {
    id("fore-android-plugin")
    kotlin("android")
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Shared.Versions.kotlinx_coroutines_android}")
    api("androidx.lifecycle:lifecycle-runtime-ktx:${Shared.Versions.androidx_lifecycle_common}")
}
