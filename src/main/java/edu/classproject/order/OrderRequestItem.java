package edu.classproject.order;

/**
 * A single line in a customer's order request.
 *
 * GRASP Information Expert: owns and validates its own data.
 * SOLID SRP: pure value carrier — no orchestration logic.
 */
public record OrderRequestItem(String menuItemId, int quantity) {

    public OrderRequestItem {
        if (menuItemId == null || menuItemId.isBlank()) {
            throw new IllegalArgumentException("menuItemId must not be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                "quantity must be > 0, got: " + quantity);
        }
    }
}
