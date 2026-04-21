package edu.classproject.order;

import edu.classproject.common.Money;
import edu.classproject.common.OrderStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Order aggregate root.
 *
 * GRASP Information Expert: owns status transition rules because it
 * holds the current status and knows what transitions are legal.
 *
 * SOLID SRP: only responsible for order data and lifecycle state.
 */
public class Order {

    private final String orderId;
    private final String customerId;
    private final String restaurantId;
    private final List<OrderItem> items;
    private OrderStatus status;

    /** Legal transitions: from → allowed next statuses */
    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED = Map.of(
        OrderStatus.CREATED,          Set.of(OrderStatus.PLACED),
        OrderStatus.PLACED,           Set.of(OrderStatus.CONFIRMED, OrderStatus.PAYMENT_FAILED),
        OrderStatus.CONFIRMED,        Set.of(OrderStatus.PREPARING, OrderStatus.CANCELLED),
        OrderStatus.PREPARING,        Set.of(OrderStatus.READY_FOR_PICKUP),
        OrderStatus.READY_FOR_PICKUP, Set.of(OrderStatus.OUT_FOR_DELIVERY),
        OrderStatus.OUT_FOR_DELIVERY, Set.of(OrderStatus.DELIVERED),
        OrderStatus.DELIVERED,        Set.of(),
        OrderStatus.PAYMENT_FAILED,   Set.of(),
        OrderStatus.CANCELLED,        Set.of()
    );

    public Order(String orderId, String customerId, String restaurantId, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        this.orderId      = orderId;
        this.customerId   = customerId;
        this.restaurantId = restaurantId;
        this.items        = new ArrayList<>(items);
        this.status       = OrderStatus.CREATED;
    }

    /**
     * Validates and applies a status transition.
     *
     * @throws IllegalStateException if the transition is not permitted
     */
    public void transitionTo(OrderStatus newStatus) {
        Set<OrderStatus> allowed = ALLOWED.getOrDefault(this.status, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new IllegalStateException(
                "Cannot transition from " + this.status + " to " + newStatus);
        }
        this.status = newStatus;
    }

    public String orderId()      { return orderId; }
    public String customerId()   { return customerId; }
    public String restaurantId() { return restaurantId; }
    public OrderStatus status()  { return status; }

    public List<OrderItem> items() {
        return Collections.unmodifiableList(items);
    }

    public Money totalAmount() {
        Money total = Money.of(0);
        for (OrderItem item : items) {
            total = total.add(item.lineTotal());
        }
        return total;
    }
}
