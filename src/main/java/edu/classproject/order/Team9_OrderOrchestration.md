# Team 9 — Order Orchestration Module

## Introduction

This document covers the **Order Orchestration** module, developed by Team 9 as part of an Object-Oriented Analysis & Design (OOAD) mini project for a Food Delivery Application.

The Order Orchestration module is responsible for managing the complete lifecycle of a food delivery order — from the moment a customer places a request, through payment processing, kitchen preparation, dispatch, and final delivery. It acts as the central coordinator between several external services: the Restaurant Service (menu/item resolution), the Payment Service (transaction processing), and the Notification Service (customer alerts).

The module is built around a **Domain-Driven Design (DDD)** approach, with `Order` as the core aggregate, a strict state machine enforcing all valid order transitions, and a clean service layer (`DefaultOrderService`) that depends only on interfaces — making the system highly testable and extensible.

---

## Class Diagram

The following class diagram shows the full structure of the Order Orchestration module, including all entities, value objects, interfaces, and their relationships.

![Class Diagram](ClassDiagram.png)

**Key classes and their roles:**

- **`Order`** — The aggregate root. Holds customer/restaurant info, a list of `OrderItem`s, and the current `OrderStatus`. Enforces state transitions internally.
- **`OrderItem`** — Represents a snapshotted line item (itemId, name, price at time of order, quantity). Computes its own `lineTotal()`.
- **`OrderStatus`** — Enum of all possible states: `CREATED`, `PLACED`, `CONFIRMED`, `PREPARING`, `READY_FOR_PICKUP`, `OUT_FOR_DELIVERY`, `DELIVERED`, `PAYMENT_FAILED`, `CANCELLED`.
- **`DefaultOrderService`** — The main service class implementing `OrderService`. Orchestrates place, update, cancel, and query operations.
- **`InMemoryOrderRepository`** — An in-memory implementation of `OrderRepository` for demo and test use.
- **`OrderItemMapper`** — Utility that maps `OrderRequestItem`s to `OrderItem`s by resolving menu items from the restaurant.
- **`OrderRequest` / `OrderRequestItem`** — Immutable records carrying the input data for a new order.
- **`Money`** — Value object wrapping `BigDecimal` for monetary calculations.

---

## Part I — Functional Requirements

### 1. Place Order

- **FR-1.1** The system shall accept an `OrderRequest` containing a non-blank `customerId`, a non-blank `restaurantId`, and at least one `OrderRequestItem`.
- **FR-1.2** The system shall reject a null `OrderRequest` with a `NullPointerException` carrying the message `"request must not be null"`.
- **FR-1.3** The system shall reject a null/blank `customerId`, null/blank `restaurantId`, or empty items list with an `IllegalArgumentException` at `OrderRequest` construction time — before any service method is called.
- **FR-1.4** The system shall reject an `OrderRequest` referencing a non-existent restaurant with an `IllegalArgumentException`.
- **FR-1.5** The system shall reject an `OrderRequest` referencing a non-existent menu item ID with an `IllegalArgumentException`.
- **FR-1.6** Each `OrderRequestItem` shall enforce a non-blank `menuItemId` and a quantity greater than zero at construction time via its compact constructor, throwing `IllegalArgumentException` on violation.
- **FR-1.7** The system shall resolve each requested menu item from the restaurant's menu and snapshot the item ID, name, and price at the time of ordering, so future menu changes do not affect existing orders.
- **FR-1.8** The system shall create the order in `CREATED` state, immediately transition it to `PLACED`, and persist it before attempting payment.
- **FR-1.9** The system shall compute the order total as the sum of `unitPrice × quantity` across all line items.
- **FR-1.10** On successful payment, the system shall transition the order to `CONFIRMED`, persist it, and send the customer a notification containing the word "confirmed".
- **FR-1.11** On failed payment, the system shall transition the order to `PAYMENT_FAILED`, persist it, and send the customer a notification containing the words "Payment failed".
- **FR-1.12** The persisted order shall be retrievable by its order ID immediately after `placeOrder` returns.

### 2. Order Item Rules

- **FR-2.1** An `OrderItem` shall enforce at construction time: a non-blank `itemId`, non-blank `itemName`, non-null `unitPrice`, and a quantity greater than zero — each violation throws `IllegalArgumentException`.
- **FR-2.2** An `OrderItem` shall compute its own `lineTotal()` as `unitPrice × quantity`.
- **FR-2.3** An `Order` shall compute `totalAmount()` as the sum of all item `lineTotal()` values.
- **FR-2.4** The items list returned by `Order.items()` shall be unmodifiable — external callers cannot add, remove, or replace items.

