package main

import io.confluent.kafka.streams.serdes.avro.SpecificAvroDeserializer
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.common.serialization.Serializer

class KotlinAvroSerializer<T> : Serializer<T> {
    val specificAvroSerializer = SpecificAvroSerializer<SpecificRecord>()

    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
        specificAvroSerializer.configure(configs, isKey)
    }

    override fun serialize(topic: String?, data: T): ByteArray {
        TODO("convert to kotlin data class to SpecificRecord")
        val record = null
        specificAvroSerializer.serialize(topic, record)
    }

    override fun close() {
        specificAvroSerializer.close()
    }
}

class KotlinAvroDeserializer<T> : Deserializer<T> {
    val specificAvroDeserializer = SpecificAvroDeserializer<SpecificRecord>()

    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
        specificAvroDeserializer.configure(configs, isKey)
    }

    override fun deserialize(topic: String?, data: ByteArray?): T {
        specificAvroDeserializer.deserialize(topic, data)
        TODO("convert specific record to kotlin data object")
    }

    override fun close() {
        specificAvroDeserializer.close()
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