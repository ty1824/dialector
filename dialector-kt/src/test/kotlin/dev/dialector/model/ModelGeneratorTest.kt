package dev.dialector.model

import dev.dialector.model.sample.MStruct
import dev.dialector.model.sample.MStructField
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import kotlin.test.assertFails

@NodeDefinition
interface ValidNode : Node {
    @Property
    val validProperty: String

    @Child
    val validChild: ValidNode

    @Reference
    val validReference: NodeReference<ValidNode>
}

@NodeDefinition
interface NotANode

@NodeDefinition
interface ANode : Node {
    @Property
    val notValidProperty: ANode

    @Child
    val notValidChild: String

    @Reference
    val notValidReference: ANode
}

class ModelGeneratorTest {

    @Test
    fun simpleTest() {
        Generator.generate(listOf(
            ValidNode::class,
            MStruct::class,
            MStructField::class
        )).writeTo(Paths.get("./src/main/generated/"))
    }

    @Test
    fun invalidNodeStructureTest() {
        assertFails { Generator.generate(listOf(ANode::class)) }
    }



}