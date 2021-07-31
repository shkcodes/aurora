object Versions {
    const val compileSdkVersion = 30
    const val minSdkVersion = 23
    const val targetSdkVersion = 30

    const val gradle = "4.2.1"
    const val kotlin = "1.4.32"

    const val coreKtx = "1.3.2"
    const val appCompat = "1.2.0"

    const val navigation = "2.3.5"
    const val material = "1.4.0-rc01"
    const val lifecycle = "2.4.0-alpha01"

    const val viewModel = "2.3.0"

    const val hilt = "2.35"
    const val hiltJetpack = "1.0.0-alpha03"

    const val coroutines = "1.4.3"

    const val reclaim = "2.1.1"
    const val coil = "1.3.0"
    const val swipeRefresh = "1.1.0"
    const val viewPager = "1.0.0"
    const val constraintLayout = "2.1.0-beta01"

    const val detekt = "1.16.0"

    const val junit = "4.13.2"
    const val mockk = "1.11.0"
    const val turbine = "0.5.2"

    const val retrofit = "2.9.0"
    const val okhttp = "4.9.0"
    const val moshi = "1.12.0"

    const val desugarLib = "1.0.9"
    const val room = "2.3.0"

    const val jsoup = "1.13.1"

    const val twitter4j = "4.0.7"

    const val timber = "4.7.1"

    const val gestureView = "2.7.1"
    const val exoplayer = "2.13.3"
}

object Dependencies {
    const val gradlePlugin = "com.android.tools.build:gradle:${Versions.gradle}"
    const val kotlinPlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlin}"

    const val coreKtx = "androidx.core:core-ktx:${Versions.coreKtx}"
    const val appCompat = "androidx.appcompat:appcompat:${Versions.appCompat}"

    const val navigation = "androidx.navigation:navigation-fragment-ktx:${Versions.navigation}"
    const val navigationUi = "androidx.navigation:navigation-ui-ktx:${Versions.navigation}"
    const val navigationSafeArgs =
        "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.navigation}"
    const val material = "com.google.android.material:material:${Versions.material}"
    const val lifecycle = "androidx.lifecycle:lifecycle-runtime-ktx:${Versions.lifecycle}"
    const val lifecycleCompiler = "androidx.lifecycle:lifecycle-compiler:${Versions.lifecycle}"

    const val viewModel = "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.viewModel}"

    const val hiltGradlePlugin = "com.google.dagger:hilt-android-gradle-plugin:${Versions.hilt}"
    const val hilt = "com.google.dagger:hilt-android:${Versions.hilt}"
    const val hiltCompiler = "com.google.dagger:hilt-compiler:${Versions.hilt}"
    const val hiltCompilerJetpack = "androidx.hilt:hilt-compiler:${Versions.hiltJetpack}"
    const val hiltViewModel = "androidx.hilt:hilt-lifecycle-viewmodel:${Versions.hiltJetpack}"

    const val coroutinesCore =
        "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}"
    const val coroutinesAndroid =
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}"

    const val reclaim = "com.github.fueled:reclaim:${Versions.reclaim}"
    const val coil = "io.coil-kt:coil:${Versions.coil}"
    const val swipeRefresh = "androidx.swiperefreshlayout:swiperefreshlayout:${Versions.swipeRefresh}"
    const val viewPager = "androidx.viewpager2:viewpager2:${Versions.viewPager}"
    const val constraintLayout = "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}"

    const val detektGradlePlugin =
        "io.gitlab.arturbosch.detekt:detekt-gradle-plugin:${Versions.detekt}"
    const val detektFormatter = "io.gitlab.arturbosch.detekt:detekt-formatting:${Versions.detekt}"

    const val retrofit = "com.squareup.retrofit2:retrofit:${Versions.retrofit}"
    const val okhttp = "com.squareup.okhttp3:logging-interceptor:${Versions.okhttp}"
    const val moshi = "com.squareup.moshi:moshi-kotlin:${Versions.moshi}"
    const val moshiCompiler = "com.squareup.moshi:moshi-kotlin-codegen:${Versions.moshi}"
    const val scalarsConverter = "com.squareup.retrofit2:converter-scalars:${Versions.retrofit}"
    const val moshiConverter = "com.squareup.retrofit2:converter-moshi:${Versions.retrofit}"

    const val desugarLib = "com.android.tools:desugar_jdk_libs:${Versions.desugarLib}"

    const val room = "androidx.room:room-runtime:${Versions.room}"
    const val roomCompiler = "androidx.room:room-compiler:${Versions.room}"
    const val roomKtx = "androidx.room:room-ktx:${Versions.room}"

    const val jsoup = "org.jsoup:jsoup:${Versions.jsoup}"

    const val twitter4j = "org.twitter4j:twitter4j-core:${Versions.twitter4j}"

    const val timber = "com.jakewharton.timber:timber:${Versions.timber}"

    const val gestureView = "com.alexvasilkov:gesture-views:${Versions.gestureView}"
    const val exoplayer = "com.google.android.exoplayer:exoplayer:${Versions.exoplayer}"

}

object TestDependencies {
    const val junit = "junit:junit:${Versions.junit}"
    const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.coroutines}"
    const val mockk = "io.mockk:mockk:${Versions.mockk}"
    const val mockkJvmAgent = "io.mockk:mockk-agent-jvm:${Versions.mockk}"
    const val turbine = "app.cash.turbine:turbine:${Versions.turbine}"
}
