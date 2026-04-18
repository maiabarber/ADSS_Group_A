package domain;

import java.util.ArrayList;
import java.util.List;

public class DeliveryDocument{
    
    private int documentNumber;
    private List<DeliveryItem> itemsDelivered;

    public DeliveryDocument(int documentNumber, List<DeliveryItem> itemsDelivered){
        if (itemsDelivered == null) {
            throw new IllegalArgumentException("itemsDelivered cannot be null");
        }
        this.documentNumber = documentNumber;
        this.itemsDelivered = new ArrayList<>(itemsDelivered);
    }

    public int getDocumentNumber(){
        return this.documentNumber;
    }

    public List<DeliveryItem> getItemsDelivered(){
        return new ArrayList<>(this.itemsDelivered);
    }

    public void addItem(DeliveryItem item){
        if (item == null) {
            throw new IllegalArgumentException("item cannot be null");
        }
        
        for (DeliveryItem existingItem : this.itemsDelivered) {
            if (existingItem.getItemId().equals(item.getItemId())){
                existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                return;
            }
        }
        this.itemsDelivered.add(item);
    }

    public void removeItem(String itemId, int quantityToRemove){
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException("itemId cannot be empty");
        }
        if (quantityToRemove <= 0) {
            throw new IllegalArgumentException("quantityToRemove must be positive");
        }

        for (int i = 0; i < itemsDelivered.size(); i++){
            DeliveryItem item = itemsDelivered.get(i);
            if (item.getItemId().equals(itemId)) {
                int newQuantity = item.getQuantity() - quantityToRemove;

                if (newQuantity > 0){
                    item.setQuantity(newQuantity);
                } else if (newQuantity == 0){
                    itemsDelivered.remove(i);
                } 
                else{
                    throw new IllegalArgumentException("Cannot remove more than existing quantity");
                }
                return;
            }
        }
    }



}
