package main

import demo.*
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*

val EXAMPLE_PERSON_PRODUCER_OUTPUT_TOPIC = "examplePersonFromProducer"

fun main(args: Array<String>) {
    ExamplePersonAvroBasedProducer.runProducer(3, EXAMPLE_PERSON_PRODUCER_OUTPUT_TOPIC, 0)
}

object ExamplePersonAvroBasedProducer {
    fun runProducer(sendMessageCount: Int,
                    outputTopic: String,
                    colourOffset: Int) {
        val producer = createProducer()
        val time = System.currentTimeMillis()

        try {
            val limit = 0
            for (index in limit until limit + sendMessageCount) {
                val favoriteColour = Arrays.asList("red", "green", "blue")[(index + colourOffset) % 3]
                var examplePersonKt = ExamplePersonKt(
                        id = index,
                        username = "name-$favoriteColour")
                val record = ProducerRecord(
                        outputTopic,
                        "" + index,
                        examplePersonKt.toAvroSpecificRecord())

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

    private fun createProducer(): Producer<String, ExamplePerson> {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = "http://localhost:9092"
        props["schema.registry.url"] = "http://localhost:8081"
        props[ProducerConfig.CLIENT_ID_CONFIG] = "KafkaExampleProducer"
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java.name
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java.name
        return KafkaProducer(props)
    }
}
