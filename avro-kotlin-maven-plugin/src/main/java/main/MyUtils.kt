package main

import com.squareup.kotlinpoet.FileSpec

fun extractContent(fileSpec: FileSpec): String {
    val builder = StringBuilder()
    fileSpec.writeTo(builder)
    val fileContent = builder.toString()
    return fileContent
}