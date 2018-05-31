package net.avro2kotlin.compiler

import com.squareup.kotlinpoet.ClassName
import net.avro2kotlin.compiler.*
import org.apache.avro.Schema
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class DataClassGeneratorTest {

    private val spec = SkinnyAvroFileSpec(
            namespace = "com.example",
            name = "ExampleInterface",
            schemaSpecs = listOf(
                    SkinnySchemaSpec(
                            namespace = "com.example",
                            name = "ExampleNesting",
                            type = Schema.Type.RECORD,
                            fields = listOf()
                    ),
                    SkinnySchemaSpec(
                            namespace = "com.example",
                            name = "ExampleRecord",
                            type = Schema.Type.RECORD,
                            fields = listOf(
                                    MinimalFieldSpec(
                                            name = "exampleString",
                                            minimalTypeSpec = MinimalTypeSpec(
                                                    namespace = "kotlin",
                                                    name = "String",
                                                    kotlinType = ClassName("kotlin", "String"),
                                                    avroType = false)
                                    ),
                                    MinimalFieldSpec(
                                            name = "exampleNesting",
                                            minimalTypeSpec = MinimalTypeSpec(
                                                    namespace = "com.example",
                                                    name = "ExampleNesting",
                                                    kotlinType = ClassName("com.example", "ExampleNesting"),
                                                    avroType = true)
                                    )
                            )
                    )
            )
    )

    @Test
    fun shouldOutputDataClass() {
        val fileMaker = DataClassGenerator.generateFrom(spec)
        assertThat(fileMaker.relativePath, `is`("com/example"))
        assertThat(fileMaker.fileName, `is`("ExampleInterface.kt"))

        val generatedCode = fileMaker.content
        assertThat(generatedCode, containsString("class ExampleRecord"))
        assertThat(generatedCode, containsString("val exampleString: String"))
    }

    @Test
    fun shouldOutputConverter() {
        val fileMaker = DataClassConverterGenerator.generateFrom(spec)
        assertThat(fileMaker.relativePath, `is`("com/example/converter"))
        assertThat(fileMaker.fileName, `is`("ExampleInterfaceConverter.kt"))

        val generatedCode = fileMaker.content
        assertThat(generatedCode, containsString("class ExampleRecordConverter : KotlinAvroConverter<ExampleRecordKt, ExampleRecord>"))
        assertThat(generatedCode, containsString("com.example.converter.ExampleNestingConverter.toAvroSpecificRecord(exampleRecord.exampleNesting)"))
        assertThat(generatedCode, containsString("override fun toAvroSpecificRecord(exampleRecord: ExampleRecordKt): ExampleRecord = ExampleRecordConverter.toAvroSpecificRecord(exampleRecord)"))
        assertThat(generatedCode, containsString("fun toAvroSpecificRecord(exampleRecord: ExampleRecordKt)"))
        assertThat(generatedCode, containsString("override fun fromAvroSpecificRecord(exampleRecord: ExampleRecord): ExampleRecordKt = ExampleRecordConverter.fromAvroSpecificRecord(exampleRecord)"))
        assertThat(generatedCode, containsString("fun fromAvroSpecificRecord(exampleRecord: ExampleRecord)"))
    }
}