plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdk = 32

    defaultConfig {
        applicationId = "io.posidon.android.cintalauncher"
        minSdk = 26
        targetSdk = 32
        versionCode = 1
        versionName = "22.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.6.20")
    implementation("com.willowtreeapps:fuzzywuzzy-kotlin-jvm:0.9.0")
    implementation("com.github.bumptech.glide:glide:4.12.0")

    implementation("io.posidon:android.launcherUtils:30aa020c1a")
    implementation("io.posidon:android.libduckduckgo:22.0")
    implementation("io.posidon:android.rsslib:22.0")
    implementation("io.posidon:android.conveniencelib:22.0")

    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.palette:palette-ktx:1.0.0")

    implementation("com.google.android.material:material:1.5.0")

    testImplementation("junit:junit:4.13.2")
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}
