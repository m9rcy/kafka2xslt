package com.example;

import com.github.javafaker.Faker;
import com.github.javafaker.service.FakeValuesService;
import com.github.javafaker.service.RandomService;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.*;

import static com.example.Constants.HEADER_ORDER_CHANGES;
import static org.apache.camel.builder.Builder.bean;

/**
 * A simple Camel route that triggers from a timer and calls a bean and prints to system out.
 * <p/>
 * Use <tt>@Component</tt> to make Camel auto detect this route when starting.
 */
@Component
public class MySpringBootRouter extends RouteBuilder {

//    @Autowired
//    GlobalKTableSearchService lookupService;

    @Autowired
    private KeyValueStoreService<String, Order> keyValueStoreService;

    @Override
    public void configure() {
        ChangeDetector changeDetector = new ChangeDetector();
        FakeValuesService fakeValuesService = new FakeValuesService(
                new Locale("en-NZ"), new RandomService());

        Faker faker = new Faker(new Locale("en-NZ"));

        from("timer:orderProducer?period={{timer.period}}").routeId("hello")
                .process(exchange -> {
                    String[] possibleIds = {"ID001", "ID002", "ID003", "ID004", "ID005"};

                    int randomCount = faker.random().nextInt(0, 5);
                    List<String> randomLengthList = new ArrayList<>();
                    for (int i = 0; i < randomCount; i++) {
                        randomLengthList.add(faker.idNumber().valid());
                    }
                    Order orderEvent = Order.builder()
                            .version(faker.number().numberBetween(1,20))
                            .createdOn(OffsetDateTime.now())
                            .modifiedOn(OffsetDateTime.now())
                            //.id(faker.idNumber().valid())
                            .id(faker.options().option(possibleIds))
                            .contactName(faker.name().firstName())
                            .contactNumber(faker.phoneNumber().phoneNumber())
                            .description(faker.lorem().characters(10))
                            .start(OffsetDateTime.now())
                            .end(OffsetDateTime.now().plusMinutes(5))
                            .workOrders(randomLengthList).build();
                    exchange.getMessage().setBody(orderEvent);
                    exchange.getMessage().setHeader(KafkaConstants.KEY, orderEvent.getId());
                })

                .marshal().json(JsonLibrary.Jackson)
                .log("Body before sending to Kafka ${body}")
                .to("kafka:order-data-topic?"
                    + "brokers=localhost:9092"
                    + "&valueSerializer=org.apache.kafka.common.serialization.StringSerializer"
                    + "&keySerializer=org.apache.kafka.common.serialization.StringSerializer");


        from("kafka:order-data-topic?"
             + "brokers=localhost:9092"
             + "&groupId=my-group"
             + "&valueDeserializer=org.apache.kafka.common.serialization.StringDeserializer"
             + "&keyDeserializer=org.apache.kafka.common.serialization.StringDeserializer")
            .log("Body received from topic ${body}")
            .setProperty(Constants.ORIGINAL_PAYLOAD, body())
            //.enrich("direct:validateInput", new UseOriginalAggregationStrategy())
            .enrich("direct:validateInput")
            .process(exchange -> {
                Order currentOrder = exchange.getIn().getBody(Order.class);
                String key = exchange.getIn().getHeader(KafkaConstants.KEY, String.class);
//                Optional<Order> previousOrder = lookupService.findByKey("order-global-store", key);
//                OrderChanges orderChanges= previousOrder.map(p -> compareOrderChanges(p, currentOrder))
//                        .orElse(OrderChanges.builder().id(currentOrder.getId()).hasChange(false).build());
                if (keyValueStoreService.isStoreAvailable("order-global-store")) {
                    keyValueStoreService.findByKey("order-global-store", key)
                            .ifPresent(p-> exchange.setProperty("previousOrder", p));
                }

            })
                .setHeader(HEADER_ORDER_CHANGES, method(changeDetector, "compare"))
            .process(exchange -> {
                OrderChanges orderChanges = exchange.getIn().getHeader(HEADER_ORDER_CHANGES, OrderChanges.class);
                exchange.getMessage().setHeader(Constants.HEADER_HAS_ENTITY_CHANGE, orderChanges.isHasChange()) ;
                exchange.getMessage().setHeader(Constants.HEADER_HAS_ENTITY_WORK_ORDER_CHANGE, orderChanges.isHasWorkOrderChange());
            })
            .choice()
                .when(header(Constants.HEADER_HAS_ENTITY_CHANGE))
                    .marshal().json(JsonLibrary.Jackson)
                    .log("Before transform Order: ${body}")
                    .to("xj:classpath:request-transform.xsl?transformDirection=JSON2XML")
                    .log("Transformed Order XML: ${body}")
                .endChoice()
            .otherwise()
                .log("No Change will not publish Order")
            .end()
            .choice()
                .when(header(Constants.HEADER_HAS_ENTITY_WORK_ORDER_CHANGE))
                    .setBody(header(HEADER_ORDER_CHANGES))
                    .marshal().json(JsonLibrary.Jackson)
                    .log("Before transform Work Order Changes: ${body}")
                    .to("xj:classpath:request-wo-transform.xsl?transformDirection=JSON2XML")
                    .log("Transformed WorkOrder XML: ${body}")
                .endChoice()
            .otherwise()
                .log("No Change will not publish Work Order")
            .end()
                .setBody(exchangeProperty(Constants.ORIGINAL_PAYLOAD))
                .to("kafka:_order-data-topic?"
                    + "brokers=localhost:9092"
                    + "&valueSerializer=org.apache.kafka.common.serialization.StringSerializer"
                    + "&keySerializer=org.apache.kafka.common.serialization.StringSerializer");





        from("direct:validateInput")
            .log("Body received from validateInput: ${body}")
            .unmarshal().json(JsonLibrary.Jackson, Order.class)
            .log("Body after unmarshal validateInput: ${body}")
            .to("bean-validator://testing")
            .log("Valid user received: ${body.id}")
            .end();

    }

