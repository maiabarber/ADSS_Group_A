package tests;

import domain.DeliveryDocument;
import domain.DeliveryItem;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DeliveryDocumentTest {

    private DeliveryItem createMilkItem(int quantity) {
        return new DeliveryItem("ITEM-001", "Milk 3%", quantity);
    }

    private DeliveryItem createBreadItem(int quantity) {
        return new DeliveryItem("ITEM-002", "Whole Wheat Bread", quantity);
    }

    private DeliveryDocument createValidDocument() {
        List<DeliveryItem> items = new ArrayList<>();
        items.add(createMilkItem(10));
        items.add(createBreadItem(5));
        return new DeliveryDocument(1001, items);
    }

    @Test
    void constructor_validInput_createsDocumentSuccessfully() {
        DeliveryDocument document = createValidDocument();

        assertNotNull(document);
        assertEquals(1001, document.getDocumentNumber());
        assertEquals(2, document.getItems().size());
    }

    @Test
    void constructor_documentNumberNotPositive_throwsException() {
        List<DeliveryItem> items = new ArrayList<>();
        items.add(createMilkItem(10));

        assertThrows(IllegalArgumentException.class, () ->
                new DeliveryDocument(0, items)
        );
    }

    @Test
    void constructor_negativeDocumentNumber_throwsException() {
        List<DeliveryItem> items = new ArrayList<>();
        items.add(createMilkItem(10));

        assertThrows(IllegalArgumentException.class, () ->
                new DeliveryDocument(-5, items)
        );
    }

    @Test
    void constructor_nullItems_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new DeliveryDocument(1001, null)
        );
    }

    @Test
    void constructor_copiesInputItemsListAndDoesNotKeepOriginalReference() {
        List<DeliveryItem> originalItems = new ArrayList<>();
        originalItems.add(createMilkItem(10));

        DeliveryDocument document = new DeliveryDocument(1001, originalItems);

        originalItems.add(createBreadItem(5));

        assertEquals(1, document.getItems().size());
    }

    @Test
    void getItems_returnsCopyAndNotOriginalList() {
        DeliveryDocument document = createValidDocument();

        List<DeliveryItem> itemsFromGetter = document.getItems();
        itemsFromGetter.add(new DeliveryItem("ITEM-003", "Shampoo 250ml", 7));

        assertEquals(2, document.getItems().size());
    }

    @Test
    void getItemsDelivered_legacyGetter_returnsSameLogicalContent() {
        DeliveryDocument document = createValidDocument();

        assertEquals(document.getItems().size(), document.getItemsDelivered().size());
    }

    @Test
    void addItem_newItem_addsItemSuccessfully() {
        DeliveryDocument document = createValidDocument();

        document.addItem(new DeliveryItem("ITEM-003", "Shampoo 250ml", 7));

        assertEquals(3, document.getItems().size());
        assertTrue(document.containsItem("ITEM-003"));
    }

    @Test
    void addItem_existingItemId_mergesQuantities() {
        DeliveryDocument document = createValidDocument();

        document.addItem(new DeliveryItem("ITEM-001", "Milk 3%", 4));

        assertEquals(2, document.getItems().size());

        DeliveryItem milkItem = document.getItems().stream()
                .filter(item -> item.getItemId().equals("ITEM-001"))
                .findFirst()
                .orElseThrow();

        assertEquals(14, milkItem.getQuantity());
    }

    @Test
    void addItem_nullItem_throwsException() {
        DeliveryDocument document = createValidDocument();

        assertThrows(IllegalArgumentException.class, () ->
                document.addItem(null)
        );
    }

    @Test
    void removeItem_partialQuantity_removesOnlyRequestedAmount() {
        DeliveryDocument document = createValidDocument();

        document.removeItem("ITEM-001", 4);

        DeliveryItem milkItem = document.getItems().stream()
                .filter(item -> item.getItemId().equals("ITEM-001"))
                .findFirst()
                .orElseThrow();

        assertEquals(6, milkItem.getQuantity());
    }

    @Test
    void removeItem_fullQuantity_removesItemCompletely() {
        DeliveryDocument document = createValidDocument();

        document.removeItem("ITEM-002", 5);

        assertFalse(document.containsItem("ITEM-002"));
        assertEquals(1, document.getItems().size());
    }

    @Test
    void removeItem_nullItemId_throwsException() {
        DeliveryDocument document = createValidDocument();

        assertThrows(IllegalArgumentException.class, () ->
                document.removeItem(null, 1)
        );
    }

    @Test
    void removeItem_blankItemId_throwsException() {
        DeliveryDocument document = createValidDocument();

        assertThrows(IllegalArgumentException.class, () ->
                document.removeItem("   ", 1)
        );
    }

    @Test
    void removeItem_zeroQuantity_throwsException() {
        DeliveryDocument document = createValidDocument();

        assertThrows(IllegalArgumentException.class, () ->
                document.removeItem("ITEM-001", 0)
        );
    }

    @Test
    void removeItem_negativeQuantity_throwsException() {
        DeliveryDocument document = createValidDocument();

        assertThrows(IllegalArgumentException.class, () ->
                document.removeItem("ITEM-001", -1)
        );
    }

    @Test
    void removeItem_quantityGreaterThanExisting_throwsException() {
        DeliveryDocument document = createValidDocument();

        assertThrows(IllegalArgumentException.class, () ->
                document.removeItem("ITEM-001", 11)
        );
    }

    @Test
    void removeItem_itemDoesNotExist_throwsException() {
        DeliveryDocument document = createValidDocument();

        assertThrows(IllegalArgumentException.class, () ->
                document.removeItem("ITEM-999", 1)
        );
    }

    @Test
    void containsItem_existingItem_returnsTrue() {
        DeliveryDocument document = createValidDocument();

        assertTrue(document.containsItem("ITEM-001"));
    }

    @Test
    void containsItem_nonExistingItem_returnsFalse() {
        DeliveryDocument document = createValidDocument();

        assertFalse(document.containsItem("ITEM-999"));
    }

    @Test
    void containsItem_nullItemId_throwsException() {
        DeliveryDocument document = createValidDocument();

        assertThrows(IllegalArgumentException.class, () ->
                document.containsItem(null)
        );
    }

    @Test
    void containsItem_blankItemId_throwsException() {
        DeliveryDocument document = createValidDocument();

        assertThrows(IllegalArgumentException.class, () ->
                document.containsItem("   ")
        );
    }

    @Test
    void getTotalDifferentItems_returnsCorrectCount() {
        DeliveryDocument document = createValidDocument();

        assertEquals(2, document.getTotalDifferentItems());
    }
}