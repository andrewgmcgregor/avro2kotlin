package main

import demo.*
import org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig.*
import java.util.*

val EXAMPLE_PERSON_KSTREAM_OUTPUT_TOPIC = "examplePersonFromKStream"

fun main(args: Array<String>) {
    val config = Properties()
    config[APPLICATION_ID_CONFIG] = "avro-stream-processor-" + System.currentTimeMillis()
    config[BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
    config[AUTO_OFFSET_RESET_CONFIG] = "earliest"
    config["schema.registry.url"] = "http://localhost:8081"
    config[DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String().javaClass
    config[DEFAULT_VALUE_SERDE_CLASS_CONFIG] = KotlinAvroSerde::class.java
    config[CACHE_MAX_BYTES_BUFFERING_CONFIG] = "0"

    val builder = StreamsBuilder()

    builder.stream<String, ExamplePersonKt>(EXAMPLE_PERSON_PRODUCER_OUTPUT_TOPIC)
            .peek { key, value -> println("${key} = ${value}") }
            .mapValues { it.copy(username = it.username + "asfasfasd") }
            .to(EXAMPLE_PERSON_KSTREAM_OUTPUT_TOPIC)

    val kafkaStreams = KafkaStreams(builder.build(), config)
    kafkaStreams.start()
}