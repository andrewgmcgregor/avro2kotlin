package net.avro2kotlin.compiler

import com.squareup.kotlinpoet.FileSpec

fun extractContent(fileSpec: FileSpec): String {
    val builder = StringBuilder()
    fileSpec.writeTo(builder)
    val fileContent = builder.toString()
    return fileContent
}