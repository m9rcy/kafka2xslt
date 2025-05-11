package com.example;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaStreamsConfigMockTest {

    @Mock (lenient = true)
    private StreamsBuilder streamsBuilder;

    @Mock (lenient = true)
    private ObjectMapper objectMapper;

    @Mock (lenient = true)
    private GlobalKTable<String, Order> mockGlobalKTable;

    @InjectMocks
    private KafkaStreamsConfig kafkaStreamsConfig;

    @Test
    void shouldConfigureOrderGlobalKTableWithCorrectSerde() {
        // Setup
        when(objectMapper.isEnabled(any(MapperFeature.class))).thenReturn(true);
        when(objectMapper.isEnabled(any(SerializationFeature.class))).thenReturn(true);
        when(streamsBuilder.globalTable(
            any(String.class),
            any(Consumed.class),
            any(Materialized.class))
        ).thenReturn(mockGlobalKTable);

        // Test
        GlobalKTable<String, Order> result = 
            kafkaStreamsConfig.orderDataGlobalKTable(streamsBuilder, objectMapper);

        // Verify
        assertNotNull(result);
        verify(streamsBuilder).globalTable(
            eq(kafkaStreamsConfig.getOrderTopic()),
            any(Consumed.class),
            eq(Materialized.as("order-global-store")));
    }
}