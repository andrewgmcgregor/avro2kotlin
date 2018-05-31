package net.avro2kotlin.compiler

import org.apache.avro.Schema
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class SkinnySchemaSpecTest {

    @Test
    fun shouldBuildMultipleSchemas() {
        val testProtocol = """
            @namespace("com.example")
            protocol ExampleInterface {
              record ExampleRecord1 {}
              record ExampleRecord2 {}
            }
        """

        val avroInterfaceSpec = SkinnySchemaSpecBuilder.generateFromAvdl(testProtocol.byteInputStream())

        val skinnySchemaSpec1 = avroInterfaceSpec.schemaSpecs.get(0)
        assertThat(skinnySchemaSpec1.namespace, `is`("com.example"))
        assertThat(skinnySchemaSpec1.name, `is`("ExampleRecord1"))
        assertThat(skinnySchemaSpec1.type, `is`(Schema.Type.RECORD))

        val skinnySchemaSpec2 = avroInterfaceSpec.schemaSpecs.get(1)
        assertThat(skinnySchemaSpec2.namespace, `is`("com.example"))
        assertThat(skinnySchemaSpec2.name, `is`("ExampleRecord2"))
    }

    @Test
    fun shouldBuildMultipleFields() {
        val testProtocol = """
            @namespace("com.example")
            protocol ExampleInterface {
              record ExampleRecord {
                string testString;
                int testInt;
                boolean testBoolean;
              }
            }
        """

        val avroInterfaceSpec = SkinnySchemaSpecBuilder.generateFromAvdl(testProtocol.byteInputStream())

        val skinnySchemaSpec = avroInterfaceSpec.schemaSpecs.get(0)
        assertThat(skinnySchemaSpec.namespace, `is`("com.example"))
        assertThat(skinnySchemaSpec.name, `is`("ExampleRecord"))

        assertThat(skinnySchemaSpec.fields.get(0).name, `is`("testString"))
        assertThat(skinnySchemaSpec.fields.get(0).minimalTypeSpec!!.kotlinType.toString(), `is`("kotlin.String"))

        assertThat(skinnySchemaSpec.fields.get(1).name, `is`("testInt"))
        assertThat(skinnySchemaSpec.fields.get(1).minimalTypeSpec!!.kotlinType.toString(), `is`("kotlin.Int"))

        assertThat(skinnySchemaSpec.fields.get(2).name, `is`("testBoolean"))
        assertThat(skinnySchemaSpec.fields.get(2).minimalTypeSpec!!.kotlinType.toString(), `is`("kotlin.Boolean"))
    }

    @Test
    fun shouldBuildEnum() {
        val testProtocol = """
            @namespace("com.example")
            protocol ExampleInterfaceWithEnum {
              enum ExampleEnum {
                ENUM_VALUE_1,
                ENUM_VALUE_2
              }
            }
        """

        val avroInterfaceSpec = SkinnySchemaSpecBuilder.generateFromAvdl(testProtocol.byteInputStream())

        val skinnySchemaSpec = avroInterfaceSpec.schemaSpecs.get(0)
        assertThat(skinnySchemaSpec.namespace, `is`("com.example"))
        assertThat(skinnySchemaSpec.name, `is`("ExampleEnum"))

        assertThat(skinnySchemaSpec.fields.get(0).name, `is`("ENUM_VALUE_1"))
        assertThat(skinnySchemaSpec.fields.get(0).minimalTypeSpec, `is`(nullValue()))

        assertThat(skinnySchemaSpec.fields.get(1).name, `is`("ENUM_VALUE_2"))
        assertThat(skinnySchemaSpec.fields.get(1).minimalTypeSpec, `is`(nullValue()))
    }

}