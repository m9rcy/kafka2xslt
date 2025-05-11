package com.example;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.kafka.streams.StreamsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.test.context.EmbeddedKafka;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EmbeddedKafka
@Import(KafkaStreamsConfig.class)
class KafkaStreamsConfigTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StreamsBuilderFactoryBean streamsBuilderFactoryBean;

    @Test
    void objectMapperShouldHaveCorrectConfiguration() {
        assertFalse(objectMapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS),
            "WRITE_DATES_AS_TIMESTAMPS should be disabled");

        assertFalse(objectMapper.isEnabled(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE),
            "ADJUST_DATES_TO_CONTEXT_TIME_ZONE should be disabled");

//        assertTrue(objectMapper.getRegisteredModuleIds().contains("com.fasterxml.jackson.datatype:jackson-datatype-jsr310"),
//            "JavaTimeModule should be registered");
    }

    @Test
    void kafkaStreamsConfigurationShouldBeValid() {
        var config = streamsBuilderFactoryBean.getStreamsConfiguration();
        assertNotNull(config);
        assertEquals(StreamsConfig.EXACTLY_ONCE_V2, config.get(StreamsConfig.PROCESSING_GUARANTEE_CONFIG));
    }
}