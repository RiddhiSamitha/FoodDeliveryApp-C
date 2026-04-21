package edu.classproject.order;

import edu.classproject.restaurant.MenuItem;
import edu.classproject.restaurant.Restaurant;

import java.util.List;

/**
 * Converts a list of OrderRequestItems into OrderItem snapshots.
 *
 * GRASP Information Expert: this class owns the mapping logic because
 * it needs both the request items and the restaurant's menu data.
 *
 * SOLID SRP: responsible only for translating request DTOs into
 * domain OrderItem objects with snapshotted prices.
 */
public class OrderItemMapper {

    private OrderItemMapper() {
        // utility class – no instances
    }

    /**
     * Maps each {@link OrderRequestItem} to an {@link OrderItem} by looking
     * up the menu item in the given restaurant and snapshotting its price.
     *
     * @param restaurant   the restaurant whose menu is used for lookup
     * @param requestItems the list of items from the customer's order request
     * @return a list of OrderItems with snapshotted names and prices
     * @throws IllegalArgumentException if any itemId is not found on the menu
     */
    public static List<OrderItem> from(Restaurant restaurant,
                                       List<OrderRequestItem> requestItems) {
        return requestItems.stream()
                .map(req -> {
                    MenuItem menuItem = restaurant.getMenuItem(req.menuItemId());
                    return new OrderItem(
                            menuItem.itemId(),
                            menuItem.name(),
                            menuItem.price(),
                            req.quantity()
                    );
                })
                .toList();
    }
}
