package net.avro2kotlin.compiler

import com.squareup.kotlinpoet.*
import org.apache.avro.Schema


object DataClassGenerator {
    fun generateFrom(avroSpec: SkinnyAvroFileSpec): FileMaker {
        val builder = FileSpec.builder(avroSpec.namespace, avroSpec.name)
        avroSpec.schemaSpecs.forEach { schemaSpec ->
            if (schemaSpec.type == Schema.Type.RECORD) {
                val kotlinClassName = "${schemaSpec.name}Kt"
                builder.addType(TypeSpec.classBuilder(kotlinClassName)
                        .addModifiers(KModifier.DATA)
                        .primaryConstructor(buildPrimaryConstructor(schemaSpec))
                        .addProperties(buildPropertySpecs(schemaSpec))
                        .build())
            } else if (schemaSpec.type == Schema.Type.ENUM) {
                val kotlinClassName = "${schemaSpec.name}Kt"
                val enumBuilder = TypeSpec.enumBuilder(kotlinClassName)
                schemaSpec.fields.forEach { minimalFieldSpec ->
                    enumBuilder.addEnumConstant(name = minimalFieldSpec.name)
                }
                builder.addType(enumBuilder.build())
            }
        }

        val fileSpec = builder.build()
        return AvroSpecFileMakerFactory.newInstance(avroSpec, fileSpec)
    }

    private fun buildPrimaryConstructor(schemaSpec: SkinnySchemaSpec): FunSpec {
        val primaryConstructorBuilder = FunSpec.constructorBuilder()
        schemaSpec.fields.forEach { minimalFieldSpec ->
            primaryConstructorBuilder.addParameter(minimalFieldSpec.name, minimalFieldSpec.minimalTypeSpec!!.kotlinType)
        }
        val primaryConstructor = primaryConstructorBuilder.build()
        return primaryConstructor
    }

    private fun buildPropertySpecs(schemaSpec: SkinnySchemaSpec): Iterable<PropertySpec> {
        return schemaSpec.fields.map { minimalFieldSpec ->
                    PropertySpec
                            .builder(name = minimalFieldSpec.name, type = minimalFieldSpec.minimalTypeSpec!!.kotlinType)
                            .initializer(minimalFieldSpec.name)
                            .build()
                }
    }
}