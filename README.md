## run demo stream
* kafka-topics --zookeeper localhost:2181 --create --partitions 1 --replication-factor 1 --topic examplesFromProducer
* kafka-topics --zookeeper localhost:2181 --create --partitions 1 --replication-factor 1 --topic exampleNestingsFromKStream
* run AvroBasedExampleProducer
* run AvroStreamProcessor
---

## create topic, produce then consume on the console
* kafka-topics --zookeeper localhost:2181 --create --partitions 1 --replication-factor 1 --topic bar
* kafka-console-producer --broker-list localhost:9092 --property parse.key=true --property key.separator=, --topic bar
* kafka-console-consumer --bootstrap-server localhost:9092 --property print.key=true --property key.separator=, --from-beginning --topic bar
