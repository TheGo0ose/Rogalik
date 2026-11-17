package io.github.some_example_name.item;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryTest {

    @Test
    void refusesItemsBeyondCapacity() {
        Inventory inventory = new Inventory();
        for (int i = 0; i < Inventory.CAPACITY; i++) {
            assertTrue(inventory.add(Items.healingPotion()));
        }
        assertTrue(inventory.isFull());
        assertFalse(inventory.add(Items.rareSword()));
        assertEquals(Inventory.CAPACITY, inventory.size());
    }

    @Test
    void itemListCannotBeModifiedFromOutside() {
        Inventory inventory = new Inventory();
        assertThrows(UnsupportedOperationException.class, () -> inventory.getItems().add(Items.rareSword()));
    }

    @Test
    void swordsHaveIncreasingDamage() {
        assertTrue(Items.ironSword().getDamageBonus() < Items.rareSword().getDamageBonus());
        assertTrue(Items.rareSword().getDamageBonus() < Items.mythSword().getDamageBonus());
    }
}
