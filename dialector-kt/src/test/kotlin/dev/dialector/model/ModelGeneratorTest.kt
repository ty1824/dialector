package dev.dialector.model

import dev.dialector.model.sample.MClass
import org.junit.jupiter.api.Test
import java.nio.file.Paths

class ModelGeneratorTest {

    @Test
    fun simpleTest() {
        Generator().generate(MClass::class).writeTo(Paths.get("./src/main/generated/"))
    }
}