### 3. State Machine

- **FR-3.1** Every `Order` shall start in `CREATED` state upon construction.
- **FR-3.2** The system shall enforce the following — and only the following — legal transitions:

| From State | To State(s) |
|---|---|
| `CREATED` | `PLACED` |
| `PLACED` | `CONFIRMED`, `PAYMENT_FAILED` |
| `CONFIRMED` | `PREPARING`, `CANCELLED` |
| `PREPARING` | `READY_FOR_PICKUP` |
| `READY_FOR_PICKUP` | `OUT_FOR_DELIVERY` |
| `OUT_FOR_DELIVERY` | `DELIVERED` |
| `DELIVERED` | *(terminal)* |
| `PAYMENT_FAILED` | *(terminal)* |
| `CANCELLED` | *(terminal)* |

- **FR-3.3** Any attempt to perform an illegal transition shall throw an `IllegalStateException`.
- **FR-3.4** Any attempt to transition out of a terminal state shall throw an `IllegalStateException`.
- **FR-3.5** The full legal delivery lifecycle — `CONFIRMED → PREPARING → READY_FOR_PICKUP → OUT_FOR_DELIVERY → DELIVERED` — shall complete without error.

### 4. Update Status

- **FR-4.1** The system shall advance an order to a given new status, provided the transition is legal.
- **FR-4.2** On a successful status update, the system shall notify the customer with a message containing the new status name.
- **FR-4.3** The system shall throw `IllegalArgumentException` if the order ID does not exist.
- **FR-4.4** The system shall throw `IllegalStateException` if the requested transition is illegal.
- **FR-4.5** The updated status shall be persisted and visible when the order is subsequently retrieved.

### 5. Cancel Order

- **FR-5.1** The system shall allow cancellation of an order in `CONFIRMED` state only, transitioning it to `CANCELLED` and persisting the result.
- **FR-5.2** On cancellation, the system shall notify the customer with a message containing the word "cancelled".
- **FR-5.3** The system shall throw `IllegalStateException` when attempting to cancel an order that has reached a terminal state.
- **FR-5.4** The system shall throw `IllegalArgumentException` when attempting to cancel a non-existent order ID.

### 6. Query Orders

- **FR-6.1** The system shall return all orders belonging to a given customer ID.
- **FR-6.2** The system shall return an empty list when a customer has no orders.
- **FR-6.3** The system shall return all orders for a given restaurant in a specified status.
- **FR-6.4** The system shall return an empty list when no orders match.
- **FR-6.5** The system shall return all orders system-wide in a specified status.
- **FR-6.6** The system shall return an empty list when no orders exist in the requested status.

### 7. Dependency & Construction Rules

- **FR-7.1** `DefaultOrderService` shall require four dependencies at construction: `OrderRepository`, `RestaurantService`, `PaymentService`, `NotificationService`.
- **FR-7.2** Passing null for any dependency shall throw `NullPointerException`.
- **FR-7.3** `DefaultOrderService` shall depend only on interfaces.

### 8. Persistence

- **FR-8.1** `InMemoryOrderRepository` shall store orders by order ID using `save()` upsert.
- **FR-8.2** `findById()` shall return `Optional` — empty if not found.
- **FR-8.3** `findAll()` shall return all stored orders.
- **FR-8.4** `InMemoryOrderRepository` is not thread-safe and intended for demo/test use only.

### 9. Notifications

- **FR-9.1** The system shall send notifications on: order confirmed, payment failed, status updated, and order cancelled.
- **FR-9.2** Notification failures are not suppressed — exceptions propagate to the caller.

### 10. ID Generation

- **FR-10.1** Order IDs shall follow the format `ORD-XXXXXXXX`, generated using `IdGenerator.nextId("ORD")` with the first 8 characters of a UUID.

---

## Part II — Non-Functional Requirements

### NFR-1. Reliability & Correctness

The state machine enforces only the defined legal transitions with zero tolerance for illegal state changes — all violations throw `IllegalStateException` immediately via `Order.transitionTo()`. Order total computation is deterministic and exact, using price snapshots taken at order time via `OrderItem.lineTotal()`, immune to future menu changes. Payment outcomes always result in a persisted state change (`CONFIRMED` or `PAYMENT_FAILED`) — no order is left in `PLACED` after `processPayment()` returns.

### NFR-2. Fail-Fast & Defensive Behavior

