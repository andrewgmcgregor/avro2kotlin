package main

import com.squareup.kotlinpoet.*
import org.apache.avro.Schema
import org.apache.avro.compiler.idl.Idl
import java.io.File
import java.io.InputStream
import java.io.PrintStream

object KotlinGenerator {
    fun generateFromFile(schemaFilename: String, printStream: PrintStream) {
        val file = File(schemaFilename)
        val inputStream = file.inputStream()
        inputStream.use {
            when {
                schemaFilename.endsWith(".avsc") -> generateFromAvsc(inputStream, printStream)
                schemaFilename.endsWith(".avdl") -> generateFromAvdl(inputStream, printStream)
            }
        }
    }

    fun generateFromAvsc(inputStream: InputStream, printStream: PrintStream) {
        val schema = Schema.Parser().parse(inputStream)
        generateFromSchema(listOf(schema), schema.namespace, schema.name)
                .writeTo(printStream)
    }

    fun generateFromAvdl(inputStream: InputStream, printStream: PrintStream) {
        val compilationUnit = Idl(inputStream).CompilationUnit()
        val schemas = compilationUnit.types
        val fileSpec = generateFromSchema(schemas, compilationUnit.namespace, compilationUnit.name)
        fileSpec.writeTo(printStream)
    }

    fun generateFromSchema(schemas: Collection<Schema>, namespace: String, name: String): FileSpec {
        val builder = FileSpec.builder(namespace, name)
        schemas.forEach { schema ->
            val fileName = kotlinName(schema)
            builder.addType(TypeSpec.classBuilder(fileName)
                    .addModifiers(KModifier.DATA)
                    .primaryConstructor(buildPrimaryConstructor(schema))
                    .addProperties(buildPropertySpecs(schema))
                    .addFunction(buildConverterToAvro(schema))
                    .companionObject(TypeSpec.companionObjectBuilder(fileName)
                            .addFunction(buildConverterFromAvro(schema))
                            .build())
                    .build())
        }
        return builder.build()
    }

    private fun buildPropertySpecs(schema: Schema): Iterable<PropertySpec> {
        return schemaToFieldNamesAndTypes(schema)
                .map { minimalFieldSpec ->
                    PropertySpec
                            .builder(name = minimalFieldSpec.name, type = minimalFieldSpec.minimalTypeSpec.kotlinType)
                            .initializer(minimalFieldSpec.name)
                            .build()
                }
    }

    private fun toKotlinType(schema: Schema): MinimalTypeSpec {
        if (isSimpleKotlinType(schema.type)) {
            return MinimalTypeSpec(
                    kotlinType = toSimpleKotlinType(schema.type)!!,
                    avroType = false)
        }

        if (schema.type == Schema.Type.UNION) {
            var illegalArgumentMessage = "unions of a single type and null (e.g. union { null, MyRecord })"

            val innerTypes = schema.types.map { it.type }
            val containsNull = innerTypes.contains(Schema.Type.NULL)
            if (!containsNull) {
                throw IllegalArgumentException(illegalArgumentMessage)
            }

            val typesOtherThanNull = innerTypes.filterNot { it == Schema.Type.NULL }
            if (typesOtherThanNull.size > 1) {
                throw IllegalArgumentException(illegalArgumentMessage)
            }

            var type = typesOtherThanNull.get(0)
            if (isSimpleKotlinType(type)) {
                return MinimalTypeSpec(
                        kotlinType = toSimpleKotlinType(type)!!.asNullable(),
                        avroType = false)
            }

            val innerSchema = schema.types
                    .filter { it.type != Schema.Type.NULL }
                    .first()
            val innerMinimalTypeSpec = toKotlinType(innerSchema)
            return innerMinimalTypeSpec
                    .copy(kotlinType = innerMinimalTypeSpec.kotlinType.asNullable())
        }

        if (schema.type == Schema.Type.RECORD) {
            return MinimalTypeSpec(
                    kotlinType = kotlinType(schema),
                    avroType = true)
        }

        throw IllegalArgumentException(schema.type.getName());
    }

    private fun isSimpleKotlinType(type: Schema.Type): Boolean = toSimpleKotlinType(type) != null

