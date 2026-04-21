package edu.classproject.order;

import edu.classproject.common.OrderStatus;

import java.util.List;
import java.util.Optional;

/**
 * Persistence port for Order aggregates.
 *
 * SOLID DIP: high-level services depend on this abstraction, not on any
 *   concrete store. Swapping to a database requires only a new class.
 * SOLID ISP: exposes only what order-domain clients need; analytics or
 *   reporting queries belong in their own ports.
 * GRASP Pure Fabrication: a design construct that keeps domain objects
 *   free of persistence concerns.
 */
public interface OrderRepository {

    /** Inserts a new order, or overwrites the one with the same ID. */
    void save(Order order);

    /** Returns the order with the given ID, or empty if not found. */
    Optional<Order> findById(String orderId);

    /** Returns all orders placed by a specific customer. */
    List<Order> findByCustomerId(String customerId);

    /** Returns all orders for a restaurant that are in the given status. */
    List<Order> findByRestaurantIdAndStatus(String restaurantId, OrderStatus status);

    /** Returns all orders currently in the given status (admin/dispatch use). */
    List<Order> findByStatus(OrderStatus status);

    /** Returns every stored order. Use with care — for admin/analytics only. */
    List<Order> findAll();
}
