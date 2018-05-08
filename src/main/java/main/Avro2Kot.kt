package main

import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream

fun main(args: Array<String>) {
    if (args.size == 0) {
        val name = "avro2kotlin"
        System.err.println("$name - Generates a Java and Kotlin files from an Avro schema")
        System.err.println("Usage: $name [schema-file]")
        System.exit(1)
    }

    val parseOut = PrintStream(FileOutputStream("GeneratedCode.kt"))
    main.KotlinGenerator.generate(File(args[0]), parseOut)
}
