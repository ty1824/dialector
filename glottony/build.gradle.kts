plugins {
    id("symbol-processing") version "1.4.10-dev-experimental-20201120"
    kotlin("jvm") version "1.4.10"
    java
    antlr
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    antlr("org.antlr:antlr4:4.8")

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation(kotlin("reflect"))

    implementation("org.antlr:antlr4-runtime:4.8")
    implementation("com.google.guava:guava:28.2-jre")
    implementation(project(":dialector-kt"))
    implementation(project(":dialector-kt-processor"))
    ksp(project(":dialector-kt-processor"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("com.natpryce:hamkrest:1.7.0.2")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

gradle.taskGraph.addTaskExecutionListener(object : TaskExecutionListener {
    var lastTimestamp: Long = 0
    override fun beforeExecute(task: Task) {
        lastTimestamp = System.nanoTime()
    }

    override fun afterExecute(task: Task, state: TaskState) {
        println("Time spent: " + (System.nanoTime() - lastTimestamp)/ 1000000000.0)
    }
})

tasks.named("generateGrammarSource", AntlrTask::class) {
    arguments.addAll(listOf("-visitor"))
    outputDirectory = File("${project.projectDir}/src/main/gen/java/dev/dialector/glottony/parser")
}

sourceSets.getByName("main").java {
    srcDir("src/main/gen/java")
    srcDir("build/generated/ksp/main/kotlin")
}

tasks.withType<Test> {
    useJUnitPlatform()
}