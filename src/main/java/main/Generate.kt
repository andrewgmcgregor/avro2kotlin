package main

fun main(args: Array<String>) {
    val schemas = mutableListOf(SchemaUtils.getSchemaForAvsc("example.avsc"))
    schemas.addAll(SchemaUtils.getSchemasForAvdl("example.avdl"))

    schemas
            .map { schema -> KotlinGenerator.generate(schema) }
            .forEach { fileSpec -> fileSpec.writeTo(System.out) }
}
