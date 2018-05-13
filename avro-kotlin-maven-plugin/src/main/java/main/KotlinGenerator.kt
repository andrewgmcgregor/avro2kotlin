package main

import com.squareup.kotlinpoet.*
import org.apache.avro.Schema
import org.apache.avro.compiler.idl.Idl
import java.io.File
import java.io.InputStream

object KotlinGenerator {
    fun generateFromFile(schemaFilename: String): FileSpec {
        val file = File(schemaFilename)
        val inputStream = file.inputStream()
        inputStream.use {
            when {
                schemaFilename.endsWith(".avsc") -> return generateFromAvsc(inputStream)
                schemaFilename.endsWith(".avdl") -> return generateFromAvdl(inputStream)
                else -> throw IllegalArgumentException("expected file ending in .avsc or .avdl")
            }
        }
    }

    fun generateFromAvsc(inputStream: InputStream): FileSpec {
        val schema = Schema.Parser().parse(inputStream)
        val fileSpec = generateFromSchemas(listOf(schema), schema.namespace, schema.name)
        return fileSpec
    }

    fun generateFromAvdl(inputStream: InputStream): FileSpec {
        val compilationUnit = Idl(inputStream).CompilationUnit()
        val schemas = compilationUnit.types
        val fileSpec = generateFromSchemas(schemas, compilationUnit.namespace, compilationUnit.name)
        return fileSpec
    }

    fun generateFromSchemas(schemas: Collection<Schema>,
                            namespace: String,
                            name: String): FileSpec {
        val builder = FileSpec.builder(namespace, name)
        schemas.forEach { schema ->
            if (schema.type == Schema.Type.RECORD) {
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
            } else if (schema.type == Schema.Type.ENUM) {
                val javaEnumName = javaName(schema)
                val enumName = kotlinName(schema)
                val enumBuilder = TypeSpec.enumBuilder(enumName)
                schema.enumSymbols.forEach { symbol -> enumBuilder.addEnumConstant(symbol) }
                builder.addType(enumBuilder
                        .addFunction(FunSpec.builder("toAvroSpecificRecord")
                                .addStatement("return $javaEnumName.valueOf(this.name)")
                                .build())
                        .build())

                val fromAvroSpecificRecordParameterName = schema.name.decapitalize()
                builder.addType(TypeSpec.objectBuilder(enumName)
                        .addFunction(FunSpec.builder("fromAvroSpecificRecord")
                                .addParameter(fromAvroSpecificRecordParameterName, javaType(schema))
                                .addStatement("return ${kotlinName(schema)}.valueOf(${fromAvroSpecificRecordParameterName}.name)")
                                .build()
                        )
                        .build())

            }
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

        if (schema.type == Schema.Type.ENUM) {
            val kotlinType = kotlinType(schema)
            return MinimalTypeSpec(
                    kotlinType = kotlinType,
                    avroType = true
            )
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
        return FunSpec.builder("fromAvroSpecificRecord")
                .addParameter(fromAvroSpecificRecordParameterName, javaType(schema))
                .addStatement("return ${kotlinName(schema)}${kotlinConstructorFieldList}")
                .build()
    }

    private fun javaName(schema: Schema) = schema.name
    private fun kotlinName(schema: Schema) = "${javaName(schema)}Kt"
    private fun javaType(schema: Schema): TypeName {
        val className = ClassName(schema.namespace, schema.name)
        return className
    }

    private fun kotlinType(schema: Schema): TypeName {
        val kotlinName = "${schema.name}Kt"
        val className = ClassName(schema.namespace, kotlinName)
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