package com.example;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaStateStoreService1Test {

    @Mock
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    @Mock
    private KafkaStreams kafkaStreams;

    @Mock
    private GlobalKTable<String, String> globalKTable;

    @Mock
    private ReadOnlyKeyValueStore<String, String> keyValueStore;

    private KafkaStateStoreService kafkaStateStoreService;

    @BeforeEach
    void setUp() {
        kafkaStateStoreService = new KafkaStateStoreService(streamsBuilderFactoryBean);
    }

    @Test
    void getStoreFor_shouldRetrieveStoreUsingGlobalKTableName() {
        // Arrange
        String storeName = "test-store";
        when(globalKTable.queryableStoreName()).thenReturn(storeName);
        when(streamsBuilderFactoryBean.getKafkaStreams()).thenReturn(kafkaStreams);

        // Fix: Use type-safe matcher for the generic return
        when(kafkaStreams.store(any(StoreQueryParameters.class)))
                .thenAnswer(invocation -> {
                    StoreQueryParameters<?> params = invocation.getArgument(0);
                    if (params.queryableStoreType().equals(QueryableStoreTypes.keyValueStore())) {
                        return keyValueStore;
                    }
                    return null;
                });

        // Act
        ReadOnlyKeyValueStore<String, String> result =
                kafkaStateStoreService.getStoreFor(globalKTable);

        // Assert
        assertSame(keyValueStore, result);
        verify(globalKTable).queryableStoreName();
        verify(kafkaStreams).store(any(StoreQueryParameters.class));
    }

    @Test
    void getStore_shouldRetrieveStoreByName() {
        // Arrange
        String storeName = "test-store";
        when(streamsBuilderFactoryBean.getKafkaStreams()).thenReturn(kafkaStreams);

        // Fix: Use type-safe matcher
        when(kafkaStreams.store(any(StoreQueryParameters.class)))
                .thenAnswer(invocation -> {
                    StoreQueryParameters<?> params = invocation.getArgument(0);
                    if (params.queryableStoreType().equals(QueryableStoreTypes.keyValueStore())) {
                        return keyValueStore;
                    }
                    return null;
                });

        // Act
        ReadOnlyKeyValueStore<String, String> result =
                kafkaStateStoreService.getStore(storeName);

        // Assert
        assertSame(keyValueStore, result);
        verify(kafkaStreams).store(any(StoreQueryParameters.class));
    }

    @Test
    void getKafkaStream_shouldThrowWhenStreamsNotInitialized() {
        // Arrange
        when(streamsBuilderFactoryBean.getKafkaStreams()).thenReturn(null);

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> kafkaStateStoreService.getStore("any-store")
        );
        
        assertEquals("Kafka Streams not available yet", exception.getMessage());
    }

    @Test
    void getStore_shouldUseCorrectStoreType() {
        // Arrange
        String storeName = "test-store";
        when(streamsBuilderFactoryBean.getKafkaStreams()).thenReturn(kafkaStreams);
        when(kafkaStreams.store(any(StoreQueryParameters.class))).thenReturn(keyValueStore);

        // Act
        kafkaStateStoreService.getStore(storeName);

        // Assert - Verify the correct store type was requested
        verify(kafkaStreams).store(StoreQueryParameters.fromNameAndType(
            storeName,
            QueryableStoreTypes.keyValueStore()
        ));
    }
}