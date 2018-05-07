package main

fun main(args: Array<String>) {
    val schemas = mutableListOf(SchemaUtils.getSchemaForAvsc("src/test/resources/example.avsc"))
    schemas.addAll(SchemaUtils.getSchemasForAvdl("src/test/resources/example.avdl"))

    schemas
            .map { schema -> KotlinGenerator.generate(schema) }
            .forEach { fileSpec -> fileSpec.writeTo(System.out) }
}
