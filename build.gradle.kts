plugins {
  id("com.github.fkorotkov.libraries") version "1.1"
  id("org.jetbrains.kotlin.jvm") version "1.3.72"
  `java-library`
}

repositories {
  jcenter()
}

dependencies {
  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
  implementation(libraries["org.jetbrains.kotlinx:kotlinx-coroutines-core"])
  testImplementation("org.jetbrains.kotlin:kotlin-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}
