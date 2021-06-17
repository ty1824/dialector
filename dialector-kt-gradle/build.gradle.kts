plugins {
    id("java-gradle-plugin")
    kotlin("jvm")
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
}