plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlinx.kover")
    `java-library`
}

val ktorVersion: String by project
dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.2")

    implementation(project(":dialector-kt"))
    api("org.eclipse.lsp4j:org.eclipse.lsp4j:0.19.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testImplementation("io.mockk:mockk:1.13.4")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("com.natpryce:hamkrest:1.7.0.2")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

kover {
    xmlReport {
        onCheck.set(true)
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}