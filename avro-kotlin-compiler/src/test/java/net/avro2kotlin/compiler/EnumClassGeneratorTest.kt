package net.avro2kotlin.compiler

import net.avro2kotlin.compiler.*
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
        assertThat(generatedCode, containsString("override fun toAvroSpecificRecord(exampleEnum: ExampleEnumKt): ExampleEnum = ExampleEnumConverter.toAvroSpecificRecord(exampleEnum)"))
        assertThat(generatedCode, containsString("fun toAvroSpecificRecord(exampleEnum: ExampleEnumKt) = com.example.ExampleEnum.valueOf(exampleEnum.name)"))
        assertThat(generatedCode, containsString("override fun fromAvroSpecificRecord(exampleEnum: ExampleEnum): ExampleEnumKt = ExampleEnumConverter.fromAvroSpecificRecord(exampleEnum)"))
        assertThat(generatedCode, containsString("fun fromAvroSpecificRecord(exampleEnum: ExampleEnum) = ExampleEnumKt.valueOf(exampleEnum.name)"))
    }
}