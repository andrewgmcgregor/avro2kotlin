package main

import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.PrintStream
import java.util.*

@Throws(Exception::class)
fun main(args: Array<String>) {
    Avro2Kot.run(
            System.`in`,
            System.out,
            System.err,
            Arrays.asList(*args)
    )
}

object Avro2Kot {
    val name = "avro2kot"

    val shortDescription = "Generates an Kotlin file from an Avro schema"

    @Throws(Exception::class)
    fun run(input: InputStream,
            out: PrintStream,
            err: PrintStream,
            args: List<String>): Int {

        var parseOut = out

        if (args.size > 3 || args.size == 0 || !"generate".equals(args.get(0))) {
            err.println("${name} - ${shortDescription}")
            err.println("Usage: ${name} generate [in] [out]")
            err.println("")
            err.println("If an output path is not specified, outputs to stdout.")
            err.println("If no input or output is specified, takes input from")
            err.println("stdin and outputs to stdin.")
            err.println("The special path \"-\" may also be specified to refer to")
            err.println("stdin and stdout.")
            return -1
        }

        if (args.size == 3 && "-" != args[2]) {
            parseOut = PrintStream(FileOutputStream(args[2]))
        }

        if (args.size >= 2 && "-" != args[1]) {
            main.KotlinGenerator.generate(File(args[1]), parseOut)
        } else {
            main.KotlinGenerator.generate(input, parseOut)
        }

        return 0
    }
}
