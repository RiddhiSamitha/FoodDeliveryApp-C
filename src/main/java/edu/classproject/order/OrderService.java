package edu.classproject.order;

import edu.classproject.common.OrderStatus;

import java.util.List;

/**
 * Primary application port for all order use cases.
 *
 * SOLID ISP: only operations that order-domain clients need are declared
 *   here. Analytics, kitchen-board, and reporting queries live in their
 *   own dedicated service interfaces.
 * SOLID DIP: callers and implementations both depend on this abstraction;
 *   no class holds a direct reference to DefaultOrderService.
 * GRASP Controller: the single, well-defined entry point from the
 *   application/UI layer into the order sub-domain.
 */
public interface OrderService {

    /**
     * Validates the request, resolves menu items, processes payment,
     * persists the order, and notifies the customer.
     *
     * @param request non-null, self-validated order intent
     * @return the persisted Order; status is CONFIRMED or PAYMENT_FAILED
     * @throws IllegalArgumentException if the restaurant or any item is not found
     */
    Order placeOrder(OrderRequest request);

    /**
     * Returns the order with the given ID.
     *
     * @throws IllegalArgumentException if no such order exists
     */
    Order getOrder(String orderId);

    /**
     * Advances an order to newStatus, enforcing the state machine defined
     * in Order.transitionTo(). Notifies the customer on every success.
     *
     * @throws IllegalArgumentException if the order does not exist
     * @throws IllegalStateException    if the transition is not permitted
     */
    void updateStatus(String orderId, OrderStatus newStatus);

    /**
     * Cancels an order if it has not yet reached a terminal state.
     * Notifies the customer on success.
     *
     * @throws IllegalArgumentException if the order does not exist
     * @throws IllegalStateException    if the order is already terminal
     *         (DELIVERED, PAYMENT_FAILED, or CANCELLED)
     */
    void cancelOrder(String orderId);

    /**
     * Returns all orders placed by the given customer, in no particular order.
     */
    List<Order> getOrdersByCustomer(String customerId);

    /**
     * Returns all orders for a restaurant that are in the given status.
     * Used by the kitchen board and dispatch views.
     */
    List<Order> getOrdersByRestaurant(String restaurantId, OrderStatus status);

    /**
     * Returns all orders system-wide that are in the given status.
     * Used by admin dashboards and the dispatch service.
     */
    List<Order> getOrdersByStatus(OrderStatus status);
}
