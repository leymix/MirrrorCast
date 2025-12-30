import java.util.Properties
import java.io.FileInputStream
import java.io.File

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
}

// Read local.properties file
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    FileInputStream(localPropertiesFile).use { localProperties.load(it) }
}

fun getLocalProperty(key: String): String? {
    return localProperties.getProperty(key)
}

android {
    namespace = "com.mirrorcast"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.mirrorcast"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            // Önce environment variable'dan, sonra local.properties'den, sonra project property'den oku
            val keystoreFile = System.getenv("KEYSTORE_FILE") 
                ?: getLocalProperty("KEYSTORE_FILE")
                ?: project.findProperty("KEYSTORE_FILE") as String?
            
            val keystorePassword = System.getenv("KEYSTORE_PASSWORD")
                ?: getLocalProperty("KEYSTORE_PASSWORD")
                ?: project.findProperty("KEYSTORE_PASSWORD") as String?
            
            val keyAliasValue = System.getenv("KEY_ALIAS")
                ?: getLocalProperty("KEY_ALIAS")
                ?: project.findProperty("KEY_ALIAS") as String?
                ?: "mirrorcast"
            
            val keyPasswordValue = System.getenv("KEY_PASSWORD")
                ?: getLocalProperty("KEY_PASSWORD")
                ?: project.findProperty("KEY_PASSWORD") as String?
            
            if (keystoreFile != null && keystorePassword != null && keyPasswordValue != null) {
                val keystorePath = if (File(keystoreFile).isAbsolute) {
                    file(keystoreFile)
                } else {
                    rootProject.file(keystoreFile)
                }
                
                if (keystorePath.exists()) {
                    storeFile = keystorePath
                    storePassword = keystorePassword
                    keyAlias = keyAliasValue
                    keyPassword = keyPasswordValue
                } else {
                    println("WARNING: Keystore file not found at: ${keystorePath.absolutePath}")
                }
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    
    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            resValue("string", "app_name", "CastLink Dev")
            buildConfigField("String", "ENVIRONMENT", "\"dev\"")
            buildConfigField("String", "API_BASE_URL", "\"https://api-dev.mirrorcast.com\"")
        }
        
        create("stg") {
            dimension = "environment"
            applicationIdSuffix = ".stg"
            versionNameSuffix = "-stg"
            resValue("string", "app_name", "CastLink Staging")
            buildConfigField("String", "ENVIRONMENT", "\"staging\"")
            buildConfigField("String", "API_BASE_URL", "\"https://api-staging.mirrorcast.com\"")
        }
        
        create("prod") {
            dimension = "environment"
            resValue("string", "app_name", "CastLink")
            buildConfigField("String", "ENVIRONMENT", "\"production\"")
            buildConfigField("String", "API_BASE_URL", "\"https://api.mirrorcast.com\"")
            // GitHub repository bilgisi - kendi repository'nizi buraya girin
            // Örnek: "owner/repo" formatında
            buildConfigField("String", "GITHUB_REPO", "\"YOUR_GITHUB_USERNAME/MirrorCast\"")
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    
    // Icons
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Network (for update checking)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.02.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
