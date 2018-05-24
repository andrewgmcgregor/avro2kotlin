package main

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import org.apache.avro.Schema
import org.apache.avro.compiler.idl.Idl
import java.io.File
import java.io.InputStream
import main.SkinnyAvroFileSpec
import main.MinimalFieldSpec
import main.MinimalTypeSpec
import main.SkinnySchemaSpec

object SkinnySchemaSpecBuilder {
    fun generateFromFile(schemaFilename: String): main.SkinnyAvroFileSpec {
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

    fun generateFromAvsc(inputStream: InputStream): SkinnyAvroFileSpec {
        val schema = Schema.Parser().parse(inputStream)
        SkinnySchemaSpec(
                namespace = schema.namespace,
                name = schema.name,
                type = schema.type,
                fields = schemaToFieldNamesAndTypes(schema)
        )

        return SkinnyAvroFileSpec(
                namespace = schema.namespace,
                name = schema.name,
                schemaSpecs = listOf(SkinnySchemaSpec(
                        namespace = schema.namespace,
                        name = schema.name,
                        type = schema.type,
                        fields = schemaToFieldNamesAndTypes(schema)
                ))
        )
    }

    fun generateFromAvdl(inputStream: InputStream): SkinnyAvroFileSpec {
        val compilationUnit = Idl(inputStream).CompilationUnit()

        val schemas = compilationUnit.types
        val schemaSpecs: List<SkinnySchemaSpec> = schemas.map { schema ->
            SkinnySchemaSpec(
                    namespace = schema.namespace,
                    name = schema.name,
                    type = schema.type,
                    fields = schemaToFieldNamesAndTypes(schema)
            )
        }

        return SkinnyAvroFileSpec(
                namespace = compilationUnit.namespace,
                name = compilationUnit.name,
                schemaSpecs = schemaSpecs
        )
    }

    private fun schemaToFieldNamesAndTypes(schema: Schema): List<MinimalFieldSpec> {
        when (schema.type) {
            Schema.Type.RECORD -> return schema.fields
                    .filterNotNull()
                    .map { field ->
                        MinimalFieldSpec(
                                name = field.name(),
                                minimalTypeSpec = toKotlinType(field.schema()))
                    }
            Schema.Type.ENUM -> return schema.enumSymbols
                    .filterNotNull()
                    .map { enumSymbol -> MinimalFieldSpec(name = enumSymbol, minimalTypeSpec = null) }
            else -> throw IllegalArgumentException("schemaToFieldNamesAndTypes does not support ${schema.type}")
        }
    }

    private fun toKotlinType(schema: Schema): MinimalTypeSpec {
        if (isSimpleKotlinType(schema.type)) {
            val kotlinType = toSimpleKotlinType(schema.type)!! as ClassName
            return MinimalTypeSpec(
                    namespace = kotlinType.packageName(),
                    name = kotlinType.simpleName(),
                    kotlinType = kotlinType,
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
            if (isSimpleKotlinType(schema.type)) {
                val kotlinType = toSimpleKotlinType(schema.type)!! as ClassName
                return MinimalTypeSpec(
                        namespace = kotlinType.packageName(),
                        name = kotlinType.simpleName(),
                        kotlinType = kotlinType,
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
                    namespace = schema.namespace,
                    name = schema.name,
                    kotlinType = kotlinType(schema),
                    avroType = true)
        }

        if (schema.type == Schema.Type.ENUM) {
            val kotlinType = kotlinType(schema)
            return MinimalTypeSpec(
                    namespace = schema.namespace,
                    name = schema.name,
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

    fun kotlinType(schema: Schema) = ClassName(schema.namespace, "${schema.name}Kt")
}