package dataaccess.dto;

public final class DeliveryItemDto {
    private final String itemId;
    private final int documentNumber;
    private final String itemName;
    private final int quantity;

    public DeliveryItemDto(String itemId, int documentNumber, String itemName, int quantity) {
        this.itemId = itemId;
        this.documentNumber = documentNumber;
        this.itemName = itemName;
        this.quantity = quantity;
    }

    public String getItemId() {
        return itemId;
    }

    public int getDocumentNumber() {
        return documentNumber;
    }

    public String getItemName() {
        return itemName;
    }

    public int getQuantity() {
        return quantity;
    }
}