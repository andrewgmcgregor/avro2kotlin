package demo

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import main.*
import org.apache.avro.Schema
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class KotlinGeneratorTest {

    @Test
    fun shouldOutputRecord() {
        val spec = SkinnyAvroFileSpec(
                namespace = "com.example",
                name = "ExampleInterface",
                schemaSpecs = listOf(SkinnySchemaSpec(
                        namespace = "<IGNORED>",
                        name = "ExampleRecord",
                        type = Schema.Type.RECORD,
                        fields = listOf(MinimalFieldSpec(
                                name = "exampleString",
                                minimalTypeSpec = MinimalTypeSpec(
                                        kotlinType = ClassName("kotlin", "String"),
                                        avroType = false)
                        ))
                ))
        )

        val fileSpec = DataClassGenerator.generateFrom(spec)
        val generatedCode = extractContent(fileSpec)

        assertThat(generatedCode, containsString("data class ExampleRecordKt"))
        assertThat(generatedCode, containsString("val exampleString: String"))

        println("generatedCode = ${generatedCode}")
    }

    private fun extractContent(fileSpec: FileSpec): String {
        val builder = StringBuilder()
        fileSpec.writeTo(builder)
        val fileContent = builder.toString()
        return fileContent
    }

}