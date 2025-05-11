package com.example;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for the kafka feature functionality.
 */
@Configuration
@ConfigurationProperties(prefix = "feature.kafka")
public class FeatureConfig {
    
    /**
     * Flag to enable or disable the kafka feature functionality.
     * Default value is false.
     */
    private boolean enabled = false;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}