All invalid inputs (null, blank, empty, out-of-range) are rejected at construction time — violations surface as `IllegalArgumentException` or `NullPointerException` with descriptive messages. This is enforced in `OrderItem`, `OrderRequest`, `OrderRequestItem`, and the `DefaultOrderService` constructor. Notification failures propagate naturally to the caller — `DefaultOrderService` does not wrap `notifyUser()` calls in try-catch.

### NFR-3. Maintainability & Design Quality

`DefaultOrderService` depends only on interfaces (`OrderRepository`, `RestaurantService`, `PaymentService`, `NotificationService`), never on concrete implementations — enabling substitution without modifying service code. All domain rules (state transitions, line total computation, immutability of item lists) are encapsulated within the aggregate (`Order`, `OrderItem`), not scattered across service classes.

### NFR-4. Testability

The in-memory repository (`InMemoryOrderRepository`) serves as a fully functional test double — supporting `save`, `findById`, `findByCustomerId`, `findByRestaurantIdAndStatus`, `findByStatus`, and `findAll` without external dependencies. All dependencies are injectable at construction time, making the service fully testable with lambda stubs or inner-class fakes — no mocking framework required.

### NFR-5. Data Integrity

The items list exposed by `Order.items()` is unmodifiable — external callers cannot mutate order contents after creation (`Collections.unmodifiableList()` is used internally). Order IDs follow the format `ORD-XXXXXXXX` (prefix + first 8 hex characters of a random UUID), providing low collision probability suitable for single-process demo and test environments.

### NFR-6. Extensibility

The notification contract supports multiple notification events via a single `NotificationService.notifyUser()` interface. Adding a new event requires adding a `notifyUser()` call in `DefaultOrderService` at the appropriate point in the flow. Query capabilities are composable by axis — by customer, by restaurant and status, or system-wide by status — without coupling query logic to order creation logic.

### NFR-7. Scope & Concurrency Constraints

`InMemoryOrderRepository` is explicitly not thread-safe and is scoped to demo and test environments only. Production deployment would require a thread-safe or persistent repository implementation provided as a new class implementing `OrderRepository`.

---

## Sequence Diagrams

### Place Order — Full Workflow

The Place Order flow involves four major steps: resolving menu items from the restaurant, creating the order aggregate in `PLACED` state, processing payment, and then either confirming the order or marking it as `PAYMENT_FAILED` depending on the payment outcome.

![Place Order Sequence Diagram](place_order.png)

**Flow summary:**
1. Customer submits an order with `customerId`, `restaurantId`, and items.
2. `DefaultOrderService` calls `RestaurantService.getRestaurant()` to validate the restaurant, then `OrderItemMapper.from()` to resolve and snapshot each menu item.
3. A new `Order` aggregate is created in `CREATED` state and immediately transitioned to `PLACED`, then persisted.
4. `PaymentService.processPayment()` is called with the customer ID and total amount.
   - **Payment succeeds:** order transitions to `CONFIRMED`, is saved, and customer is notified.
   - **Payment fails:** order transitions to `PAYMENT_FAILED`, is saved, and customer is notified.

---

### Update Order Status

The Update Status flow allows Kitchen staff, Dispatch, or Drivers to advance an order through the delivery lifecycle. The state machine on the `Order` aggregate validates every transition.

![Update Order Status Sequence Diagram](update_order.png)

**Flow summary:**
1. Kitchen / Dispatch / Driver calls `updateStatus(orderId, newStatus)`.
2. `DefaultOrderService` retrieves the order via `OrderRepository.findById()`.
   - **Order not found:** `IllegalArgumentException` is thrown.
3. `Order.transitionTo(newStatus)` is called. The state machine validates the transition.
   - **Transition legal:** order is saved and customer is notified with the new status.
   - **Transition illegal:** `IllegalStateException` is thrown.

---

### Cancel Order

The Cancel Order flow allows a customer to cancel their order, but only if it has not yet reached a terminal state. Cancellation is only permitted from `CONFIRMED` or `PREPARING` states.

![Cancel Order Sequence Diagram](cancel_diagram.png)

**Flow summary:**
1. Customer calls `cancelOrder(orderId)`.
2. `DefaultOrderService` retrieves the order via `OrderRepository.findById()`.
   - **Order not found:** `IllegalArgumentException` is thrown.
3. `Order.transitionTo(CANCELLED)` is called.
   - **Order is non-terminal (`CONFIRMED` or `PREPARING`):** transition is legal — order is saved and customer notified with "order cancelled".
   - **Order is terminal (`DELIVERED`, `PAYMENT_FAILED`, `CANCELLED`):** no transitions are allowed — `IllegalStateException` is thrown.
