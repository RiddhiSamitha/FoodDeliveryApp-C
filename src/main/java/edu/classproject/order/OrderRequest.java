package edu.classproject.order;

import java.util.List;

/**
 * Immutable command object expressing the customer's intent to place an order.
 *
 * GRASP Information Expert: validates its own fields.
 * SOLID SRP: data carrier only — orchestration belongs to OrderService.
 */
public record OrderRequest(
        String customerId,
        String restaurantId,
        List<OrderRequestItem> items
) {
    public OrderRequest {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("customerId must not be blank");
        }
        if (restaurantId == null || restaurantId.isBlank()) {
            throw new IllegalArgumentException("restaurantId must not be blank");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }
        items = List.copyOf(items);   // defensive copy → truly immutable
    }
}
