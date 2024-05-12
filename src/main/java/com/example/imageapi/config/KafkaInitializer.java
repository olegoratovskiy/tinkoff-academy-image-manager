package com.example.imageapi.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.RoundRobinPartitioner;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

/**
 * Kafka configuration.
 */
@Configuration
@RequiredArgsConstructor
public class KafkaInitializer {

    private final KafkaProperties properties;
    @Value("${spring.kafka.replicas}")
    private int replicas;
    @Value("${spring.kafka.partitions}")
    private int partitions;

    /**
     * Create bean for images.wip topic.
     *
     * @return images.wip topic
     */
    @Bean
    public NewTopic topicImagesWip() {
        return new NewTopic("images.wip", replicas, (short) partitions);
    }

    /**
     * Create bean for images.done topic.
     *
     * @return images.done topic
     */
    @Bean
    public NewTopic topicImagesDone() {
        return new NewTopic("images.done", replicas, (short) partitions);
    }

    /**
     * Create bean for consumers factory.
     *
     * @param <V> value type
     * @return Consumer factory
     */
    @Bean
    public <V> ConsumerFactory<String, V> consumerFactory() {
        var props = properties.buildConsumerProperties(null);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        props.put(
            ConsumerConfig.PARTITION_ASSIGNMENT_STRATEGY_CONFIG,
            "org.apache.kafka.clients.consumer.RoundRobinAssignor"
        );
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(),
            new JsonDeserializer<V>().trustedPackages("*"));
    }

    /**
     * Create bean for producers factory.
     *
     * @param <V> value type
     * @return Producer factory
     */
    @Bean
    public <V> ProducerFactory<String, V> producerFactory() {
        var props = properties.buildProducerProperties(null);

        props.put(
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
            StringSerializer.class
        );
        props.put(
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
            JsonSerializer.class
        );

        props.put(
            ProducerConfig.PARTITIONER_CLASS_CONFIG,
            RoundRobinPartitioner.class
        );

        props.put(ProducerConfig.LINGER_MS_CONFIG, 0);

        props.put(
            ProducerConfig.RETRIES_CONFIG,
            Integer.toString(Integer.MAX_VALUE)
        );
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        return new DefaultKafkaProducerFactory<>(props);
    }

}
