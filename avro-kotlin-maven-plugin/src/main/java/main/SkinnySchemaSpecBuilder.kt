package main

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import org.apache.avro.Schema
import org.apache.avro.compiler.idl.Idl
import java.io.InputStream

object SkinnySchemaSpecBuilder {
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
        return schema.fields
                .filterNotNull()
                .map { field ->
                    MinimalFieldSpec(
                            name = field.name(),
                            minimalTypeSpec = toKotlinType(field.schema()))
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

    fun kotlinType(schema: Schema)
        = ClassName(schema.namespace, "${schema.name}Kt")
}