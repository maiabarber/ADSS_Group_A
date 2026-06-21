package domain;

import java.util.ArrayList;
import java.util.List;

public class DeliveryDocument {

    private int documentNumber;
    private List<DeliveryItem> items;

    public DeliveryDocument(int documentNumber, List<DeliveryItem> items) {
        validateDocumentNumber(documentNumber);

        if (items == null) {
            throw new IllegalArgumentException("items cannot be null");
        }

        this.documentNumber = documentNumber;
        this.items = new ArrayList<>();

        for (DeliveryItem item : items) {
            addItem(item);
        }
    }

    public int getDocumentNumber() {
        return documentNumber;
    }

    public List<DeliveryItem> getItems() {
        return new ArrayList<>(items);
    }

    public List<DeliveryItem> getItemsDelivered() {
        return getItems();
    }

    public void addItem(DeliveryItem item) {
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }

        for (DeliveryItem existingItem : items) {
            if (existingItem.getItemId().equals(item.getItemId())) {
                existingItem.increaseQuantity(item.getQuantity());
                return;
            }
        }

        items.add(new DeliveryItem(item.getItemId(), item.getItemName(), item.getQuantity()));
    }

    public void removeItem(String itemId, int quantityToRemove) {
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException("itemId cannot be empty");
        }
        if (quantityToRemove <= 0) {
            throw new IllegalArgumentException("quantityToRemove must be positive");
        }

        for (int i = 0; i < items.size(); i++) {
            DeliveryItem item = items.get(i);
            if (item.getItemId().equals(itemId)) {
                if (quantityToRemove > item.getQuantity()) {
                    throw new IllegalArgumentException("cannot remove more than existing quantity");
                }

                if (quantityToRemove == item.getQuantity()) {
                    items.remove(i);
                } else {
                    item.decreaseQuantity(quantityToRemove);
                }
                return;
            }
        }

        throw new IllegalArgumentException("item with given id was not found in document");
    }

    public boolean containsItem(String itemId) {
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException("itemId cannot be empty");
        }

        for (DeliveryItem item : items) {
            if (item.getItemId().equals(itemId)) {
                return true;
            }
        }
        return false;
    }

    public int getTotalDifferentItems() {
        return items.size();
    }

    private void validateDocumentNumber(int documentNumber) {
        if (documentNumber <= 0) {
            throw new IllegalArgumentException("documentNumber must be positive");
        }
    }

    @Override
    public String toString() {
        return "DeliveryDocument{" +
                "documentNumber=" + documentNumber +
                ", items=" + items +
                '}';
    }
}