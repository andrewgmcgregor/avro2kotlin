package main

import com.squareup.kotlinpoet.*
import org.apache.avro.Schema

object KotlinGenerator {
    fun generate(schema: Schema): FileSpec =
        FileSpec.builder(schema.namespace, kotlinName(schema))
                .addType(TypeSpec.classBuilder(kotlinName(schema))
                        .addModifiers(KModifier.DATA)
                        .primaryConstructor(buildPrimaryConstructor(schema))
                        .addFunction(buildConverterToAvro(schema))
                        .companionObject(TypeSpec.companionObjectBuilder(kotlinName(schema))
                                .addFunction(buildConverterFromAvro(schema))
                                .build())
                        .build()
                )
                .build()

    private fun toKotlinType(schema: Schema): TypeName {
        if (isSimpleKotlinType(schema.type)) {
            return toSimpleKotlinType(schema.type)!!
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

            return toSimpleKotlinType(type)!!.asNullable()
        }

        throw IllegalArgumentException(schema.type.getName());
    }

    private fun isSimpleKotlinType(type: Schema.Type): Boolean
            = toSimpleKotlinType(type) != null

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
        var fieldNames = schema.fields.map { it.name() }
        val fieldList = fieldNames.joinToString(prefix = "(", separator = ", ", postfix = ")")
        val buildConverterToAvro = FunSpec.builder("toAvroSpecificRecord")
                .returns(javaType(schema))
                .addStatement("${javaName(schema)}${fieldList}")
                .build()
        return buildConverterToAvro
    }

    private fun buildConverterFromAvro(schema: Schema): FunSpec {
        val fromAvroSpecificRecordParameterName = schema.name.decapitalize()
        val kotlinConstructorFieldList = schemaToFieldNamesAndTypes(schema)
                .map { (name, type) ->
                    val suffix = if (type.nullable) "!!" else ""
                    "${name} = ${fromAvroSpecificRecordParameterName}.${name}${suffix}"
                }
                .joinToString(prefix = "(", separator = ", ", postfix = ")")
        val fromAvroSpecificRecordBuilder = FunSpec.builder("fromAvroSpecificRecord")
                .returns(Class.forName(fullKotlinName(schema)))
                .addParameter(fromAvroSpecificRecordParameterName, javaType(schema))
                .addStatement("${kotlinName(schema)}${kotlinConstructorFieldList}")
        val fromAvroSpecificRecordFunction = fromAvroSpecificRecordBuilder.build()
        return fromAvroSpecificRecordFunction
    }

    private fun javaName(schema: Schema) = schema.name
    private fun kotlinName(schema: Schema) = "${javaName(schema)}Kt"
    private fun fullJavaName(schema: Schema) = schema.fullName
    private fun fullKotlinName(schema: Schema) = "${fullJavaName(schema)}Kt"
    private fun javaType(schema: Schema) = Class.forName(schema.fullName)

    private fun buildPrimaryConstructor(schema: Schema): FunSpec {
        val primaryConstructorBuilder = FunSpec.constructorBuilder()
        schemaToFieldNamesAndTypes(schema).forEach { (name, type) -> primaryConstructorBuilder.addParameter(name, type) }
        val primaryConstructor = primaryConstructorBuilder.build()
        return primaryConstructor
    }

    private fun schemaToFieldNamesAndTypes(schema: Schema): List<Pair<String, TypeName>> {
        var fieldNamesAndTypes = schema.fields
                .filterNotNull()
                .map { field -> Pair(field.name(), toKotlinType(field.schema())) }
        return fieldNamesAndTypes
    }
}