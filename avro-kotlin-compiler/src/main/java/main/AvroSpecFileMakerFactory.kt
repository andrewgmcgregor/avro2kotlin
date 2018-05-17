package main

import com.squareup.kotlinpoet.FileSpec

object AvroSpecFileMakerFactory {
    fun newInstance(skinnyAvroFileSpec: SkinnyAvroFileSpec, fileSpec: FileSpec) = FileMaker(
            relativePath = skinnyAvroFileSpec.namespace.replace(".", "/"),
            fileName = "${skinnyAvroFileSpec.name}.kt",
            content = extractContent(fileSpec)
    )
}