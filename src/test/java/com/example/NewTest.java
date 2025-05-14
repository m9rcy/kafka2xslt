package com.example;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewTest {

    @Mock
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    @Mock
    private KafkaStreams kafkaStreams;

    @Mock
    private GlobalKTable<String, UserProfile> globalKTable;

    @Mock
    private ReadOnlyKeyValueStore<String, UserProfile> keyValueStore;

    private StateStoreService stateStoreService;

    @BeforeEach
    void setUp() {
        stateStoreService = new StateStoreService(streamsBuilderFactoryBean);
    }

    @Test
    void getStoreFor_shouldReturnStoreFromGlobalKTable() {
        // Arrange
        String storeName = "user-profiles";
        when(globalKTable.queryableStoreName()).thenReturn(storeName);
        when(streamsBuilderFactoryBean.getKafkaStreams()).thenReturn(kafkaStreams);
        // Use ArgumentMatchers.any() instead of direct StoreQueryParameters instance
        when(kafkaStreams.store(any(StoreQueryParameters.class))).thenReturn(keyValueStore);

        // Act
        ReadOnlyKeyValueStore<String, UserProfile> result = stateStoreService.getStoreFor(globalKTable);

        // Assert
        assertSame(keyValueStore, result);
        verify(globalKTable).queryableStoreName();
        verify(kafkaStreams).store(any(StoreQueryParameters.class));
    }

    @Test
    void getStore_shouldReturnStoreByName() {
        // Arrange
        String storeName = "user-profiles";
        when(streamsBuilderFactoryBean.getKafkaStreams()).thenReturn(kafkaStreams);
        // Use ArgumentMatchers.any() instead of direct StoreQueryParameters instance
        when(kafkaStreams.store(any(StoreQueryParameters.class))).thenReturn(keyValueStore);

        // Act
        ReadOnlyKeyValueStore<String, UserProfile> result = stateStoreService.getStore(storeName);

        // Assert
        assertSame(keyValueStore, result);
        verify(kafkaStreams).store(any(StoreQueryParameters.class));
    }

    @Test
    void getKafkaStream_shouldThrowWhenStreamsNotAvailable() {
        // Arrange
        when(streamsBuilderFactoryBean.getKafkaStreams()).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> stateStoreService.getStore("any-store"));
    }

    @Test
    void findByKey_shouldReturnOptionalWithValueWhenKeyExists() {
        // Arrange
        String storeName = "user-profiles";
        String key = "user-123";
        UserProfile profile = new UserProfile("user-123","John Doe");
        when(streamsBuilderFactoryBean.getKafkaStreams()).thenReturn(kafkaStreams);
        // Use ArgumentMatchers.any() instead of direct StoreQueryParameters instance
        when(kafkaStreams.store(any(StoreQueryParameters.class))).thenReturn(keyValueStore);
        when(keyValueStore.get(key)).thenReturn(profile);

        // Act
        Optional<UserProfile> result = stateStoreService.findByKey(storeName, key);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(profile, result.get());
    }

    @Test
    void findByKey_shouldReturnEmptyOptionalWhenKeyDoesNotExist() {
        // Arrange
        String storeName = "user-profiles";
        String key = "non-existent-user";
        when(streamsBuilderFactoryBean.getKafkaStreams()).thenReturn(kafkaStreams);
        // Use ArgumentMatchers.any() instead of direct StoreQueryParameters instance
        when(kafkaStreams.store(any(StoreQueryParameters.class))).thenReturn(keyValueStore);
        when(keyValueStore.get(key)).thenReturn(null);

        // Act
        Optional<UserProfile> result = stateStoreService.findByKey(storeName, key);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void isEnabled_shouldAlwaysReturnTrue() {
        assertTrue(stateStoreService.isEnabled());
    }

    @Test
    void isStoreAvailable_shouldReturnTrue() {
        assertTrue(stateStoreService.isStoreAvailable("any-store"));
    }
}