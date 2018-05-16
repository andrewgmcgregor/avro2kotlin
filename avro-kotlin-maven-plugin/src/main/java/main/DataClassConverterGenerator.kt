package main

import com.squareup.kotlinpoet.*
import org.apache.avro.Schema

interface KotlinAvroConverter<K, A> {
}

object DataClassConverterGenerator {
    fun generateFrom(spec: SkinnyAvroFileSpec): FileSpec {
        val builder = FileSpec.builder(spec.namespace, spec.name)
        spec.schemaSpecs.forEach { schemaSpec ->
            if (schemaSpec.type == Schema.Type.RECORD) {
                val fileName = "${schemaSpec.name}Converter"
                val superclass: ParameterizedTypeName = ParameterizedTypeName.get(
                        rawType = ClassName("main", "KotlinAvroConverter"),
                        typeArguments = *arrayOf(
                                ClassName(schemaSpec.namespace, "${schemaSpec.name}Kt"),
                                ClassName(schemaSpec.namespace, schemaSpec.name)
                        )
                )
                builder.addType(TypeSpec.classBuilder(fileName)
                        .addSuperinterface(superinterface = superclass)
                        .addFunction(buildConverterToAvro(schemaSpec))
                        .addFunction(buildConverterFromAvro(schemaSpec))
                        .build())
            }
        }
        return builder.build()
    }

    private fun buildConverterToAvro(schemaSpec: SkinnySchemaSpec): FunSpec {
        val parameterName = schemaSpec.name.decapitalize()
        var argList = schemaSpec.fields
                .map { "${parameterName}.${it.name}" + if (it.minimalTypeSpec.avroType) "${if (it.minimalTypeSpec.kotlinType.nullable) "?" else ""}.toAvroSpecificRecord()" else "" }
                .joinToString(prefix = "(", separator = ", ", postfix = ")")
        val buildConverterToAvro = FunSpec.builder("toAvroSpecificRecord")
                .addParameter(name = parameterName, type = ClassName(schemaSpec.namespace, schemaSpec.name))
                .addStatement("return ${schemaSpec.namespace}.${schemaSpec.name}${argList}")
                .build()
        return buildConverterToAvro
    }

    private fun buildConverterFromAvro(schemaSpec: SkinnySchemaSpec): FunSpec {
        val fromAvroSpecificRecordParameterName = schemaSpec.name.decapitalize()
        val kotlinConstructorFieldList = schemaSpec.fields
                .map {
                    var param = "${fromAvroSpecificRecordParameterName}.${it.name}"
                    "${it.name} = " +
                            "${if (it.minimalTypeSpec.kotlinType.nullable) "if (${param} == null) null else " else ""}" +
                            "${if (it.minimalTypeSpec.avroType) "${it.minimalTypeSpec.kotlinType.asNonNullable()}.fromAvroSpecificRecord(" else ""}" +
                            param +
                            "${if (it.minimalTypeSpec.avroType) ")" else ""}"
                }
                .joinToString(prefix = "(", separator = ", ", postfix = ")")
        val fromAvroSpecificRecordBuilder = FunSpec.builder("fromAvroSpecificRecord")
                .addParameter(
                        name = fromAvroSpecificRecordParameterName,
                        type = ClassName(schemaSpec.namespace, "${schemaSpec.name}Kt"))
                .addStatement("return ${schemaSpec.name}Kt${kotlinConstructorFieldList}")
        val fromAvroSpecificRecordFunction = fromAvroSpecificRecordBuilder.build()
        return fromAvroSpecificRecordFunction
    }
}