plugins {
    kotlin("jvm") version "1.4.10"
    `java-library`
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation(kotlin("reflect"))

    implementation("com.squareup:kotlinpoet:1.6.0")
    implementation("com.google.guava:guava:28.2-jre")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("com.natpryce:hamkrest:1.7.0.2")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

kotlin.sourceSets.main {
    kotlin.srcDirs += file("src/main/generated")
}

tasks.withType<Test> {
    useJUnitPlatform()
}