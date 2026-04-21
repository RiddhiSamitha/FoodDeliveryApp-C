package edu.classproject.order;

import edu.classproject.common.IdGenerator;
import edu.classproject.common.OrderStatus;
import edu.classproject.notification.NotificationService;
import edu.classproject.payment.PaymentResult;
import edu.classproject.payment.PaymentService;
import edu.classproject.restaurant.Restaurant;
import edu.classproject.restaurant.RestaurantService;

import java.util.List;
import java.util.Objects;

/**
 * Orchestrates the full order lifecycle.
 *
 * SOLID SRP:  Each collaborator owns exactly one concern.
 * SOLID DIP:  Every dependency is an abstraction; no 'new' calls on services.
 * GRASP Controller:  First non-UI object to handle "place order" etc.
 * GRASP Low Coupling:  Depends only on interfaces.
 */
public class DefaultOrderService implements OrderService {

    private final OrderRepository     orderRepository;
    private final RestaurantService   restaurantService;
    private final PaymentService      paymentService;
    private final NotificationService notificationService;

    public DefaultOrderService(OrderRepository     orderRepository,
                               RestaurantService   restaurantService,
                               PaymentService      paymentService,
                               NotificationService notificationService) {

        this.orderRepository     = Objects.requireNonNull(orderRepository,     "orderRepository");
        this.restaurantService   = Objects.requireNonNull(restaurantService,   "restaurantService");
        this.paymentService      = Objects.requireNonNull(paymentService,      "paymentService");
        this.notificationService = Objects.requireNonNull(notificationService, "notificationService");
    }

    @Override
    public Order placeOrder(OrderRequest request) {
        Objects.requireNonNull(request, "request must not be null");

        // Step 1 — resolve restaurant + build item snapshots via OrderItemMapper
        Restaurant restaurant = restaurantService.getRestaurant(request.restaurantId());
        List<OrderItem> items = OrderItemMapper.from(restaurant, request.items());

        // Step 2 — create aggregate and persist in PLACED state
        Order order = new Order(
                IdGenerator.nextId("ORD"),
                request.customerId(),
                request.restaurantId(),
                items
        );
        order.transitionTo(OrderStatus.PLACED);
        orderRepository.save(order);

        // Step 3 — process payment
        PaymentResult payment = paymentService.processPayment(
                request.customerId(), order.totalAmount());

        if (!payment.success()) {
            order.transitionTo(OrderStatus.PAYMENT_FAILED);
            orderRepository.save(order);
            notificationService.notifyUser(
                    request.customerId(),
                    "Payment failed for order " + order.orderId()
                    + ": " + payment.message());
            return order;
        }

        // Step 4 — confirm and notify
        order.transitionTo(OrderStatus.CONFIRMED);
        orderRepository.save(order);
        notificationService.notifyUser(
                request.customerId(),
                "Order " + order.orderId() + " confirmed!");

        return order;
    }

    @Override
    public Order getOrder(String orderId) {
        Objects.requireNonNull(orderId, "orderId must not be null");
        return orderRepository.findById(orderId)
                .orElseThrow(() ->
                    new IllegalArgumentException("Order not found: " + orderId));
    }

    @Override
    public void updateStatus(String orderId, OrderStatus newStatus) {
        Objects.requireNonNull(newStatus, "newStatus must not be null");
        Order order = getOrder(orderId);
        order.transitionTo(newStatus);
        orderRepository.save(order);
        notificationService.notifyUser(
                order.customerId(),
                "Your order " + orderId + " is now: " + newStatus + ".");
    }

    @Override
    public void cancelOrder(String orderId) {
        Order order = getOrder(orderId);
        order.transitionTo(OrderStatus.CANCELLED);
        orderRepository.save(order);
        notificationService.notifyUser(
                order.customerId(),
                "Your order " + orderId + " has been cancelled.");
    }

    @Override
    public List<Order> getOrdersByCustomer(String customerId) {
        Objects.requireNonNull(customerId, "customerId must not be null");
        return orderRepository.findByCustomerId(customerId);
    }

    @Override
    public List<Order> getOrdersByRestaurant(String restaurantId, OrderStatus status) {
        Objects.requireNonNull(restaurantId, "restaurantId must not be null");
        Objects.requireNonNull(status,       "status must not be null");
        return orderRepository.findByRestaurantIdAndStatus(restaurantId, status);
    }

    @Override
    public List<Order> getOrdersByStatus(OrderStatus status) {
        Objects.requireNonNull(status, "status must not be null");
        return orderRepository.findByStatus(status);
    }
}
