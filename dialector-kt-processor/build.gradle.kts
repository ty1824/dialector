plugins {
    `java-library`
    kotlin("jvm")
    kotlin("kapt")
}

repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation(kotlin("reflect"))
    implementation("com.squareup:kotlinpoet:1.6.0")
    implementation(project(":dialector-kt"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.4.10-dev-experimental-20201120")
    implementation("com.google.auto.service:auto-service:1.0-rc4")
    kapt("com.google.auto.service:auto-service:1.0-rc4")
}