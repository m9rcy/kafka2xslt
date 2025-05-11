package com.example;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class NoOpKeyValueStoreServiceTest {

    private final NoOpKeyValueStoreService service = new NoOpKeyValueStoreService();
    private static final String TEST_STORE = "test-store";
    private static final String TEST_KEY = "test-key";

    @Test
    void shouldAlwaysReturnEmptyOptional() {
        Optional<Order> result = service.findByKey(TEST_STORE, TEST_KEY);
        assertFalse(result.isPresent());
    }

    @Test
    void shouldAlwaysReturnStoreUnavailable() {
        assertFalse(service.isStoreAvailable(TEST_STORE));
    }

    @Test
    void shouldAlwaysReturnDisabled() {
        assertFalse(service.isEnabled());
    }

    @Test
    void shouldLoadInContextWhenKafkaDisabled() {
        new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class)
            .withPropertyValues("feature.kafka.enabled=false")
            .run(context -> {
                assertTrue(context.containsBean("noOpKeyValueStoreService"));
                assertTrue(context.getBean(KeyValueStoreService.class) instanceof NoOpKeyValueStoreService);
            });
    }

    @Configuration
    static class TestConfig {
        @Bean
        @ConditionalOnProperty(prefix = "feature.kafka", name = "enabled", havingValue = "false")
        public KeyValueStoreService<String, Order> noOpKeyValueStoreService() {
            return new NoOpKeyValueStoreService();
        }
    }
}