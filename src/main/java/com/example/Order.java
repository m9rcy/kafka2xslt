package com.example;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import jdk.jfr.DataAmount;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@JsonDeserialize(builder = Order.OrderBuilder.class)
@Builder
@Data
public class Order {

    private Integer version;
    private OffsetDateTime createdOn;
    private OffsetDateTime modifiedOn;

    //private OrderWindow orderWindow;

    @NotNull
    private String id;
    @NotNull
    private String description;
    @NotNull
    private String contactName;
    @NotNull
    private String contactNumber;
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime start;
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime end;
    private List<String> workOrders;

    @JsonPOJOBuilder(withPrefix = "")
    public static class OrderBuilder {
    }


}
