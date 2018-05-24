package main

import org.apache.avro.Schema
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class EnumClassGeneratorTest {

    val spec = SkinnyAvroFileSpec(
            namespace = "com.example",
            name = "ExampleInterfaceWithEnum",
            schemaSpecs = listOf(
                    SkinnySchemaSpec(
                            namespace = "com.example",
                            name = "ExampleEnum",
                            type = Schema.Type.ENUM,
                            fields = listOf(
                                    MinimalFieldSpec("ENUM_VALUE_1", null),
                                    MinimalFieldSpec("ENUM_VALUE_2", null)
                            )
                    )
            )
    )

    @Test
    fun shouldOutputEnum() {
        val generatedCode = DataClassGenerator.generateFrom(spec).content

        assertThat(generatedCode, containsString("enum class ExampleEnum"))
        assertThat(generatedCode, containsString("ENUM_VALUE_1,"))
        assertThat(generatedCode, containsString("ENUM_VALUE_2"))
    }

    @Test
    fun shouldOutputConverterForEnum() {
        val generatedCode = DataClassConverterGenerator.generateFrom(spec).content

        assertThat(generatedCode, containsString("class ExampleEnumConverter : KotlinAvroConverter<ExampleEnumKt, ExampleEnum>"))
        TODO("GET THE CONVERTER WORKING WITHOUT RELYING ON INHERITING FROM SPECIFIC RECORD")
//        assertThat(generatedCode, containsString("override fun toAvroSpecificRecord(exampleRecord: ExampleRecordKt): ExampleRecord = ExampleRecordConverter.toAvroSpecificRecord(exampleRecord)"))
//        assertThat(generatedCode, containsString("fun toAvroSpecificRecord(exampleRecord: ExampleRecordKt)"))
//        assertThat(generatedCode, containsString("override fun fromAvroSpecificRecord(exampleRecord: ExampleRecord): ExampleRecordKt = ExampleRecordConverter.fromAvroSpecificRecord(exampleRecord)"))
//        assertThat(generatedCode, containsString("fun fromAvroSpecificRecord(exampleRecord: ExampleRecord)"))
    }
}