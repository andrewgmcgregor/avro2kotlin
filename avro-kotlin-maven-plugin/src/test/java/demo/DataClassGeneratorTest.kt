package demo

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import main.*
import org.apache.avro.Schema
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class DataClassGeneratorTest {

    private val spec = SkinnyAvroFileSpec(
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

    @Test
    fun shouldOutputDataClass() {
        val fileSpec = DataClassGenerator.generateFrom(spec)
        val generatedCode = extractContent(fileSpec)

        assertThat(generatedCode, containsString("class ExampleRecord"))
        assertThat(generatedCode, containsString("val exampleString: String"))
    }

    @Test
    fun shouldOutputConverter() {
        val fileSpec = DataClassConverterGenerator.generateFrom(spec)
        val generatedCode = extractContent(fileSpec)

        assertThat(generatedCode, containsString("class ExampleRecordConverter : KotlinAvroConverter<ExampleRecordKt, ExampleRecord>"))
        assertThat(generatedCode, containsString("fun toAvroSpecificRecord(exampleRecord: ExampleRecord)"))
        assertThat(generatedCode, containsString("fun fromAvroSpecificRecord(exampleRecord: ExampleRecordKt)"))
    }

    private fun extractContent(fileSpec: FileSpec): String {
        val builder = StringBuilder()
        fileSpec.writeTo(builder)
        val fileContent = builder.toString()
        return fileContent
    }

}