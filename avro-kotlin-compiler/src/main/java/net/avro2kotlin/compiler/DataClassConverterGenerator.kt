package net.avro2kotlin.compiler

import com.squareup.kotlinpoet.*
import org.apache.avro.Schema

object DataClassConverterGenerator {
    fun generateFrom(avroSpec: SkinnyAvroFileSpec): FileMaker {
        val converterAvroSpec = avroSpec.copy(
                namespace = "${avroSpec.namespace}.converter",
                name = "${avroSpec.name}Converter")

        val builder = FileSpec.builder(converterAvroSpec.namespace, converterAvroSpec.name)
        converterAvroSpec.schemaSpecs.forEach { schemaSpec ->
            if (schemaSpec.type == Schema.Type.RECORD) {
                val fileName = "${schemaSpec.name}Converter"
                val superclass: ParameterizedTypeName = ParameterizedTypeName.get(
                        rawType = ClassName("net.avro2kotlin.compiler", "KotlinAvroConverter"),
                        typeArguments = *arrayOf(
                                ClassName(schemaSpec.namespace, "${schemaSpec.name}Kt"),
                                ClassName(schemaSpec.namespace, schemaSpec.name)
                        )
                )
                builder.addType(TypeSpec.classBuilder(fileName)
                        .addSuperinterface(superinterface = superclass)
                        .addFunction(buildConverterToAvroOverride(schemaSpec))
                        .addFunction(buildConverterFromAvroOverride(schemaSpec))
                        .companionObject(TypeSpec.companionObjectBuilder()
                                .addFunction(buildConverterToAvro(schemaSpec))
                                .addFunction(buildConverterFromAvro(schemaSpec))
                                .build())
                        .build())
            } else if (schemaSpec.type == Schema.Type.ENUM) {
                val fileName = "${schemaSpec.name}Converter"
                val superclass: ParameterizedTypeName = ParameterizedTypeName.get(
                        rawType = ClassName("net.avro2kotlin.compiler", "KotlinAvroConverter"),
                        typeArguments = *arrayOf(
                                ClassName(schemaSpec.namespace, "${schemaSpec.name}Kt"),
                                ClassName(schemaSpec.namespace, schemaSpec.name)
                        )
                )
                builder.addType(TypeSpec.classBuilder(fileName)
                        .addSuperinterface(superinterface = superclass)
                        .addFunction(buildConverterToAvroOverride(schemaSpec))
                        .addFunction(buildConverterFromAvroOverride(schemaSpec))
                        .companionObject(TypeSpec.companionObjectBuilder()
                                .addFunction(buildEnumConverterToAvro(schemaSpec))
                                .addFunction(buildEnumConverterFromAvro(schemaSpec))
                                .build())
                        .build())
            }
        }

        val fileSpec = builder.build()
        return AvroSpecFileMakerFactory.newInstance(
                converterAvroSpec, fileSpec)
    }

    private fun buildConverterToAvroOverride(schemaSpec: SkinnySchemaSpec): FunSpec {
        val name = schemaSpec.name.decapitalize()
        return FunSpec.builder("toAvroSpecificRecord")
                .addModifiers(listOf(KModifier.OVERRIDE))
                .addParameter(name = name, type = ClassName(schemaSpec.namespace, "${schemaSpec.name}Kt"))
                .returns(ClassName(schemaSpec.namespace, "${schemaSpec.name}"))
                .addStatement("return ${schemaSpec.name}Converter.toAvroSpecificRecord(${name})")
                .build()
    }

    private fun buildConverterFromAvroOverride(schemaSpec: SkinnySchemaSpec): FunSpec {
        val name = schemaSpec.name.decapitalize()
        return FunSpec.builder("fromAvroSpecificRecord")
                .addModifiers(listOf(KModifier.OVERRIDE))
                .addParameter(name = name, type = ClassName(schemaSpec.namespace, "${schemaSpec.name}"))
                .returns(ClassName(schemaSpec.namespace, "${schemaSpec.name}Kt"))
                .addStatement("return ${schemaSpec.name}Converter.fromAvroSpecificRecord(${name})")
                .build()
    }

