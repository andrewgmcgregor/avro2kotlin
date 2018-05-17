package main

import com.squareup.kotlinpoet.ClassName
import org.apache.avro.Schema
import org.hamcrest.CoreMatchers.`is`
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

//    @Test
//    fun shouldOutputDataClass() {
//        val fileMaker = main.DataClassGenerator.generateFrom(spec)
//        assertThat(fileMaker.relativePath, `is`("com/example"))
//        assertThat(fileMaker.fileName, `is`("ExampleInterface.kt"))
//
//        val generatedCode = fileMaker.content
//        assertThat(generatedCode, containsString("class ExampleRecord"))
//        assertThat(generatedCode, containsString("val exampleString: String"))
//    }

//    @Test
//    fun shouldOutputConverter() {
//        val fileMaker = main.DataClassConverterGenerator.generateFrom(spec)
//        assertThat(fileMaker.relativePath, `is`("com/example/converter"))
//        assertThat(fileMaker.fileName, `is`("ExampleInterfaceConverter.kt"))
//
//        val generatedCode = fileMaker.content
//        assertThat(generatedCode, containsString("class ExampleRecordConverter : KotlinAvroConverter<ExampleRecordKt, ExampleRecord>"))
//        assertThat(generatedCode, containsString("fun toAvroSpecificRecord(exampleRecord: ExampleRecord)"))
//        assertThat(generatedCode, containsString("fun fromAvroSpecificRecord(exampleRecord: ExampleRecordKt)"))
//    }

}