    private OrderChanges compareOrderChanges(Order previous, Order current) {
        OrderChanges result = OrderChanges.builder().id(current.getId()).hasChange(false).build();
        if (previous == null) {
            return result;
        }

        if (previous.getVersion() > current.getVersion() && previous.getModifiedOn().isBefore(current.getModifiedOn())) {
            return result;
        } else {
            // Check if Order fields have changed
            if (!current.getId().equals(previous.getId()) ||
                !current.getDescription().equals(previous.getDescription()) ||
                !current.getStart().equals(previous.getStart()) ||
                !current.getEnd().equals(previous.getEnd()) ||
                !current.getContactName().equals(previous.getContactName()) ||
                !current.getContactNumber().equals(previous.getContactNumber())) {
                OrderChanges orderChanges = OrderChanges.builder().id(current.getId()).hasChange(true).build();

                // Check if Work Orders have changed
                if (hasWorkOrderChanges(previous, current)) {
                    compareWorkOrder(orderChanges, previous.getWorkOrders(), current.getWorkOrders());
                    orderChanges.setHasWorkOrderChange(true);
                    result = orderChanges;
                }
            }
        }

        return result;
    }

    private OrderChanges compareWorkOrder(OrderChanges orderChanges, List<String> previousWorkOrders, List<String> currentWorkOrders) {
        List<String> toBeAdded = new ArrayList<>();
        List<String> toBeDeleted = new ArrayList<>();

        // Find added items (in current but not in previous)
        for (String item : currentWorkOrders) {
            if (!previousWorkOrders.contains(item)) {
                toBeAdded.add(item);
            }
        }

        // Find deleted items (in previous but not in current)
        for (String item : previousWorkOrders) {
            if (!currentWorkOrders.contains(item)) {
                toBeDeleted.add(item);
            }
        }

        orderChanges.setAddedWorkOrders(toBeAdded);
        orderChanges.setDeletedWorkOrders(toBeDeleted);

        return orderChanges;
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
