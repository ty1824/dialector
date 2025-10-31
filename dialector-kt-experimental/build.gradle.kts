import jdk.tools.jlink.resources.plugins

plugins {
    alias(libs.plugins.kotlin.jvm)
    id("maven-publish")
    signing
}

dependencies {
    implementation(project(":dialector-kt"))
    implementation(libs.kotlin.reflect)

    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockk)
}

kotlin {
    explicitApiWarning()
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.withType<Test> {
    useJUnitPlatform()
}
