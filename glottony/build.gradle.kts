plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.41"
    antlr
}

dependencies {
    antlr("org.antlr:antlr4:4.8")
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation(kotlin("reflect"))

    implementation("org.antlr:antlr4-runtime:4.8")

    implementation("com.google.guava:guava:28.2-jre")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("com.natpryce:hamkrest:1.7.0.2")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

//tasks.named("generateGrammarSource") {
//    outputDirectory = new File("${project.buildDir}/generated-src/antlr/main/net/example".toString())
//}

tasks.withType<Test> {
    useJUnitPlatform()
}