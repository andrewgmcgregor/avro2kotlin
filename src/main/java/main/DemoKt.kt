package main

import com.squareup.kotlinpoet.*
import demo.ExamplePerson
import demo.ExamplePersonKt
import org.apache.avro.specific.SpecificRecord

fun main(args: Array<String>) {
//    stuff()
    generateFoo()
    generateBar()
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

