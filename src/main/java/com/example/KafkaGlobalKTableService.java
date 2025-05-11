package com.example;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(prefix = "feature.kafka", name = "enabled", havingValue = "true")
public class KafkaGlobalKTableService implements KeyValueStoreService<String, Order> {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaGlobalKTableService.class);
    
    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    @Autowired
    public KafkaGlobalKTableService(StreamsBuilderFactoryBean streamsBuilderFactoryBean) {
        this.streamsBuilderFactoryBean = streamsBuilderFactoryBean;
    }

    @Override
    public Optional<Order> findByKey(String storeName, String key) {
        try {
            KafkaStreams kafkaStreams = getKafkaStreams();
            if (kafkaStreams == null) {
                return Optional.empty();
            }
            
            ReadOnlyKeyValueStore<String, Order> store = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType(storeName, QueryableStoreTypes.keyValueStore())
            );
            return Optional.ofNullable(store.get(key));
        } catch (Exception e) {
            logger.warn("Error accessing store '{}' for key '{}'", storeName, key, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean isStoreAvailable(String storeName) {
        try {
            KafkaStreams kafkaStreams = getKafkaStreams();
            if (kafkaStreams == null) return false;
            return isEnabled();
        } catch (Exception e) {
            logger.debug("Store '{}' not available", storeName, e);
            return false;
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
    
    private KafkaStreams getKafkaStreams() {
        KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
        if (kafkaStreams == null || kafkaStreams.state() != KafkaStreams.State.RUNNING) {
            logger.warn("KafkaStreams not available or not running");
            return null;
        }
        return kafkaStreams;
    }
}