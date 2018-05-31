package net.avro2kotlin.compiler

import com.squareup.kotlinpoet.TypeName
import org.apache.avro.Schema

data class SkinnyAvroFileSpec(val namespace: String,
                              val name: String,
                              val schemaSpecs: List<SkinnySchemaSpec>)

data class SkinnySchemaSpec(val namespace: String,
                            val name: String,
                            val type: Schema.Type,
                            val fields: List<MinimalFieldSpec> = listOf())

data class MinimalFieldSpec(val name: String,
                            val minimalTypeSpec: MinimalTypeSpec?)

data class MinimalTypeSpec(val namespace: String,
                           val name: String,
                           val kotlinType: TypeName,
                           val avroType: Boolean)
