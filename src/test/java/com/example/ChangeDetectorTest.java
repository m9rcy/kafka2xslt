package com.example;

import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChangeDetectorTest {

    private final ChangeDetector changeDetector = new ChangeDetector();
    private final OffsetDateTime now = OffsetDateTime.now();
    private final OffsetDateTime later = now.plusHours(1);

    @Test
    void compare_whenPreviousIsNull_shouldReturnChanges() {
        Order current = createSampleOrder("1", "desc", "name", "123", now, now.plusHours(1),
                Arrays.asList("wo1", "wo2"));

        OrderChanges result = changeDetector.compare(null, current);

        assertTrue(result.isHasChange());
        assertFalse(result.isHasWorkOrderChange());
        assertNull(result.getAddedWorkOrders());
        assertNull(result.getDeletedWorkOrders());
    }

    @Test
    void compare_whenPreviousVersionIsHigherButModifiedEarlier_shouldReturnNoChanges() {
        Order previous = createSampleOrderWithVersion("1", 2, now.minusHours(1));
        Order current = createSampleOrderWithVersion("1", 1, now);

        OrderChanges result = changeDetector.compare(previous, current);

        assertFalse(result.isHasChange());
        assertFalse(result.isHasWorkOrderChange());
    }

    @Test
    void compare_whenNoFieldsChanged_shouldReturnNoChanges() {
        Order previous = createSampleOrder("1", "desc", "name", "123", now, now.plusHours(1),
                Arrays.asList("wo1", "wo2"));
        Order current = createSampleOrder("1", "desc", "name", "123", now, now.plusHours(1),
                Arrays.asList("wo1", "wo2"));

        OrderChanges result = changeDetector.compare(previous, current);

        assertFalse(result.isHasChange());
        assertFalse(result.isHasWorkOrderChange());
    }

    @Test
    void compare_whenOrderFieldsChanged_shouldDetectChanges() {
        Order previous = createSampleOrder("1", "desc", "name", "123", now, now.plusHours(1),
                Collections.emptyList());
        Order current = createSampleOrder("1", "new desc", "new name", "456", now, now.plusHours(2),
                Collections.emptyList());

        OrderChanges result = changeDetector.compare(previous, current);

        assertTrue(result.isHasChange());
        assertFalse(result.isHasWorkOrderChange());
    }

    @Test
    void compare_whenWorkOrdersAdded_shouldDetectAddedWorkOrders() {
        Order previous = createSampleOrder("1", "desc", "name", "123", now, now.plusHours(1),
                Arrays.asList("wo1", "wo2"));
        Order current = createSampleOrder("1", "desc", "name", "123", now, now.plusHours(1),
                Arrays.asList("wo1", "wo2", "wo3"));

        OrderChanges result = changeDetector.compare(previous, current);

        assertTrue(result.isHasChange());
        assertTrue(result.isHasWorkOrderChange());
        assertEquals(1, result.getAddedWorkOrders().size());
        assertEquals("wo3", result.getAddedWorkOrders().get(0));
        assertTrue(result.getDeletedWorkOrders().isEmpty());
    }

    @Test
    void compare_whenWorkOrdersRemoved_shouldDetectDeletedWorkOrders() {
        Order previous = createSampleOrder("1", "desc", "name", "123", now, now.plusHours(1),
                Arrays.asList("wo1", "wo2", "wo3"));
        Order current = createSampleOrder("1", "desc", "name", "123", now, now.plusHours(1),
                Arrays.asList("wo1", "wo2"));

        OrderChanges result = changeDetector.compare(previous, current);

        assertTrue(result.isHasChange());
        assertTrue(result.isHasWorkOrderChange());
        assertEquals(1, result.getDeletedWorkOrders().size());
        assertEquals("wo3", result.getDeletedWorkOrders().get(0));
        assertTrue(result.getAddedWorkOrders().isEmpty());
    }

    @Test
    void compare_whenWorkOrdersReplaced_shouldDetectBothAddedAndDeleted() {
        Order previous = createSampleOrder("1", "desc", "name", "123", now, now.plusHours(1),
                Arrays.asList("wo1", "wo2"));
        Order current = createSampleOrder("1", "desc", "name", "123", now, now.plusHours(1),
                Arrays.asList("wo3", "wo4"));

        OrderChanges result = changeDetector.compare(previous, current);

        assertTrue(result.isHasChange());
        assertTrue(result.isHasWorkOrderChange());
        assertEquals(2, result.getAddedWorkOrders().size());
        assertEquals(2, result.getDeletedWorkOrders().size());
        assertTrue(result.getAddedWorkOrders().containsAll(Arrays.asList("wo3", "wo4")));
        assertTrue(result.getDeletedWorkOrders().containsAll(Arrays.asList("wo1", "wo2")));
    }

    @Test
    void compare_whenWorkOrdersReordered_shouldNotDetectChanges() {
        Order previous = createSampleOrder("1", "desc", "name", "123", now, now.plusHours(1),
                Arrays.asList("wo1", "wo2"));
        Order current = createSampleOrder("1", "desc", "name", "123", now, now.plusHours(1),
                Arrays.asList("wo2", "wo1"));

        OrderChanges result = changeDetector.compare(previous, current);

        assertFalse(result.isHasChange());
        assertFalse(result.isHasWorkOrderChange());
    }

    @Test
    void compare_whenWorkOrdersChangedFromNullToEmpty_shouldNotDetectChanges() {
        Order previous = createSampleOrder("1", "desc", "name", "123", now, now.plusHours(1), null);
        Order current = createSampleOrder("1", "desc", "name", "123", now, now.plusHours(1),
                Collections.emptyList());

        OrderChanges result = changeDetector.compare(previous, current);

        assertFalse(result.isHasChange());
        assertFalse(result.isHasWorkOrderChange());
    }

    @Test
    void compare_whenWorkOrdersChangedFromEmptyToNull_shouldNotDetectChanges() {
        Order previous = createSampleOrder("1", "desc", "name", "123", now, now.plusHours(1),
                Collections.emptyList());
        Order current = createSampleOrder("1", "desc", "name", "123", now, now.plusHours(1), null);

        OrderChanges result = changeDetector.compare(previous, current);

        assertFalse(result.isHasChange());
        assertFalse(result.isHasWorkOrderChange());
    }

    @Test
    void compare_whenWorkOrdersChangedFromNullToNonEmpty_shouldDetectChanges() {
        Order previous = createSampleOrder("1", "desc", "name", "123", now, now.plusHours(1), null);
        Order current = createSampleOrder("1", "desc", "name", "123", now, now.plusHours(1),
                List.of("wo1"));

        OrderChanges result = changeDetector.compare(previous, current);

        assertTrue(result.isHasChange());
        assertTrue(result.isHasWorkOrderChange());
        assertEquals(1, result.getAddedWorkOrders().size());
        assertEquals("wo1", result.getAddedWorkOrders().get(0));
        assertTrue(result.getDeletedWorkOrders().isEmpty());
    }

    @Test
    void compare_whenWorkOrdersChangedFromNonEmptyToNull_shouldDetectChanges() {
        Order previous = createSampleOrder("1", "desc", "name", "123", now, now.plusHours(1),
                List.of("wo1"));
        Order current = createSampleOrder("1", "desc", "name", "123", now, now.plusHours(1), null);

        OrderChanges result = changeDetector.compare(previous, current);

        assertTrue(result.isHasChange());
        assertTrue(result.isHasWorkOrderChange());
        assertEquals(1, result.getDeletedWorkOrders().size());
        assertEquals("wo1", result.getDeletedWorkOrders().get(0));
        assertTrue(result.getAddedWorkOrders().isEmpty());
    }

    @Test
    void compare_whenOnlyWorkOrdersChanged_shouldDetectChanges() {
        Order previous = createSampleOrder("1", "desc", "name", "123", now, now.plusHours(1),
                List.of("wo1"));
        Order current = createSampleOrder("1", "desc", "name", "123", now, now.plusHours(1),
                List.of("wo2"));

        OrderChanges result = changeDetector.compare(previous, current);

        assertTrue(result.isHasChange());
        assertTrue(result.isHasWorkOrderChange());
        assertEquals(1, result.getAddedWorkOrders().size());
        assertEquals(1, result.getDeletedWorkOrders().size());
        assertEquals("wo2", result.getAddedWorkOrders().get(0));
        assertEquals("wo1", result.getDeletedWorkOrders().get(0));
    }

    private Order createSampleOrder(String id, String desc, String contactName, String contactNumber,
                                    OffsetDateTime start, OffsetDateTime end, List<String> workOrders) {
        return Order.builder()
                .id(id)
                .description(desc)
                .contactName(contactName)
                .contactNumber(contactNumber)
                .start(start)
                .end(end)
                .workOrders(workOrders)
                .version(1)
                .createdOn(now)
                .modifiedOn(now)
                .build();
    }

    private Order createSampleOrderWithVersion(String id, int version, OffsetDateTime modifiedOn) {
        return Order.builder()
                .id(id)
                .description("desc")
                .contactName("name")
                .contactNumber("123")
                .start(now)
                .end(now.plusHours(1))
                .workOrders(Collections.emptyList())
                .version(version)
                .createdOn(now)
                .modifiedOn(modifiedOn)
                .build();
    }
}