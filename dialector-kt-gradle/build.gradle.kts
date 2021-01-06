plugins {
    id("java-gradle-plugin")
    kotlin("jvm")
    kotlin("kapt")
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