package com.example;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@JsonDeserialize(builder = OrderChanges.OrderChangesBuilder.class)
@Builder
@Data
public class OrderChanges {

    @NotNull
    private String id;

    private boolean hasChange;
    private boolean hasWorkOrderChange;

    private List<String> addedWorkOrders;
    private List<String> deletedWorkOrders;

    @JsonPOJOBuilder(withPrefix = "")
    public static class OrderChangesBuilder {
    }

}
