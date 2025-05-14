package com.example;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StateStoreServiceTest {

    @Mock
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    @Mock
    private ReadOnlyKeyValueStore<String, UserProfile> readOnlyKeyValueStore;

    private StateStoreService stateStoreService;

    @BeforeEach
    void setUp() {
        // Create a real instance with the mocked factory bean, then spy on it
        StateStoreService realService = new StateStoreService(streamsBuilderFactoryBean);
        stateStoreService = spy(realService);
    }

    @Test
    void findByKey_withExistingKey_shouldReturnValue() {
        // Arrange
        String storeName = "user-profile-store";
        String key = "user123";
        UserProfile expectedProfile = new UserProfile("user123", "John Doe");

        doReturn(readOnlyKeyValueStore).when(stateStoreService).getStore(storeName);
        when(readOnlyKeyValueStore.get(key)).thenReturn(expectedProfile);

        // Act
        Optional<UserProfile> result = stateStoreService.findByKey(storeName, key);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(expectedProfile, result.get());
        verify(stateStoreService).getStore(storeName);
        verify(readOnlyKeyValueStore).get(key);
    }

    @Test
    void findByKey_withNonExistingKey_shouldReturnEmptyOptional() {
        // Arrange
        String storeName = "user-profile-store";
        String key = "nonexistent";

        doReturn(readOnlyKeyValueStore).when(stateStoreService).getStore(storeName);
        when(readOnlyKeyValueStore.get(key)).thenReturn(null);

        // Act
        Optional<UserProfile> result = stateStoreService.findByKey(storeName, key);

        // Assert
        assertFalse(result.isPresent());
        verify(stateStoreService).getStore(storeName);
        verify(readOnlyKeyValueStore).get(key);
    }

    @Test
    void isEnabled_shouldReturnTrue() {
        // Act
        boolean result = stateStoreService.isEnabled();

        // Assert
        assertTrue(result);
    }

    @Test
    void isStoreAvailable_shouldDelegateToIsEnabled() {
        // Arrange
        String storeName = "any-store";

        // Act
        boolean result = stateStoreService.isStoreAvailable(storeName);

        // Assert
        assertTrue(result);
        // This is implicitly testing that the default implementation from the interface is used,
        // which just returns isEnabled()
    }

    @Test
    void exceptionHandling_whenStoreThrowsException_shouldPropagateException() {
        // Arrange
        String storeName = "user-profile-store";
        String key = "user123";
        RuntimeException expectedException = new RuntimeException("Store access error");

        doReturn(readOnlyKeyValueStore).when(stateStoreService).getStore(storeName);
        when(readOnlyKeyValueStore.get(key)).thenThrow(expectedException);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> stateStoreService.findByKey(storeName, key)
        );

        assertEquals("Store access error", exception.getMessage());
        verify(stateStoreService).getStore(storeName);
        verify(readOnlyKeyValueStore).get(key);
    }
}

// Simple UserProfile class for testing purposes
class UserProfile {
    private String id;
    private String name;

    public UserProfile(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserProfile that = (UserProfile) obj;
        return id.equals(that.id) && name.equals(that.name);
    }
}