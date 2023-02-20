# dialector-kt-processor

A Kotlin Symbol Processor for dialector-kt that generates Node implementations and builders for annotated Node interfaces.

## Usage
This module provides a plugin for [Kotlin Symbol Processor](https://kotlinlang.org/docs/ksp-overview.html) that processes
annotated interfaces extending `Node` and generates implementations along with convenient builder DSLs. 

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
    arg("dev.dialector.targetPackage", "org.example.output") // Required
    arg("dev.dialector.indent", "    ") // Optional, the indent pattern used for codegen. Defaults to 4 spaces.
}
```

If you are using IntelliJ, add the following to ensure your generated code is indexed & compiled.
```
sourceSets.getByName("main").java 
    srcDir("build/generated/ksp/main/kotlin")
}
```