package main

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import io.confluent.kafka.serializers.KafkaAvroDeserializer
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig
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

object KotlinAvroConverterCache {
    val converters: MutableMap<String, KotlinAvroConverter<Any, SpecificRecord>> = mutableMapOf()

    fun converterFor(fullyQualifiedSchemaName: String): KotlinAvroConverter<Any, SpecificRecord> {
        if (converters.containsKey(fullyQualifiedSchemaName)) {
            return converters.getValue(fullyQualifiedSchemaName)
        }

        val parts = fullyQualifiedSchemaName.split(".").asReversed()
        val partialList = parts.drop(1)
        val converterName = listOf("${parts[0]}Converter", "converter", *partialList.toTypedArray())
                .asReversed()
                .joinToString(separator = ".")
        val converterClass = Class.forName(converterName)
        val converter = converterClass.getConstructor().newInstance() as KotlinAvroConverter<Any, SpecificRecord>
        converters.put(fullyQualifiedSchemaName, converter)

        return converter
    }
}

class KotlinAvroDeserializer<T> : Deserializer<T> {
    var schemaRegistryClient: CachedSchemaRegistryClient? = null
    var inner: KafkaAvroDeserializer? = null

//    val specificAvroDeserializer = SpecificAvroDeserializer<SpecificRecord>()

    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
        val mutableConfigs = configs as MutableMap<String, Any>
        mutableConfigs.put("specific.avro.reader", true)

        val config = KafkaAvroDeserializerConfig(configs)

        val urls = config.getSchemaRegistryUrls()
        val maxSchemaObject = config.getMaxSchemasPerSubject()
        this.schemaRegistryClient = CachedSchemaRegistryClient(urls, maxSchemaObject);
        this.inner = KafkaAvroDeserializer(schemaRegistryClient, configs)
    }

//    fun withSpecificAvroEnabled(config: Map<String, *>?): Map<String, Any> {
//        val specificAvroEnabledConfig = if (config == null) mutableMapOf<String, Any>() else mutableMapOf<String, Any>(config)
//        specificAvroEnabledConfig["specific.avro.reader"] = true
//        return specificAvroEnabledConfig
//    }


    override fun deserialize(topic: String?, data: ByteArray?): T {
//        val specificRecord: SpecificRecord = specificAvroDeserializer.deserialize(topic, data)
        val specificRecord = this.inner?.deserialize(topic, data) as SpecificRecord


//        val allSubjects = this.schemaRegistryClient?.allSubjects
//        println("schemaRegistryClient = ${schemaRegistryClient}")

//        val converter = KotlinAvroConverterCache.converterFor(topic!!)
//        val fullyQualifiedConverterName = "${specificRecord.schema.namespace}.converter.${specificRecord.schema.name}Converter"
        val fullyQualifiedSchemaName = "${specificRecord.schema.fullName}"
        val converter = KotlinAvroConverterCache.converterFor(fullyQualifiedSchemaName)
        return converter.fromAvroSpecificRecord(specificRecord) as T
    }

    override fun close() {
//        specificAvroDeserializer.close()
        this.inner?.close()
    }
}

class KotlinAvroSerializer<T> : Serializer<T> {
    val specificAvroSerializer = SpecificAvroSerializer<SpecificRecord>()

    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
        specificAvroSerializer.configure(configs, isKey)
    }

    override fun serialize(topic: String?, data: T): ByteArray {
        val classname = (data as Any).javaClass.name.replace(regex = Regex("Kt$"), replacement = "")
        val converter = KotlinAvroConverterCache.converterFor(classname)
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