package main

import demo.ExamplePerson
import demo.ExamplePersonKt
import io.confluent.kafka.streams.serdes.avro.SpecificAvroDeserializer
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.Serializer

interface KotlinAvroConverter<K, A : SpecificRecord> {
    fun toAvroSpecificRecord(k: K): A
    fun fromAvroSpecificRecord(a: A): K
}

//fun createExamplePersonKotlinAvroConverter(): KotlinAvroConverter<ExamplePersonKt, ExamplePerson> {
//    return KotlinAvroConverter<ExamplePersonKt, ExamplePerson>() {
//        fun toAvroSpecificRecord(k: ExamplePersonKt): ExamplePerson = TODO()
//        fun fromAvroSpecificRecord(a: ExamplePerson): ExamplePersonKt = TODO()
//    }
//}

class ExamplePersonKotlinAvroConverter : KotlinAvroConverter<ExamplePersonKt, ExamplePerson> {
    override fun toAvroSpecificRecord(examplePersonKt: ExamplePersonKt) = ExamplePerson(examplePersonKt.id, examplePersonKt.username)
    override fun fromAvroSpecificRecord(examplePerson: ExamplePerson) = ExamplePersonKt(id = examplePerson.id, username = if (examplePerson.username == null) null else examplePerson.username)
}

object KotlinAvroConverterCache {
    val converters: MutableMap<String, KotlinAvroConverter<Any, SpecificRecord>> = mutableMapOf()

    fun converterFor(fullyQualifiedSchemaName: String): KotlinAvroConverter<Any, SpecificRecord> {
        if (converters.containsKey(fullyQualifiedSchemaName)) {
            return converters.getValue(fullyQualifiedSchemaName)
        }

        val converterClass = Class.forName("${fullyQualifiedSchemaName}KotlinAvroConverter")
        val converter = converterClass.getConstructor().newInstance() as KotlinAvroConverter<Any, SpecificRecord>
        converters.put(fullyQualifiedSchemaName, converter)

        return converter
    }
}

class KotlinAvroDeserializer<T> : Deserializer<T> {
    val specificAvroDeserializer = SpecificAvroDeserializer<SpecificRecord>()

    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
        specificAvroDeserializer.configure(configs, isKey)
    }

    override fun deserialize(topic: String?, data: ByteArray?): T {
//        specificAvroDeserializer.

        val specificRecord: SpecificRecord = specificAvroDeserializer.deserialize(topic, data)
        val converter = KotlinAvroConverterCache.converterFor(topic!!)
        return converter.fromAvroSpecificRecord(specificRecord) as T
    }

    override fun close() {
        specificAvroDeserializer.close()
    }
}

class KotlinAvroSerializer<T> : Serializer<T> {
    val specificAvroSerializer = SpecificAvroSerializer<SpecificRecord>()

    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
        specificAvroSerializer.configure(configs, isKey)
    }

    override fun serialize(topic: String?, data: T): ByteArray {
        val converter = KotlinAvroConverterCache.converterFor(topic!!)
        val specificRecord = converter.toAvroSpecificRecord(data as Any)
        return specificAvroSerializer.serialize(topic, specificRecord)
    }

    override fun close() {
        specificAvroSerializer.close()
    }
}

class KotlinAvroSerde<T> : Serde<T> {

    private val inner = Serdes.serdeFrom<T>(KotlinAvroSerializer(), KotlinAvroDeserializer())

    override fun serializer(): Serializer<T> {
        return this.inner.serializer()
    }

    override fun deserializer(): Deserializer<T> {
        return this.inner.deserializer()
    }

    override fun configure(serdeConfig: Map<String, *>, isSerdeForRecordKeys: Boolean) {
        this.inner.serializer().configure(serdeConfig, isSerdeForRecordKeys)
        this.inner.deserializer().configure(serdeConfig, isSerdeForRecordKeys)
    }

    override fun close() {
        this.inner.serializer().close()
        this.inner.deserializer().close()
    }

}