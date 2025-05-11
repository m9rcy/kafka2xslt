package com.example;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalKTableSearchServiceTest {

    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;
    private KafkaStreams kafkaStreams;
    private ReadOnlyKeyValueStore<String, String> store;
    private GlobalKTableSearchService service;

    @BeforeEach
    void setup() {
        streamsBuilderFactoryBean = mock(StreamsBuilderFactoryBean.class);
        kafkaStreams = mock(KafkaStreams.class);
        store = mock(ReadOnlyKeyValueStore.class);

        when(streamsBuilderFactoryBean.getKafkaStreams()).thenReturn(kafkaStreams);
        when(kafkaStreams.state()).thenReturn(KafkaStreams.State.RUNNING);

        service = new GlobalKTableSearchService(streamsBuilderFactoryBean);
    }

    @Test
    void testFindByKey_valueFound() {
        when(kafkaStreams.store(any(StoreQueryParameters.class)))
                .thenReturn(store);
        when(store.get("testKey")).thenReturn("testValue");

        Optional<String> result = service.findByKey("storeName", "testKey");
        assertTrue(result.isPresent());
        assertEquals("testValue", result.get());
    }

    @Test
    void testFindByKey_valueNotFound() {
        when(kafkaStreams.store(any(StoreQueryParameters.class)))
                .thenReturn(store);
        when(store.get("missingKey")).thenReturn(null);

        Optional<String> result = service.findByKey("storeName", "missingKey");
        assertFalse(result.isPresent());
    }

    @Test
    void testFindByKey_kafkaStreamsNotRunning() {
        when(kafkaStreams.state()).thenReturn(KafkaStreams.State.REBALANCING);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                service.findByKey("storeName", "testKey")
        );
        assertTrue(exception.getMessage().contains("KafkaStreams is not in RUNNING state"));
    }

    @Test
    void testFindByKey_kafkaStreamsNull() {
        when(streamsBuilderFactoryBean.getKafkaStreams()).thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                service.findByKey("storeName", "testKey")
        );
        assertTrue(exception.getMessage().contains("KafkaStreams is not available"));
    }
}
