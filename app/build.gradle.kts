plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    compileSdk = 30
    buildToolsVersion = "30.0.3"

    defaultConfig {
        applicationId = "io.posidon.android.cintalauncher"
        minSdk = 26
        targetSdk = 30
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        this["release"].apply {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("com.github.bumptech.glide:glide:4.12.0")
    implementation("io.posidon:android.launcherUtils:master-SNAPSHOT")
    implementation("io.posidon:android.loader:master-SNAPSHOT")
    implementation("io.posidon:android.convenienceLib:master-SNAPSHOT")
    implementation("com.willowtreeapps:fuzzywuzzy-kotlin-jvm:0.9.0")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.21")
    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("com.google.android.material:material:1.4.0")
    testImplementation("junit:junit:4.13.2")
    //androidTestImplementation("androidx.test.ext:junit:1.1.2")
    //androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}
