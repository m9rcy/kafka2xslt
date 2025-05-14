package com.example;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.state.QueryableStoreType;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KafkaStateStoreServiceTest {

    @Mock
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    @Mock
    private KafkaStreams kafkaStreams;

    @Mock
    private GlobalKTable<String, String> globalKTable;

    @Mock
    private ReadOnlyKeyValueStore<String, String> readOnlyKeyValueStore;

    private KafkaStateStoreService kafkaStateStoreService;

    @BeforeEach
    void setUp() {
        kafkaStateStoreService = new KafkaStateStoreService(streamsBuilderFactoryBean);
    }

    @Test
    void getStoreFor_shouldGetStoreFromGlobalKTable() {
        // Arrange
        String storeName = "test-store";
        when(globalKTable.queryableStoreName()).thenReturn(storeName);
        when(streamsBuilderFactoryBean.getKafkaStreams()).thenReturn(kafkaStreams);
        when(kafkaStreams.store(any(StoreQueryParameters.class))).thenReturn(readOnlyKeyValueStore);

        // Act
        ReadOnlyKeyValueStore<String, String> result = kafkaStateStoreService.getStoreFor(globalKTable);

        // Assert
        assertEquals(readOnlyKeyValueStore, result);
        verify(globalKTable).queryableStoreName();
        verify(streamsBuilderFactoryBean).getKafkaStreams();

        // Verify that StoreQueryParameters was created correctly
        ArgumentCaptor<StoreQueryParameters> parametersCaptor = ArgumentCaptor.forClass(StoreQueryParameters.class);
        verify(kafkaStreams).store(parametersCaptor.capture());
        
        StoreQueryParameters capturedParameters = parametersCaptor.getValue();
        assertEquals(storeName, capturedParameters.storeName());
        assertTrue(capturedParameters.queryableStoreType() instanceof QueryableStoreTypes.KeyValueStoreType);
    }

    @Test
    void getStore_shouldReturnKeyValueStore() {
        // Arrange
        String storeName = "test-store";
        when(streamsBuilderFactoryBean.getKafkaStreams()).thenReturn(kafkaStreams);
        when(kafkaStreams.store(any(StoreQueryParameters.class))).thenReturn(readOnlyKeyValueStore);

        // Act
        ReadOnlyKeyValueStore<String, String> result = kafkaStateStoreService.getStore(storeName);

        // Assert
        assertEquals(readOnlyKeyValueStore, result);
        verify(streamsBuilderFactoryBean).getKafkaStreams();

        // Verify that StoreQueryParameters was created correctly
        ArgumentCaptor<StoreQueryParameters> parametersCaptor = ArgumentCaptor.forClass(StoreQueryParameters.class);
        verify(kafkaStreams).store(parametersCaptor.capture());
        
        StoreQueryParameters capturedParameters = parametersCaptor.getValue();
        assertEquals(storeName, capturedParameters.storeName());
        assertTrue(capturedParameters.queryableStoreType() instanceof QueryableStoreTypes.KeyValueStoreType);
    }

    @Test
    void getKafkaStream_shouldThrowExceptionWhenStreamsNotAvailable() {
        // Arrange
        when(streamsBuilderFactoryBean.getKafkaStreams()).thenReturn(null);

        // Act & Assert
        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            () -> kafkaStateStoreService.getStore("test-store")
        );
        
        assertEquals("Kafka Streams not available yet", exception.getMessage());
    }
}