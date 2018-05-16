package demo

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import main.*
import org.apache.avro.Schema
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class DataClassConverterGeneratorTest {

    @Test
    fun shouldOutputRecord() {
        val spec = SkinnyAvroFileSpec(
                namespace = "com.example",
                name = "ExampleInterface",
                schemaSpecs = listOf(SkinnySchemaSpec(
                        namespace = "com.example",
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

        val fileSpec = DataClassConverterGenerator.generateFrom(spec)
        val generatedCode = extractContent(fileSpec)


        assertThat(generatedCode, containsString("class ExampleRecordConverter : KotlinAvroConverter<ExampleRecordKt, ExampleRecord>"))
//        assertThat(generatedCode, containsString("fun toAvroSpecificRecord("))

        println("generatedCode = ${generatedCode}")
    }

    private fun extractContent(fileSpec: FileSpec): String {
        val builder = StringBuilder()
        fileSpec.writeTo(builder)
        val fileContent = builder.toString()
        return fileContent
    }

}