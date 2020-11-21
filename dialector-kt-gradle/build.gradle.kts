plugins {
    id("java-gradle-plugin")
    kotlin("jvm") version "1.4.10"
    kotlin("kapt") version "1.4.10"
}

gradlePlugin {
    plugins {
        create("gradle-dialector") {
            id = "dev.dialector.gradle"
            implementationClass = "dev.dialector.gradle.DialectorGradlePlugin"
        }
    }
}
dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin-api")
    compileOnly("com.google.auto.service:auto-service:1.0-rc4")
    kapt("com.google.auto.service:auto-service:1.0-rc4")
}