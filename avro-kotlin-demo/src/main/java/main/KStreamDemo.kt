package main

import org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig.*
import org.apache.kafka.streams.kstream.ForeachAction
import java.util.*

fun main(args: Array<String>) {
    val config = Properties()
    config[APPLICATION_ID_CONFIG] = "kstream-demo-" + System.currentTimeMillis()
    config[BOOTSTRAP_SERVERS_CONFIG] = "localhost:9092"
    config[AUTO_OFFSET_RESET_CONFIG] = "earliest"
    config[DEFAULT_KEY_SERDE_CLASS_CONFIG] = Serdes.String()::class.java
    config[DEFAULT_VALUE_SERDE_CLASS_CONFIG] = Serdes.String()::class.java //SpecificAvroSerde::class.java
    config[CACHE_MAX_BYTES_BUFFERING_CONFIG] = "0"

    val builder = StreamsBuilder()
    val stream = builder.stream<String, String>("foo")

    stream.foreach(StringStringPrinter())

    KafkaStreams(builder.build(), config).start()
}

class StringStringPrinter : ForeachAction<String, String> {
    override fun apply(key: String?, value: String?) {
        println("${key} = ${value}")
    }
}