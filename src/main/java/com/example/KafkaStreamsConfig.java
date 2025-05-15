package com.example;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafkaStreams
@Conditional(FeatureFlagCondition.class)
public class KafkaStreamsConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.streams.application-id}")
    private String applicationId;

    @Value("${global.ktable.search.userTopic}")
    private String userTopic;

    @Value("${global.ktable.search.orderTopic}")
    private String orderTopic;

    /**
     * Configure Kafka Streams.
     */
    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        
        // Enable exactly-once processing
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
        
        // Configure state directory for local state stores
        //props.put(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams");
        props.put(StreamsConfig.STATE_DIR_CONFIG, "C://Users//m9rcy//dev//kafka//kafka-streams");

        return new KafkaStreamsConfiguration(props);
    }

    /**
     * Example of creating a GlobalKTable with String values.
     */
    //@Bean
    public GlobalKTable<String, String> stringGlobalKTable(StreamsBuilder streamsBuilder) {
        return streamsBuilder.globalTable(
                "string-data-topic",
                Consumed.with(Serdes.String(), Serdes.String()),
                Materialized.as("string-global-store")
        );
    }


    /**
     * Example of creating a GlobalKTable with Order values.
     */
    @Bean
    public GlobalKTable<String, Order> orderDataGlobalKTable(StreamsBuilder streamsBuilder, ObjectMapper objectMapper) {
        // Verify the configuration
//        if (objectMapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)) {
//            throw new IllegalStateException("WRITE_DATES_AS_TIMESTAMPS should be disabled");
//        }
//        if (objectMapper.isEnabled(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)) {
//            throw new IllegalStateException("ADJUST_DATES_TO_CONTEXT_TIME_ZONE should be disabled");
//        }
        JsonSerde<Order> orderSerde = new JsonSerde<>(Order.class, objectMapper);
        return streamsBuilder.globalTable(
                "_order-data-topic",
                Consumed.with(Serdes.String(),  orderSerde),
                Materialized.as("order-global-store")
        );
    }


    /**
     * Example of creating a GlobalKTable with POJO values.
     */
    @Bean
    public GlobalKTable<String, UserProfile> userProfileGlobalKTable(StreamsBuilder streamsBuilder) {
        return streamsBuilder.globalTable(
                userTopic,
                Consumed.with(Serdes.String(), new JsonSerde<>(UserProfile.class)),
                Materialized.as("user-profile-global-store")
        );
    }

    public String getUserTopic() {
        return userTopic;
    }

    public String getOrderTopic() {
        return orderTopic;
    }
}