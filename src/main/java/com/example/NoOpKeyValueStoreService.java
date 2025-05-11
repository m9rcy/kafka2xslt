package com.example;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@ConditionalOnProperty(prefix = "feature.kafka", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoOpKeyValueStoreService implements KeyValueStoreService<String, Order> {

    @Override
    public Optional<Order> findByKey(String storeName, String key) {
        return Optional.empty();
    }

    @Override
    public boolean isStoreAvailable(String storeName) {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}