    private fun buildConverterToAvro(schemaSpec: SkinnySchemaSpec): FunSpec {
        val toAvroSpecificRecordParameterName = schemaSpec.name.decapitalize()
        val parameterName = schemaSpec.name.decapitalize()
        var argList = schemaSpec.fields
                .map { minimalFieldSpec ->
                    val converterName = "${minimalFieldSpec.minimalTypeSpec!!.namespace}.converter.${minimalFieldSpec.minimalTypeSpec.name}Converter"
                    var param = "${toAvroSpecificRecordParameterName}.${minimalFieldSpec.name}"
                    "" +
                            "${if (minimalFieldSpec.minimalTypeSpec!!.kotlinType.nullable) "if (${param} == null) null else " else ""}" +
                            "${if (minimalFieldSpec.minimalTypeSpec!!.avroType) "${converterName}.toAvroSpecificRecord(" else ""}" +
                            param +
                            "${if (minimalFieldSpec.minimalTypeSpec!!.avroType) ")" else ""}"
                }
                .joinToString(prefix = "(", separator = ", ", postfix = ")")
        val buildConverterToAvro = FunSpec.builder("toAvroSpecificRecord")
                .addParameter(name = parameterName, type = ClassName(schemaSpec.namespace, "${schemaSpec.name}Kt"))
                .addStatement("return ${schemaSpec.namespace}.${schemaSpec.name}${argList}")
                .build()
        return buildConverterToAvro
    }

    private fun buildEnumConverterToAvro(schemaSpec: SkinnySchemaSpec): FunSpec {
        val toAvroSpecificRecordParameterName = schemaSpec.name.decapitalize()
        val parameterName = schemaSpec.name.decapitalize()
        val buildConverterToAvro = FunSpec.builder("toAvroSpecificRecord")
                .addParameter(name = parameterName, type = ClassName(schemaSpec.namespace, "${schemaSpec.name}Kt"))
                .addStatement("return ${schemaSpec.namespace}.${schemaSpec.name}.valueOf(${toAvroSpecificRecordParameterName}.name)")
                .build()
        return buildConverterToAvro
    }

    private fun buildConverterFromAvro(schemaSpec: SkinnySchemaSpec): FunSpec {
        val fromAvroSpecificRecordParameterName = schemaSpec.name.decapitalize()
        val kotlinConstructorFieldList = schemaSpec.fields
                .map { minimalFieldSpec ->
                    val converterName = "${minimalFieldSpec.minimalTypeSpec!!.namespace}.converter.${minimalFieldSpec.minimalTypeSpec.name}Converter"
                    var param = "${fromAvroSpecificRecordParameterName}.${minimalFieldSpec.name}"
                    "${minimalFieldSpec.name} = " +
                            "${if (minimalFieldSpec.minimalTypeSpec!!.kotlinType.nullable) "if (${param} == null) null else " else ""}" +
                            "${if (minimalFieldSpec.minimalTypeSpec!!.avroType) "${converterName}.fromAvroSpecificRecord(" else ""}" +
                            param +
                            "${if (minimalFieldSpec.minimalTypeSpec!!.avroType) ")" else ""}"
                }
                .joinToString(prefix = "(", separator = ", ", postfix = ")")
        val fromAvroSpecificRecordBuilder = FunSpec.builder("fromAvroSpecificRecord")
                .addParameter(
                        name = fromAvroSpecificRecordParameterName,
                        type = ClassName(schemaSpec.namespace, schemaSpec.name))
                .addStatement("return ${schemaSpec.name}Kt${kotlinConstructorFieldList}")
        val fromAvroSpecificRecordFunction = fromAvroSpecificRecordBuilder.build()
        return fromAvroSpecificRecordFunction
    }

    private fun buildEnumConverterFromAvro(schemaSpec: SkinnySchemaSpec): FunSpec {
        val fromAvroSpecificRecordParameterName = schemaSpec.name.decapitalize()
        val fromAvroSpecificRecordBuilder = FunSpec.builder("fromAvroSpecificRecord")
                .addParameter(
                        name = fromAvroSpecificRecordParameterName,
                        type = ClassName(schemaSpec.namespace, schemaSpec.name))
                .addStatement("return ${schemaSpec.name}Kt.valueOf(${fromAvroSpecificRecordParameterName}.name)")
        val fromAvroSpecificRecordFunction = fromAvroSpecificRecordBuilder.build()
        return fromAvroSpecificRecordFunction
    }
}