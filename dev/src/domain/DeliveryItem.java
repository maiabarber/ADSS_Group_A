package domain;

public class DeliveryItem{
    
    private String itemId;
    private String itemName;
    private int quantity;

    public DeliveryItem(String itemId, String itemName, int quantity){
        if (itemId == null || itemId.isBlank()){
            throw new IllegalArgumentException("itemId cannot be empty");
        }
        if (itemName == null || itemName.isBlank()){
            throw new IllegalArgumentException("itemName cannot be empty");
        }
        if (quantity < 0){
            throw new IllegalArgumentException("quantity cannot be negative");
        }
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
    }

    public String getItemId(){
        return this.itemId;
    }

    public String getItemName(){
        return this.itemName;
    }

    public int getQuantity(){
        return this.quantity;
    }

    public void setQuantity(int quantity){
        if (quantity < 0){
            throw new IllegalArgumentException("quantity cannot be negative");
        }
        this.quantity = quantity;
    }


}
