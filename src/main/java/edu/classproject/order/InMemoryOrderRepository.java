package edu.classproject.order;

import edu.classproject.common.OrderStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * In-process, HashMap-backed implementation of OrderRepository.
 *
 * Suitable for unit tests and demos. Replace with a database-backed
 * implementation by providing a new class — nothing else changes.
 *
 * SOLID OCP: switching storage is additive; no existing class is modified.
 * SOLID LSP: fully substitutable for any OrderRepository.
 * GRASP Pure Fabrication: encapsulates all storage mechanics so that
 *   domain objects remain clean.
 *
 * Thread-safety: not synchronised — single-threaded use only.
 */
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<String, Order> store = new HashMap<>();

    @Override
    public void save(Order order) {
        Objects.requireNonNull(order, "order must not be null");
        store.put(order.orderId(), order);
    }

    @Override
    public Optional<Order> findById(String orderId) {
        Objects.requireNonNull(orderId, "orderId must not be null");
        return Optional.ofNullable(store.get(orderId));
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        Objects.requireNonNull(customerId, "customerId must not be null");
        return store.values().stream()
                .filter(o -> customerId.equals(o.customerId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByRestaurantIdAndStatus(String restaurantId, OrderStatus status) {
        Objects.requireNonNull(restaurantId, "restaurantId must not be null");
        Objects.requireNonNull(status, "status must not be null");
        return store.values().stream()
                .filter(o -> restaurantId.equals(o.restaurantId())
                          && status == o.status())
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        Objects.requireNonNull(status, "status must not be null");
        return store.values().stream()
                .filter(o -> status == o.status())
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> findAll() {
        return new ArrayList<>(store.values());
    }
}
