package net.avro2kotlin.compiler

import java.io.File
import java.io.PrintStream

class FileMaker(val relativePath: String,
                val fileName: String,
                val content: String) {

    fun writeFileRelativeTo(outputDirectory: File) {
        val outputDirectoryWithPackage = outputDirectory.getAbsolutePath() + "/" + relativePath

        val outputStream = Utils.tryOrThrow<PrintStream> {
            getOutputStream(fileName, File(outputDirectoryWithPackage))
        }
        outputStream.print(content)
        outputStream.close()
    }

    @Throws(Exception::class)
    private fun getOutputStream(filename: String, outputDirectory: File): PrintStream {
        outputDirectory.mkdirs()
        val outputFile = File(outputDirectory.absolutePath + "/" + filename)
        return PrintStream(outputFile)
    }
}