    private fun toSimpleKotlinType(type: Schema.Type): TypeName? {
        val typeName = when (type) {
            Schema.Type.STRING -> String::class
            Schema.Type.INT -> Int::class
            Schema.Type.FLOAT -> Float::class
            Schema.Type.DOUBLE -> Double::class
            Schema.Type.LONG -> Long::class
            Schema.Type.BOOLEAN -> Boolean::class
            else -> null
        }
        return typeName?.asTypeName()
    }

    private fun buildConverterToAvro(schema: Schema): FunSpec {
        var argList = schemaToFieldNamesAndTypes(schema)
                .map { it.name + if (it.minimalTypeSpec.avroType) "${if (it.minimalTypeSpec.kotlinType.nullable) "?" else ""}.toAvroSpecificRecord()" else "" }
                .joinToString(prefix = "(", separator = ", ", postfix = ")")
        val buildConverterToAvro = FunSpec.builder("toAvroSpecificRecord")
                .addStatement("return ${javaName(schema)}${argList}")
                .build()
        return buildConverterToAvro
    }

    private fun buildConverterFromAvro(schema: Schema): FunSpec {
        val fromAvroSpecificRecordParameterName = schema.name.decapitalize()
        val kotlinConstructorFieldList = schemaToFieldNamesAndTypes(schema)
                .map {
                    var param = "${fromAvroSpecificRecordParameterName}.${it.name}"
                    "${it.name} = " +
                            "${if (it.minimalTypeSpec.kotlinType.nullable) "if (${param} == null) null else " else ""}" +
                            "${if (it.minimalTypeSpec.avroType) "${it.minimalTypeSpec.kotlinType.asNonNullable()}.fromAvroSpecificRecord(" else ""}" +
                            param +
                            "${if (it.minimalTypeSpec.avroType) ")" else ""}"
                }
                .joinToString(prefix = "(", separator = ", ", postfix = ")")
        val fromAvroSpecificRecordBuilder = FunSpec.builder("fromAvroSpecificRecord")
                .addParameter(fromAvroSpecificRecordParameterName, javaType(schema))
                .addStatement("return ${kotlinName(schema)}${kotlinConstructorFieldList}")
        val fromAvroSpecificRecordFunction = fromAvroSpecificRecordBuilder.build()
        return fromAvroSpecificRecordFunction
    }

    private fun javaName(schema: Schema) = schema.name
    private fun kotlinName(schema: Schema) = "${javaName(schema)}Kt"
    private fun javaType(schema: Schema): TypeName {
        println("schema.namespace = ${schema.namespace}")
        println("schema.namespace = ${schema.name}")
        val className = ClassName(schema.namespace, schema.name)
        println("className = ${className}")
        return className
    }
    private fun kotlinType(schema: Schema): TypeName {
        println("schema.namespace = ${schema.namespace}")
        val kotlinName = "${schema.name}Kt"
        println("kotlinName = ${kotlinName}")
        val className = ClassName(schema.namespace, kotlinName)
        println("className = ${className}")
        return className
    }

    private fun buildPrimaryConstructor(schema: Schema): FunSpec {
        val primaryConstructorBuilder = FunSpec.constructorBuilder()
        schemaToFieldNamesAndTypes(schema).forEach { minimalFieldSpec ->
            primaryConstructorBuilder.addParameter(minimalFieldSpec.name, minimalFieldSpec.minimalTypeSpec.kotlinType)
        }
        val primaryConstructor = primaryConstructorBuilder.build()
        return primaryConstructor
    }

    private fun schemaToFieldNamesAndTypes(schema: Schema): List<MinimalFieldSpec> {
        return schema.fields
                .filterNotNull()
                .map { field ->
                    MinimalFieldSpec(
                            name = field.name(),
                            minimalTypeSpec = toKotlinType(field.schema()))
                }
    }

    private data class MinimalTypeSpec(val kotlinType: TypeName,
                                       val avroType: Boolean)

    private data class MinimalFieldSpec(val name: String,
                                        val minimalTypeSpec: MinimalTypeSpec)
}