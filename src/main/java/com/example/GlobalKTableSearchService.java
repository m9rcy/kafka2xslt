package com.example;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * A generic service for searching GlobalKTable state stores by key.
 * Supports both String and POJO values and can be enabled/disabled via configuration.
 */
@Service
@ConditionalOnBean(name = "orderDataGlobalKTable")
public class GlobalKTableSearchService {
    private static final Logger logger = LoggerFactory.getLogger(GlobalKTableSearchService.class);

    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    private static final String STORE_NAME = "order-global-store";

    @Autowired
    public GlobalKTableSearchService(StreamsBuilderFactoryBean streamsBuilderFactoryBean) {
        this.streamsBuilderFactoryBean = streamsBuilderFactoryBean;
    }

    /**
     * Get the KafkaStreams instance from the factory bean.
     *
     * @return KafkaStreams instance
     * @throws IllegalStateException if KafkaStreams is not available
     */
    private KafkaStreams getKafkaStreams() {
        KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
        if (kafkaStreams == null) {
            throw new IllegalStateException("KafkaStreams is not available. Ensure Kafka Streams application is running.");
        }

        // Check if the KafkaStreams instance is in a running state
        if (kafkaStreams.state() != KafkaStreams.State.RUNNING) {
            logger.warn("KafkaStreams is not in RUNNING state. Current state: {}", kafkaStreams.state());
            throw new IllegalStateException("KafkaStreams is not in RUNNING state. Current state: " + kafkaStreams.state());
        }

        return kafkaStreams;
    }

    public <K, V> Optional<V> findByKey(String storeName, K key) {
        logger.debug("Searching for key '{}' in store '{}'", key, storeName);
        ReadOnlyKeyValueStore<K, V> store = getKafkaStreams().store(
                StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.keyValueStore())
        );

        V value = store.get(key);
        return Optional.ofNullable(value);
    }

}