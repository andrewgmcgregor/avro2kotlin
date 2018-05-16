package main

import com.squareup.kotlinpoet.*
import org.apache.avro.Schema

interface KotlinAvroConverter<K, A> {
}

object DataClassConverterGenerator {
    fun generateFrom(spec: SkinnyAvroFileSpec): FileSpec {
        val builder = FileSpec.builder(spec.namespace, spec.name)
        spec.schemaSpecs.forEach { schemaSpec ->
            if (schemaSpec.type == Schema.Type.RECORD) {
                val fileName = "${schemaSpec.name}Converter"
                val superclass: ParameterizedTypeName = ParameterizedTypeName.get(
                        rawType = ClassName("main", "KotlinAvroConverter"),
                        typeArguments = *arrayOf(
                                ClassName(schemaSpec.namespace, "${schemaSpec.name}Kt"),
                                ClassName(schemaSpec.namespace, schemaSpec.name)
                        )
                )
                builder.addType(TypeSpec.classBuilder(fileName)
                        .addSuperinterface(superinterface = superclass)
                        .build())
            }
        }
        return builder.build()
    }
}