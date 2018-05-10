package main;

import demo.Example;
import demo.ExampleNesting;
import demo.ExamplePerson;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Arrays;
import java.util.Properties;

public class AvroBasedExampleProducer {
    public static void main(String[] args) throws Exception {
        runProducer(3, "foo", 2);
    }

    private static void runProducer(final int sendMessageCount,
                                    String outputTopic,
                                    int colourOffset) throws Exception {
        final Producer<String, Example> producer = createProducer();
        long time = System.currentTimeMillis();

        try {
            int limit = 0;
            for (int index = limit; index < limit + sendMessageCount; index++) {
                String favoriteColour = Arrays.asList("red", "green", "blue").get((index + colourOffset) % 3);
                final ProducerRecord<String, Example> record = new ProducerRecord<>(
                        outputTopic,
                        "" + index,
                        new Example((long) index, new ExampleNesting(index == 0), index == 0 ? null : new ExampleNesting(true), "name-" + favoriteColour));

                RecordMetadata metadata = producer.send(record).get();

                long elapsedTime = System.currentTimeMillis() - time;
                System.out.printf("sent record(key=%s value=%s) meta(partition=%d, offset=%d) time=%d\n",
                        record.key(), record.value(), metadata.partition(), metadata.offset(), elapsedTime);
            }
        } finally {
            producer.flush();
            producer.close();
        }
    }

    private static Producer<String, Example> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "http://localhost:9092");
        props.put("schema.registry.url", "http://localhost:8081");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaExampleProducer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        return new KafkaProducer<>(props);
    }
}
