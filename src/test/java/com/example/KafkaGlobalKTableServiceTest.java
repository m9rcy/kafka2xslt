package com.example;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.StreamsMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaGlobalKTableServiceTest {

    private static final String TEST_STORE = "test-store";
    private static final String TEST_KEY = "test-key";
    private static final Order TEST_ORDER = Order.builder().build();
    @Mock
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    @Mock
    private KafkaStreams kafkaStreams;

    @Mock
    private ReadOnlyKeyValueStore<String, Order> mockStore;

    @Mock
    private StreamsMetadata streamsMetadata;

    private KafkaGlobalKTableService service;

    @BeforeEach
    void setUp() {
        service = new KafkaGlobalKTableService(streamsBuilderFactoryBean);
    }

    @Test
    void shouldReturnOrderWhenFoundInStore() {
        when(streamsBuilderFactoryBean.getKafkaStreams()).thenReturn(kafkaStreams);
        when(kafkaStreams.state()).thenReturn(KafkaStreams.State.RUNNING);
        when(kafkaStreams.store(any(StoreQueryParameters.class))).thenReturn(mockStore);
        when(mockStore.get(TEST_KEY)).thenReturn(TEST_ORDER);

        Optional<Order> result = service.findByKey(TEST_STORE, TEST_KEY);

        assertTrue(result.isPresent());
        assertEquals(TEST_ORDER, result.get());
    }

    @Test
    void shouldReturnEmptyWhenKeyNotFound() {
        when(streamsBuilderFactoryBean.getKafkaStreams()).thenReturn(kafkaStreams);
        when(kafkaStreams.state()).thenReturn(KafkaStreams.State.RUNNING);
        when(kafkaStreams.store(any(StoreQueryParameters.class))).thenReturn(mockStore);
        when(mockStore.get(TEST_KEY)).thenReturn(null);

        Optional<Order> result = service.findByKey(TEST_STORE, TEST_KEY);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldReturnEmptyWhenStreamsNotRunning() {
        when(streamsBuilderFactoryBean.getKafkaStreams()).thenReturn(kafkaStreams);
        when(kafkaStreams.state()).thenReturn(KafkaStreams.State.REBALANCING);

        Optional<Order> result = service.findByKey(TEST_STORE, TEST_KEY);

        assertFalse(result.isPresent());
    }

    @Test
    void shouldReturnTrueForStoreAvailability() {
        when(streamsBuilderFactoryBean.getKafkaStreams()).thenReturn(kafkaStreams);
        when(kafkaStreams.state()).thenReturn(KafkaStreams.State.RUNNING);
        when(kafkaStreams.allMetadataForStore(TEST_STORE))
                .thenReturn(Collections.singletonList(streamsMetadata));

        assertTrue(service.isStoreAvailable(TEST_STORE));
    }

    @Test
    void shouldReturnFalseForUnavailableStore() {
        when(streamsBuilderFactoryBean.getKafkaStreams()).thenReturn(kafkaStreams);
        when(kafkaStreams.state()).thenReturn(KafkaStreams.State.RUNNING);
        when(kafkaStreams.allMetadataForStore(TEST_STORE)).thenReturn(Collections.emptyList());

        assertFalse(service.isStoreAvailable(TEST_STORE));
    }

    @Test
    void shouldAlwaysReturnEnabled() {
        assertTrue(service.isEnabled());
    }
}