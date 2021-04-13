object Versions {
    const val compileSdkVersion = 30
    const val minSdkVersion = 23
    const val targetSdkVersion = 30

    const val gradle = "7.0.0-alpha12"
    const val kotlin = "1.4.31"

    const val coreKtx = "1.3.2"
    const val appCompat = "1.2.0"

    const val compose = "1.0.0-beta03"
    const val composeActivity = "1.3.0-alpha05"
    const val composeViewModel = "1.0.0-alpha03"
    const val composeNavigation = "1.0.0-alpha09"

    const val viewModel = "2.3.0"

    const val hilt = "2.33-beta"
    const val hiltJetpack = "1.0.0-alpha03"
    const val hiltComposeNavigation = "1.0.0-alpha01"

    const val coroutines = "1.4.3"

    const val detekt = "1.16.0"

    const val junit = "4.13.2"
    const val mockk = "1.11.0"
    const val turbine = "0.4.1"

    const val retrofit = "2.9.0"
    const val okhttp = "4.9.0"
    const val moshi = "1.11.0"
}

object Dependencies {
    const val gradlePlugin = "com.android.tools.build:gradle:${Versions.gradle}"
    const val kotlinPlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"

    const val coreKtx = "androidx.core:core-ktx:${Versions.coreKtx}"
    const val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"

    const val composeUi = "androidx.compose.ui:ui:${Versions.compose}"
    const val composeTooling = "androidx.compose.ui:ui-tooling:${Versions.compose}"
    const val composeFoundation = "androidx.compose.foundation:foundation:${Versions.compose}"
    const val composeMaterial = "androidx.compose.material:material:${Versions.compose}"
    const val composeIcons = "androidx.compose.material:material-icons-core:${Versions.compose}"
    const val composeActivity =
        "androidx.activity:activity-compose:${Versions.composeActivity}"
    const val composeViewModel =
        "androidx.lifecycle:lifecycle-viewmodel-compose:${Versions.composeViewModel}"
    const val composeNavigation =
        "androidx.navigation:navigation-compose:${Versions.composeNavigation}"

    const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.viewModel}"

    const val hiltGradlePlugin = "com.google.dagger:hilt-android-gradle-plugin:${Versions.hilt}"
    const val hilt = "com.google.dagger:hilt-android:${Versions.hilt}"
    const val hiltCompiler = "com.google.dagger:hilt-compiler:${Versions.hilt}"
    const val hiltCompilerJetpack = "androidx.hilt:hilt-compiler:${Versions.hiltJetpack}"
    const val hiltViewModel = "androidx.hilt:hilt-lifecycle-viewmodel:${Versions.hiltJetpack}"
    const val hiltComposeNavigation =
        "androidx.hilt:hilt-navigation-compose:${Versions.hiltComposeNavigation}"

    const val coroutinesCore =
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    const val coroutinesAndroid =
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"

    const val detektGradlePlugin =
        "io.gitlab.arturbosch.detekt:detekt-gradle-plugin:${Versions.detekt}"
    const val detektFormatter = "io.gitlab.arturbosch.detekt:detekt-formatting:${Versions.detekt}"

    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val okhttp = "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp}"
    const val moshi = "com.squareup.moshi:moshi-kotlin:${Versions.moshi}"
    const val scalarsConverter = "com.squareup.retrofit2:converter-scalars:${Versions.retrofit}"
    const val moshiConverter = "com.squareup.retrofit2:converter-moshi:${Versions.retrofit}"

}

object TestDependencies {
    const val junit = "junit:junit:${Versions.junit}"
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}"
    const val mockk = "io.mockk:mockk:${Versions.mockk}"
    const val turbine = "app.cash.turbine:turbine:${Versions.turbine}"
}
