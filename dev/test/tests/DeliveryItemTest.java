package tests;

import domain.DeliveryItem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DeliveryItemTest {

    private DeliveryItem createValidItem() {
        return new DeliveryItem("ITEM-001", "Milk 3%", 10);
    }

    @Test
    void constructor_validInput_createsItemSuccessfully() {
        DeliveryItem item = createValidItem();

        assertNotNull(item);
        assertEquals("ITEM-001", item.getItemId());
        assertEquals("Milk 3%", item.getItemName());
        assertEquals(10, item.getQuantity());
    }

    @Test
    void constructor_nullItemId_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new DeliveryItem(null, "Milk 3%", 10)
        );
    }

    @Test
    void constructor_blankItemId_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new DeliveryItem("   ", "Milk 3%", 10)
        );
    }

    @Test
    void constructor_nullItemName_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new DeliveryItem("ITEM-001", null, 10)
        );
    }

    @Test
    void constructor_blankItemName_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new DeliveryItem("ITEM-001", "   ", 10)
        );
    }

    @Test
    void constructor_negativeQuantity_throwsException() {
        assertThrows(IllegalArgumentException.class, () ->
                new DeliveryItem("ITEM-001", "Milk 3%", -1)
        );
    }

    @Test
    void setItemName_validName_updatesName() {
        DeliveryItem item = createValidItem();

        item.setItemName("Chocolate Milk");

        assertEquals("Chocolate Milk", item.getItemName());
    }

    @Test
    void setItemName_nullName_throwsException() {
        DeliveryItem item = createValidItem();

        assertThrows(IllegalArgumentException.class, () ->
                item.setItemName(null)
        );
    }

    @Test
    void setItemName_blankName_throwsException() {
        DeliveryItem item = createValidItem();

        assertThrows(IllegalArgumentException.class, () ->
                item.setItemName("   ")
        );
    }

    @Test
    void setQuantity_validQuantity_updatesQuantity() {
        DeliveryItem item = createValidItem();

        item.setQuantity(25);

        assertEquals(25, item.getQuantity());
    }

    @Test
    void setQuantity_zeroQuantity_updatesQuantity() {
        DeliveryItem item = createValidItem();

        item.setQuantity(0);

        assertEquals(0, item.getQuantity());
    }

    @Test
    void setQuantity_negativeQuantity_throwsException() {
        DeliveryItem item = createValidItem();

        assertThrows(IllegalArgumentException.class, () ->
                item.setQuantity(-5)
        );
    }

    @Test
    void increaseQuantity_positiveAmount_increasesQuantity() {
        DeliveryItem item = createValidItem();

        item.increaseQuantity(5);

        assertEquals(15, item.getQuantity());
    }

    @Test
    void increaseQuantity_zeroAmount_throwsException() {
        DeliveryItem item = createValidItem();

        assertThrows(IllegalArgumentException.class, () ->
                item.increaseQuantity(0)
        );
    }

    @Test
    void increaseQuantity_negativeAmount_throwsException() {
        DeliveryItem item = createValidItem();

        assertThrows(IllegalArgumentException.class, () ->
                item.increaseQuantity(-1)
        );
    }

    @Test
    void decreaseQuantity_validAmount_decreasesQuantity() {
        DeliveryItem item = createValidItem();

        item.decreaseQuantity(4);

        assertEquals(6, item.getQuantity());
    }

    @Test
    void decreaseQuantity_whenAmountEqualsQuantity_setsQuantityToZero() {
        DeliveryItem item = createValidItem();

        item.decreaseQuantity(10);

        assertEquals(0, item.getQuantity());
    }

    @Test
    void decreaseQuantity_zeroAmount_throwsException() {
        DeliveryItem item = createValidItem();

        assertThrows(IllegalArgumentException.class, () ->
                item.decreaseQuantity(0)
        );
    }

    @Test
    void decreaseQuantity_negativeAmount_throwsException() {
        DeliveryItem item = createValidItem();

        assertThrows(IllegalArgumentException.class, () ->
                item.decreaseQuantity(-1)
        );
    }

    @Test
    void decreaseQuantity_whenAmountGreaterThanQuantity_throwsException() {
        DeliveryItem item = createValidItem();

        assertThrows(IllegalArgumentException.class, () ->
                item.decreaseQuantity(11)
        );
    }

    @Test
    void equals_whenItemIdsAreEqual_returnsTrue() {
        DeliveryItem item1 = new DeliveryItem("ITEM-001", "Milk 3%", 10);
        DeliveryItem item2 = new DeliveryItem("ITEM-001", "Another Name", 999);

        assertEquals(item1, item2);
    }

    @Test
    void equals_whenItemIdsAreDifferent_returnsFalse() {
        DeliveryItem item1 = new DeliveryItem("ITEM-001", "Milk 3%", 10);
        DeliveryItem item2 = new DeliveryItem("ITEM-002", "Milk 3%", 10);

        assertNotEquals(item1, item2);
    }

    @Test
    void hashCode_whenItemIdsAreEqual_isEqual() {
        DeliveryItem item1 = new DeliveryItem("ITEM-001", "Milk 3%", 10);
        DeliveryItem item2 = new DeliveryItem("ITEM-001", "Another Name", 999);

        assertEquals(item1.hashCode(), item2.hashCode());
    }
}