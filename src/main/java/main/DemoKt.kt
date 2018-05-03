package main

import com.squareup.kotlinpoet.*
import org.apache.avro.Schema

fun main(args: Array<String>) {
    val schema = SchemaUtils.getSchemaForAvsc("example.avsc")
    generate(schema)
}

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

private fun generate(schema: Schema) {
    var javaName = schema.name
    var fullJavaName = schema.fullName
    var kotlinName = "${schema.name}Kt"
    var fullKotlinName = "${fullJavaName}Kt"

    val primaryConstructor = FunSpec.constructorBuilder()
    var fieldNamesAndTypes = schema.fields
            .filterNotNull()
            .map { field -> Pair(field.name(), toKotlinType(field.schema())) }
    fieldNamesAndTypes.forEach { (name, type) -> primaryConstructor.addParameter(name, type) }

    var fieldNames = schema.fields.map { it.name() }
    val fieldList = fieldNames.joinToString(prefix = "(", separator = ", ", postfix = ")")
    val fromAvroSpecificRecordParameterName = "${schema.name.decapitalize()}"
    val kotlinConstructorFieldList = fieldNamesAndTypes
            .map { (name, type) ->
                val suffix = if (type.nullable) "!!" else ""
                "${name} = ${fromAvroSpecificRecordParameterName}.${name}${suffix}"
            }
            .joinToString(prefix = "(", separator = ", ", postfix = ")")

    var javaType = Class.forName(schema.fullName)

    val fromAvroSpecificRecord = FunSpec.builder("fromAvroSpecificRecord")
            .returns(Class.forName(fullKotlinName))
            .addParameter(fromAvroSpecificRecordParameterName, javaType)
            .addStatement("${kotlinName}${kotlinConstructorFieldList}")

    FileSpec.builder("", kotlinName)
            .addType(TypeSpec.classBuilder(kotlinName)
                    .addModifiers(KModifier.DATA)
                    .primaryConstructor(primaryConstructor.build())
                    .addFunction(FunSpec.builder("toAvroSpecificRecord")
                            .returns(javaType)
                            .addStatement("${javaName}${fieldList}")
                            .build())
                    .companionObject(TypeSpec.companionObjectBuilder(kotlinName)
                            .addFunction(fromAvroSpecificRecord.build())
                            .build())
                    .build()
            )
            .build()
            .writeTo(System.out)
}
