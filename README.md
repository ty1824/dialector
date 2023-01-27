# dialector
A language workbench for anyone.

Dialector provides a series of libraries designed to simplify development of language servers & other language tooling.

## Consumer Quick Start (For Kotlin projects using Gradle)

Add dialector-kt and dialector-kt-processor to your dependencies:
```
val dialectorVersion = "0.2.0
dependencies {
    ...
    api("dev.dialector:dialector-kt:${dialectorVersion}")
    ksp("dev.dialector:dialector-kt-processor:${dialectorVersion}")
    ...
}
```

Add the Kotlin Symbol Processor (KSP) plugin to your gradle build
```
plugins {
    ...
    kotlin("jvm") version("1.7.10")
    id("com.google.devtools.ksp") version("1.7.10-1.0.6") // Be sure to use a KSP version that is compatible with your Kotlin version!!
    ...
}
```

Add KSP configuration to set the output package for your generated code
```
ksp {
    arg("dev.dialector.targetPackage", "org.example.output") // Replace "org.example.output" with a package relevant to your project
}
```

If you are using IntelliJ, add the following to ensure your generated code is indexed:
```
sourceSets.getByName("main").java 
    srcDir("build/generated/ksp/main/kotlin")
}
```

At this point, you can start using Dialector! Running a gradle build will generate sources and include them in compilation automatically.
A more thorough introduction of how Dialector works and common use cases will be provided soon.
