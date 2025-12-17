plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.kapt)
  alias(libs.plugins.hilt)
  alias(libs.plugins.ktlint)
}

android {
  namespace = "com.ctonew.taskmanagement.core.network"
  compileSdk = 34

  defaultConfig {
    minSdk = 26
    consumerProguardFiles("consumer-rules.pro")

    buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8000/\"")
    buildConfigField("String", "API_KEY", "\"dev\"")
  }

  buildFeatures {
    buildConfig = true
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
  implementation(libs.kotlinx.coroutines.android)

  implementation(libs.okhttp)
  implementation(libs.okhttp.logging.interceptor)
  implementation(libs.retrofit)
  implementation(libs.retrofit.converter.moshi)
  implementation(libs.moshi)
  implementation(libs.moshi.kotlin)
  kapt(libs.moshi.kotlin.codegen)

  implementation(libs.hilt.android)
  kapt(libs.hilt.compiler)

  testImplementation(libs.junit4)
  testImplementation(libs.truth)
  testImplementation(libs.okhttp.mockwebserver)
  testImplementation(libs.kotlinx.coroutines.test)
}

kapt {
  correctErrorTypes = true
}
