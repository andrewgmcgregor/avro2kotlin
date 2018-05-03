package main

import demo.ExamplePerson
import demo.ExamplePersonKt

fun main(args: Array<String>) {
    build()
    run()
}

private fun build() {
    val schema = SchemaUtils.getSchemaForAvsc("example.avsc")
    var generatedFileSpec = main.KotlinGenerator.generate(schema)
    generatedFileSpec.writeTo(System.out)
}

private fun run() {
    thereAndBackAgain(ExamplePerson(42, "user1"))
    thereAndBackAgain(ExamplePerson(99, null))
//    thereAndBackAgain(ExamplePerson(null, "user2"))
}

private fun thereAndBackAgain(originalAvroSpecificRecord: ExamplePerson) {
    println("originalAvroSpecificRecord = ${originalAvroSpecificRecord}")

    val kotlinDataClass = ExamplePersonKt.fromAvroSpecificRecord(originalAvroSpecificRecord)
    println("kotlinDataClass = ${kotlinDataClass}")

    val toAvroSpecificRecord = kotlinDataClass.toAvroSpecificRecord()
    println("toAvroSpecificRecord = ${toAvroSpecificRecord}")

    println()
}