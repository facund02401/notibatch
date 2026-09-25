plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "app.notibatch"
    compileSdk = 37

    defaultConfig {
        applicationId = "app.notibatch"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.0.1-fase0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            // La firma se configura en Fase 1 con una clave fuera del repo (pendiente P10).
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    sourceSets {
        // Kotlin en src/*/kotlin (AGP 9 con Kotlin integrado).
        getByName("main").java.srcDir("src/main/kotlin")
        getByName("debug").java.srcDir("src/debug/kotlin")
        getByName("test").java.srcDir("src/test/kotlin")
    }
}

dependencies {
    testImplementation(libs.junit)
}
