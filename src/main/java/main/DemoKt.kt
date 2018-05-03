package main

import com.squareup.kotlinpoet.*
import demo.ExamplePerson
import demo.ExamplePersonKt
import org.apache.avro.Schema
import org.apache.avro.specific.SpecificRecord

fun main(args: Array<String>) {
    val schema = SchemaUtils.getSchemaForAvsc("example.avsc")
    generate(schema)

//    stuff()
//    generateFoo()
//    generateBar()
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

private fun generateFoo() {
    val greeterClass = ClassName("", "Greeter")
    val file = FileSpec.builder("", "HelloWorld")
            .addType(TypeSpec.classBuilder("Greeter")
                    .primaryConstructor(FunSpec.constructorBuilder()
                            .addParameter("name", String::class)
                            .build())
                    .addProperty(PropertySpec.builder("name", String::class)
                            .initializer("name")
                            .build())
                    .addFunction(FunSpec.builder("greet")
                            .addStatement("println(%S)", "Hello, \$name")
                            .build())
                    .build())
            .addFunction(FunSpec.builder("main")
                    .addParameter("args", String::class, KModifier.VARARG)
                    .addStatement("%T(args[0]).greet()", greeterClass)
                    .build())
            .build()

    file.writeTo(System.out)
}

private fun generateBar() {
    FileSpec.builder("", "UserKt")
            .addType(TypeSpec.classBuilder("UserKt")
                    .addModifiers(KModifier.DATA)
                    .primaryConstructor(FunSpec.constructorBuilder()
                            .addParameter("id", Int::class.asTypeName())
                            .addParameter("username", String::class.asTypeName().asNullable())
                            .addParameter("wtf", Object::class.asTypeName())
                            .addParameter("maybeWtf", Object::class.asTypeName().asNullable())
                            .build())
                    .addFunction(FunSpec.builder("toSpecificRecord")
//                            .returns(ExamplePerson::class)
                            .addStatement("User(id, username, wtf, maybeWtf)")
                            .build())
                    .companionObject(TypeSpec.companionObjectBuilder("UserKt")
                            .addFunction(FunSpec.builder("fromSpecificRecord")
                                    .addParameter("specificRecord", SpecificRecord::class)
                                    .addStatement("UserKt(" +
                                            "id = specificRecord.id!,\n" +
                                            "username = specificRecord.username,\n" +
                                            "wtf = specificRecord.username!,\n" +
                                            "maybeWtf = specificRecord)")
//                                    .addStatement("println(%S)", "Hello, world")
                                    .build())
                            .build())
                    .build()
            )
            .build()
            .writeTo(System.out)
}


private fun stuff() {
    println("asdf")

    val javaPerson = ExamplePerson(
            Int.MAX_VALUE, //id = 42,
            null, //username = "user1",
            null,
            null)
    println("javaPerson = ${javaPerson}")

    val kotlinPerson = ExamplePersonKt.toKotlin(examplePerson = javaPerson)
    println("kotlinPerson = ${kotlinPerson}")
}
