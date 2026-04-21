package edu.classproject.order;

import edu.classproject.common.Money;

import java.util.Objects;

/**
 * Immutable snapshot of one menu item as captured at order time.
 *
 * Prices are snapshotted deliberately: menu changes must never affect
 * existing orders.
 *
 * GRASP Information Expert: computes its own lineTotal() because it
 *   holds both unitPrice and quantity.
 * SOLID SRP: handles only line-level price arithmetic.
 */
public final class OrderItem {

    private final String itemId;
    private final String itemName;
    private final Money  unitPrice;
    private final int    quantity;

    public OrderItem(String itemId, String itemName, Money unitPrice, int quantity) {
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException("itemId must not be blank");
        }
        if (itemName == null || itemName.isBlank()) {
            throw new IllegalArgumentException("itemName must not be blank");
        }
        Objects.requireNonNull(unitPrice, "unitPrice must not be null");
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                "quantity must be > 0, got: " + quantity);
        }
        this.itemId    = itemId;
        this.itemName  = itemName;
        this.unitPrice = unitPrice;
        this.quantity  = quantity;
    }

    public String itemId()    { return itemId; }
    public String itemName()  { return itemName; }
    public Money  unitPrice() { return unitPrice; }
    public int    quantity()  { return quantity; }

    /**
     * Returns unitPrice × quantity.
     * GRASP Information Expert: only this class needs both values,
     * so only this class should perform this calculation.
     */
    public Money lineTotal() {
        return unitPrice.multiply(quantity);
    }

    @Override
    public String toString() {
        return String.format("OrderItem{id='%s', name='%s', qty=%d, unit=%s, line=%s}",
                itemId, itemName, quantity, unitPrice, lineTotal());
    }
}
