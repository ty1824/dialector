package dev.dialector.model

import dev.dialector.model.sample.MClass
import dev.dialector.model.sample.MField
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class ModelGeneratorTest {

    @Test
    fun simpleTest() {
        Generator().generateFromClasses(listOf(
            MClass::class,
            MField::class
        )).writeTo(Paths.get("./src/main/generated/"))
    }
}