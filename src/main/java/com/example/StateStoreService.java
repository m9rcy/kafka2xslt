package com.example;

import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StateStoreService extends KafkaStateStoreService implements KeyValueStoreService<String, UserProfile> {
    public StateStoreService(StreamsBuilderFactoryBean streamsBuilderFactoryBean) {
        super(streamsBuilderFactoryBean);
    }

    @Override
    public Optional<UserProfile> findByKey(String storeName, String key) {
        ReadOnlyKeyValueStore<String, UserProfile> store  = getStore(storeName);
        return Optional.ofNullable(store.get(key));
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
