package com.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderSerializationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeOffsetDateTimeWithTimezone() throws JsonProcessingException {
        Order order = Order.builder()
                .createdOn(OffsetDateTime.of(2025, 5, 11, 15, 1, 16, 653660300, ZoneOffset.ofHours(12)))
                .start(OffsetDateTime.of(2025, 5, 11, 15, 1, 16, 0, ZoneOffset.ofHours(12))).build();

        String json = objectMapper.writeValueAsString(order);
        
        assertTrue(json.contains("\"createdOn\":\"2025-05-11T15:01:16.6536603+12:00\"") ||
                   json.contains("\"createdOn\":\"2025-05-11T15:01:16.653660300+12:00\""),
                   "Should maintain timezone offset in serialization");

        assertTrue(json.contains("\"start\":\"2025-05-11T15:01:16+12:00\""),
                   "Should maintain timezone offset in serialization");
    }

    @Test
    void shouldDeserializeOffsetDateTimeWithTimezone() throws JsonProcessingException {
        String json = "{\"createdOn\":\"2025-05-11T15:01:16.6536603+12:00\",\"start\":\"2025-05-11T15:01:16+12:00\"}";
        
        Order order = objectMapper.readValue(json, Order.class);
        
        assertEquals(ZoneOffset.ofHours(12), order.getCreatedOn().getOffset(),
                    "Should maintain timezone offset in deserialization");
        
        assertEquals(ZoneOffset.ofHours(12), order.getStart().getOffset(),
                    "Should maintain timezone offset in deserialization");
    }
}