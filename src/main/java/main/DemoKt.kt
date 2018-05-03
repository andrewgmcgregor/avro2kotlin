package main

import com.squareup.kotlinpoet.*
import demo.ExamplePerson
import demo.ExamplePersonKt
import org.apache.avro.Schema
import org.apache.avro.specific.SpecificRecord

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
    var kotlinName = "${schema.name}Kt"

    val primaryConstructor = FunSpec.constructorBuilder()
    schema.fields.filterNotNull().forEach { field ->
        primaryConstructor.addParameter(
                name = field.name(),
                type = toKotlinType(field.schema()))
    }

    val fieldNames = schema.fields
            .map { it.name() }
            .joinToString(prefix = "(", separator = ", ", postfix = ")")

    FileSpec.builder("", kotlinName)
            .addType(TypeSpec.classBuilder(kotlinName)
                    .addModifiers(KModifier.DATA)
                    .primaryConstructor(primaryConstructor.build())
                    .addFunction(FunSpec.builder("toAvroSpecificRecord")
                            .returns(Class.forName(schema.fullName))
                            .addStatement("${javaName}${fieldNames}")
                            .build())
//                    .companionObject(TypeSpec.companionObjectBuilder(kotlinName)
//                            .addFunction(FunSpec.builder("fromJava")
//                                    .addParameter("specificRecord", SpecificRecord::class)
//                                    .addStatement("UserKt(" +
//                                            "id = specificRecord.id!,\n" +
//                                            "username = specificRecord.username,\n" +
//                                            "wtf = specificRecord.username!,\n" +
//                                            "maybeWtf = specificRecord)")
//                                    .build())
//                            .build())
                    .build()
            )
            .build()
            .writeTo(System.out)
}
