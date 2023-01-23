plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlinx.kover")
    id("maven-publish")
}

dependencies {
    implementation(kotlin("reflect"))

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("io.mockk:mockk:1.12.0")
}

kotlin {
    explicitApiWarning()
}

kover {
    xmlReport {
        onCheck.set(true)
    }
}

publishing {
    repositories {
        maven {
        }
    }
}

//dependencies {
//    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
//    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
//    implementation(kotlin("reflect"))
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
//
////    implementation("com.squareup:kotlinpoet:1.12.0")
////    implementation("com.google.guava:guava:31.1-jre")
//
//    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
//    testImplementation("io.mockk:mockk:1.9.3")
//    testImplementation("org.jetbrains.kotlin:kotlin-test")
//    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
//    testImplementation("com.natpryce:hamkrest:1.7.0.2")
//
//    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
//}


//tasks.withType<Test> {
//    useJUnitPlatform()
//}

//val compileKotlin: KotlinCompile by tasks
//compileKotlin.kotlinOptions {
//    languageVersion = "1.4"
//}