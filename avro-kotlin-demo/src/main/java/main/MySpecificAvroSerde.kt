package io.confluent.kafka.streams.serdes.avro

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import org.apache.avro.specific.SpecificRecord
import org.apache.kafka.common.annotation.InterfaceStability
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes.serdeFrom
import org.apache.kafka.common.serialization.Serializer

@InterfaceStability.Unstable
class MySpecificAvroSerde<T : SpecificRecord> : Serde<T> {
    private val inner: Serde<T>

    constructor() {
        this.inner = serdeFrom<T>(SpecificAvroSerializer(), SpecificAvroDeserializer())
    }

    internal constructor(client: SchemaRegistryClient?) {
        if (client == null) {
            throw IllegalArgumentException("schema registry client must not be null")
        } else {
            this.inner = serdeFrom<T>(SpecificAvroSerializer(client), SpecificAvroDeserializer(client))
        }
    }

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
