plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.kapt)
  alias(libs.plugins.hilt)
  alias(libs.plugins.ktlint)
}

android {
  namespace = "com.ctonew.taskmanagement.core.datastore"
  compileSdk = 34

  defaultConfig {
    minSdk = 26
    consumerProguardFiles("consumer-rules.pro")
  }

  testOptions {
    unitTests {
      isIncludeAndroidResources = true
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlinOptions {
    jvmTarget = "17"
  }

  lint {
    abortOnError = true
    warningsAsErrors = true
  }
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.kotlinx.coroutines.android)

  implementation(libs.hilt.android)
  kapt(libs.hilt.compiler)

  testImplementation(libs.junit4)
  testImplementation(libs.truth)
  testImplementation(libs.robolectric)
  testImplementation(libs.androidx.test.core)
  testImplementation(libs.kotlinx.coroutines.test)
}

kapt {
  correctErrorTypes = true
}
