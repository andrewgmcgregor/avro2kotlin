package main

import demo.Example
import demo.ExampleEnumKt
import demo.ExampleKt
import demo.ExampleNestingKt
import demo.converter.ExampleConverter
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*

val PRODUCER_OUTPUT_TOPIC = "examplesFromProducer"

fun main(args: Array<String>) {
    AvroBasedExampleProducer.runProducer(3, PRODUCER_OUTPUT_TOPIC, 2)
}

object AvroBasedExampleProducer {
    fun runProducer(sendMessageCount: Int,
                    outputTopic: String,
                    colourOffset: Int) {
        val producer = createProducer()
        val time = System.currentTimeMillis()

        try {
            val limit = 0
            for (index in limit until limit + sendMessageCount) {
                val favoriteColour = Arrays.asList("red", "green", "blue")[(index + colourOffset) % 3]
                var exampleKt = ExampleKt(
                        id = index.toLong(),
                        exampleNesting = ExampleNestingKt(index == 0),
                        my_enum = ExampleEnumKt.FOO,
                        my_nested_member = if (index == 0) null else ExampleNestingKt(true),
                        guid = "name-$favoriteColour")
                val record = ProducerRecord(
                        outputTopic,
                        "" + index,
                        ExampleConverter.toAvroSpecificRecord(exampleKt))

                val metadata = producer.send(record).get()

                val elapsedTime = System.currentTimeMillis() - time
                System.out.printf("sent record(key=%s value=%s) meta(partition=%d, offset=%d) time=%d\n",
                        record.key(), record.value(), metadata.partition(), metadata.offset(), elapsedTime)
            }
        } finally {
            producer.flush()
            producer.close()
        }
    }

    private fun createProducer(): Producer<String, Example> {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "http://localhost:9092"
        props["schema.registry.url"] = "http://localhost:8081"
        props[ProducerConfig.CLIENT_ID_CONFIG] = "KafkaExampleProducer"
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java.name
        return KafkaProducer(props)
    }
}
