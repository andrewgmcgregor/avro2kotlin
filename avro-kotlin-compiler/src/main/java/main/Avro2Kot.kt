package main

import java.io.FileOutputStream
import java.io.PrintStream

fun main(args: Array<String>) {
    if (args.size == 0) {
        val name = "Avro2Kot"
        System.err.println("$name - Generates a Java and Kotlin files from an Avro schema")
        System.err.println("Usage: Avro2Kot <schema-file>")
        System.exit(1)
    }

    val schemaFilename = args[0]
    val fileSpec = main.KotlinGenerator.generateFromFile(schemaFilename)

    val parseOut = PrintStream(FileOutputStream(fileSpec.name))
    fileSpec.writeTo(parseOut)
}
