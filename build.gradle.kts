plugins {
  id("org.jetbrains.kotlin.jvm") version "1.3.72"
  `java-library`
}

repositories {
  jcenter()
}

dependencies {
  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.6")
  testImplementation("org.jetbrains.kotlin:kotlin-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}
