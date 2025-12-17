plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kotlin.kapt) apply false
  alias(libs.plugins.hilt) apply false
  alias(libs.plugins.ktlint) apply false
  alias(libs.plugins.google.services) apply false
}

subprojects {
  pluginManager.withPlugin("org.jlleitschuh.gradle.ktlint") {
    extensions.configure<org.jlleitschuh.gradle.ktlint.KtlintExtension>("ktlint") {
      android.set(true)
      ignoreFailures.set(false)
    }
  }
}
