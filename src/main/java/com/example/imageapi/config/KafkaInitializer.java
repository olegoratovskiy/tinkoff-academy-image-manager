package com.example.imageapi.config;

import com.example.imageapi.kafka.ImagesWipMessage;
import java.util.Map;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.RoundRobinPartitioner;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
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
     * Create bean for kafka producer.
     *
     * @return kafka producer
     */
    @Bean
    public KafkaTemplate<String, ImagesWipMessage> kafkaTemplateImagesWip() {
        return new KafkaTemplate<>(producerFactory(props ->
            props.put(ProducerConfig.ACKS_CONFIG, "all"))
        );
    }

    private <V> ProducerFactory<String, V> producerFactory(
        final Consumer<Map<String, Object>> enchanter
    ) {
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

        enchanter.accept(props);

        return new DefaultKafkaProducerFactory<>(props);
    }

}
