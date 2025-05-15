package com.example;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Data;

@JsonDeserialize(builder = Order.OrderBuilder.class)
@Builder
@Data
public class OrderWindow {
    private String id;
}
