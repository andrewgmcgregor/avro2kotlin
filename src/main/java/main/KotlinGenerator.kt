package main

import com.squareup.kotlinpoet.*
import org.apache.avro.Schema

object KotlinGenerator {
    fun generate(schema: Schema): FileSpec = FileSpec.builder(schema.namespace, kotlinName(schema))
            .addType(TypeSpec.classBuilder(kotlinName(schema))
                    .addModifiers(KModifier.DATA)
                    .primaryConstructor(buildPrimaryConstructor(schema))
                    .addProperties(buildPropertySpecs(schema))
                    .addFunction(buildConverterToAvro(schema))
                    .companionObject(TypeSpec.companionObjectBuilder(kotlinName(schema))
                            .addFunction(buildConverterFromAvro(schema))
                            .build())
                    .build())
            .build()

    private fun buildPropertySpecs(schema: Schema): Iterable<PropertySpec> {
        return schemaToFieldNamesAndTypes(schema)
                .map { minimalFieldSpec ->
                    PropertySpec
                            .builder(name = minimalFieldSpec.name, type = minimalFieldSpec.minimalTypeSpec.kotlinType)
                            .initializer(minimalFieldSpec.name)
                            .build()
                }
    }

    private fun toKotlinType(schema: Schema): MinimalTypeSpec {
        if (isSimpleKotlinType(schema.type)) {
            return MinimalTypeSpec(
                    kotlinType = toSimpleKotlinType(schema.type)!!,
                    avroType = false)
        }

        if (schema.type == Schema.Type.UNION) {
            val types = schema.types.map { it.type }
            var illegalArgumentMessage = "unions of a single type and null (e.g. union { null, MyRecord })"

            val containsNull = types.contains(Schema.Type.NULL)
            if (!containsNull) {
                throw IllegalArgumentException(illegalArgumentMessage)
            }

            val typesOtherThanNull = types.filterNot { it == Schema.Type.NULL }
            if (typesOtherThanNull.size > 1) {
                throw IllegalArgumentException(illegalArgumentMessage)
            }

            var type = typesOtherThanNull.get(0)
            if (!isSimpleKotlinType(type)) {
                throw IllegalArgumentException(illegalArgumentMessage)
            }

            return MinimalTypeSpec(
                    kotlinType = toSimpleKotlinType(type)!!.asNullable(),
                    avroType = false)
        }

        if (schema.type == Schema.Type.RECORD) {
            var fullKotlinName = "${schema.fullName}Kt"
            var kotlinType = Class.forName(fullKotlinName).asTypeName()
            return MinimalTypeSpec(
                    kotlinType = kotlinType,
                    avroType = true)
        }

        throw IllegalArgumentException(schema.type.getName());
    }

    private fun isSimpleKotlinType(type: Schema.Type): Boolean = toSimpleKotlinType(type) != null

    private fun toSimpleKotlinType(type: Schema.Type): TypeName? {
        val typeName = when (type) {
            Schema.Type.STRING -> String::class
            Schema.Type.INT -> Int::class
            Schema.Type.FLOAT -> Float::class
            Schema.Type.DOUBLE -> Double::class
            Schema.Type.LONG -> Long::class
            Schema.Type.BOOLEAN -> Boolean::class
            else -> null
        }
        return typeName?.asTypeName()
    }

    private fun buildConverterToAvro(schema: Schema): FunSpec {
        var argList = schemaToFieldNamesAndTypes(schema)
                .map { it.name + if (it.minimalTypeSpec.avroType) ".toAvroSpecificRecord()" else "" }
                .joinToString(prefix = "(", separator = ", ", postfix = ")")
        val buildConverterToAvro = FunSpec.builder("toAvroSpecificRecord")
                .addStatement("return ${javaName(schema)}${argList}")
                .build()
        return buildConverterToAvro
    }

    private fun buildConverterFromAvro(schema: Schema): FunSpec {
        val fromAvroSpecificRecordParameterName = schema.name.decapitalize()
        val kotlinConstructorFieldList = schemaToFieldNamesAndTypes(schema)
                .map { (name, type) -> "${name} = ${fromAvroSpecificRecordParameterName}.${name}" }
                .joinToString(prefix = "(", separator = ", ", postfix = ")")
        val fromAvroSpecificRecordBuilder = FunSpec.builder("fromAvroSpecificRecord")
                .addParameter(fromAvroSpecificRecordParameterName, javaType(schema))
                .addStatement("return ${kotlinName(schema)}${kotlinConstructorFieldList}")
        val fromAvroSpecificRecordFunction = fromAvroSpecificRecordBuilder.build()
        return fromAvroSpecificRecordFunction
    }

    private fun javaName(schema: Schema) = schema.name
    private fun kotlinName(schema: Schema) = "${javaName(schema)}Kt"
    private fun javaType(schema: Schema) = Class.forName(schema.fullName)

    private fun buildPrimaryConstructor(schema: Schema): FunSpec {
        val primaryConstructorBuilder = FunSpec.constructorBuilder()
        schemaToFieldNamesAndTypes(schema).forEach { minimalFieldSpec ->
            primaryConstructorBuilder.addParameter(minimalFieldSpec.name, minimalFieldSpec.minimalTypeSpec.kotlinType)
        }
        val primaryConstructor = primaryConstructorBuilder.build()
        return primaryConstructor
    }

    private fun schemaToFieldNamesAndTypes(schema: Schema): List<MinimalFieldSpec> {
        return schema.fields
                .filterNotNull()
                .map { field ->
                    MinimalFieldSpec(
                            name = field.name(),
                            minimalTypeSpec = toKotlinType(field.schema()))
                }
    }

    private data class MinimalTypeSpec(val kotlinType: TypeName,
                                       val avroType: Boolean)

    private data class MinimalFieldSpec(val name: String,
                                        val minimalTypeSpec: MinimalTypeSpec)
}