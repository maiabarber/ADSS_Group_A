package domain;

import java.util.Objects;

public class DeliveryItem {

    private String itemId;
    private String itemName;
    private int quantity;

    public DeliveryItem(String itemId, String itemName, int quantity) {
        validateItemId(itemId);
        validateItemName(itemName);
        validateQuantity(quantity);

        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
    }

    public String getItemId() {
        return itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setItemName(String itemName) {
        validateItemName(itemName);
        this.itemName = itemName;
    }

    public void setQuantity(int quantity) {
        validateQuantity(quantity);
        this.quantity = quantity;
    }

    public void increaseQuantity(int amountToAdd) {
        if (amountToAdd <= 0) {
            throw new IllegalArgumentException("amountToAdd must be positive");
        }
        this.quantity += amountToAdd;
    }

    public void decreaseQuantity(int amountToRemove) {
        if (amountToRemove <= 0) {
            throw new IllegalArgumentException("amountToRemove must be positive");
        }
        if (amountToRemove > this.quantity) {
            throw new IllegalArgumentException("cannot remove more than existing quantity");
        }
        this.quantity -= amountToRemove;
    }

    private void validateItemId(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException("itemId cannot be empty");
        }
    }

    private void validateItemName(String itemName) {
        if (itemName == null || itemName.isBlank()) {
            throw new IllegalArgumentException("itemName cannot be empty");
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity cannot be negative");
        }
    }

    @Override
    public String toString() {
        return "DeliveryItem{" +
                "itemId='" + itemId + '\'' +
                ", itemName='" + itemName + '\'' +
                ", quantity=" + quantity +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof DeliveryItem other)) return false;
        return Objects.equals(itemId, other.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId);
    }
}