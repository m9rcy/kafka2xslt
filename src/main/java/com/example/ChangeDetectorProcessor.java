package com.example;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.Constants.HEADER_ORDER_CHANGES;

public class ChangeDetectorProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {
        Order current = exchange.getIn().getBody(Order.class);
        Order previous = exchange.getProperty("previousOrder", Order.class);

        // Early exit if current is null (though this is rejected by bean-validation)
        if (current == null) {
            throw new IllegalArgumentException("Current order cannot be null");
        }

        OrderChanges result = OrderChanges.builder()
                .id(current.getId())
                .hasChange(false)
                .hasWorkOrderChange(false)
                .build();

        // Early exit if previous is null but proceed
        if (previous == null) {
            result.setHasChange(true);
            setResults(exchange, result);
            return;
        }

        // Check version conflict
        if (previous.getVersion() > current.getVersion() &&
            previous.getModifiedOn().isBefore(current.getModifiedOn())) {
            setResults(exchange, result);
            return;
        }

        // Check for any changes
        boolean hasOrderChanges = !current.getId().equals(previous.getId()) ||
                                  !Objects.equals(current.getDescription(), previous.getDescription()) ||
                                  !Objects.equals(current.getStart(), previous.getStart()) ||
                                  !Objects.equals(current.getEnd(), previous.getEnd()) ||
                                  !Objects.equals(current.getContactName(), previous.getContactName()) ||
                                  !Objects.equals(current.getContactNumber(), previous.getContactNumber());

        boolean hasWorkOrderChanges = hasWorkOrderChanges(previous, current);

        if (hasOrderChanges || hasWorkOrderChanges) {
            result.setHasChange(true);

            if (hasWorkOrderChanges) {
                compareWorkOrder(result, previous.getWorkOrders(), current.getWorkOrders());
                result.setHasWorkOrderChange(true);
            }
        }

        setResults(exchange, result);
    }

    private void setResults(Exchange exchange, OrderChanges orderChanges) {
        exchange.getMessage().setHeader(HEADER_ORDER_CHANGES, orderChanges);
        exchange.getMessage().setHeader(Constants.HEADER_HAS_ENTITY_CHANGE, orderChanges.isHasChange());
        exchange.getMessage().setHeader(Constants.HEADER_HAS_ENTITY_WORK_ORDER_CHANGE, orderChanges.isHasWorkOrderChange());
    }

    private void compareWorkOrder(OrderChanges orderChanges, List<String> previousWorkOrders, List<String> currentWorkOrders) {
        // Handle null cases by converting to empty lists
        List<String> prev = previousWorkOrders != null ? previousWorkOrders : Collections.emptyList();
        List<String> curr = currentWorkOrders != null ? currentWorkOrders : Collections.emptyList();

        List<String> toBeAdded = curr.stream()
                .filter(item -> !prev.contains(item))
                .collect(Collectors.toList());

        List<String> toBeDeleted = prev.stream()
                .filter(item -> !curr.contains(item))
                .collect(Collectors.toList());

        orderChanges.setAddedWorkOrders(toBeAdded);
        orderChanges.setDeletedWorkOrders(toBeDeleted);
    }

    private boolean hasWorkOrderChanges(Order previous, Order current) {
        // Both null or empty => no change
        if ((current.getWorkOrders() == null || current.getWorkOrders().isEmpty()) &&
            (previous.getWorkOrders() == null || previous.getWorkOrders().isEmpty())) {
            return false;
        }

        // If only one is null or size increased => changed
        if (current.getWorkOrders() == null || previous.getWorkOrders() == null ||
            current.getWorkOrders().size() != previous.getWorkOrders().size()) {
            return true;
        }

        // Check if contents differ, ignoring order
        Set<String> currentSet = new HashSet<>(current.getWorkOrders());
        Set<String> previousSet = new HashSet<>(previous.getWorkOrders());

        // If they contain the same elements => no change
        return !currentSet.equals(previousSet);
    }
}