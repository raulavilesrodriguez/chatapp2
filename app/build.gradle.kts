import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hiltAndroid)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.packt.chat"
    compileSdk = 36

    signingConfigs {
        create("release") {
            // Lee las propiedades desde el archivo gradle.properties
            val storeFile = System.getenv("MYAPP_RELEASE_STORE_FILE") ?: project.property("MYAPP_RELEASE_STORE_FILE") as String?
            val storePassword = System.getenv("MYAPP_RELEASE_STORE_PASSWORD") ?: project.property("MYAPP_RELEASE_STORE_PASSWORD") as String?
            val keyAlias = System.getenv("MYAPP_RELEASE_KEY_ALIAS") ?: project.property("MYAPP_RELEASE_KEY_ALIAS") as String?
            val keyPassword = System.getenv("MYAPP_RELEASE_KEY_PASSWORD") ?: project.property("MYAPP_RELEASE_KEY_PASSWORD") as String?

            if (storeFile != null) {
                this.storeFile = file(storeFile)
                this.storePassword = storePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            }
        }
    }

    defaultConfig {
        applicationId = "com.packt.chat"
        minSdk = 29
        targetSdk = 36
        versionCode = 9
        versionName = "1.0.9"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true // para produccion
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

        }
    }

    firebaseCrashlytics {
        nativeSymbolUploadEnabled = false
        mappingFileUploadEnabled = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(project(":feature:settings"))
    implementation(project(":feature:create_chat"))
    implementation(project(":feature:conversations"))
    implementation(project(":feature:chat"))
    implementation(project(":core:ui"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    ksp(libs.hilt.compiler)
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.accompanist.permissions)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.appcheck.playintegrity)
    implementation(libs.firebase.appcheck.debug)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.coil)
    implementation(libs.android.image.cropper)
    implementation(libs.ucrop)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.material.icons.core)

}


tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        // Aquí defines tu target de bytecode JVM
        jvmTarget.set(JvmTarget.JVM_11)
        // Y si tenías flags extra:
        freeCompilerArgs.addAll(listOf(
            "-Xopt-in=kotlin.RequiresOptIn"
        ))
    }
}