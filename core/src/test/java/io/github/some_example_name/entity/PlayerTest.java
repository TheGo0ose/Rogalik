package io.github.some_example_name.entity;

import io.github.some_example_name.item.Inventory;
import io.github.some_example_name.item.Item;
import io.github.some_example_name.item.ItemType;
import io.github.some_example_name.item.Items;
import io.github.some_example_name.world.GameWorld;
import io.github.some_example_name.world.Level;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerTest {

    @Test
    void startingSwordAddsDamage() {
        Player player = new Player();
        assertEquals("Железный меч", player.getWeapon().getName());
        assertEquals(15, player.getDamage()); // 10 базовый + 5 от меча
    }

    @Test
    void startsWithHealingPotionInBackpack() {
        Player player = new Player();
        assertEquals(1, player.getInventory().size());
        assertEquals(ItemType.POTION, player.getInventory().getItems().get(0).getType());
    }

    @Test
    void equippingWeaponSwapsWithBackpack() {
        Player player = new Player();
        Item oldSword = player.getWeapon();
        Item rare = Items.rareSword();
        player.getInventory().add(rare);

        assertTrue(player.useItem(rare));

        assertSame(rare, player.getWeapon());
        assertEquals(20, player.getDamage());
        assertFalse(player.getInventory().contains(rare));
        assertTrue(player.getInventory().contains(oldSword));
    }

    @Test
    void mythSwordSpeedsUpWhileEquipped() {
        Player player = new Player();
        float baseSpeed = player.getSpeed();
        Item myth = Items.mythSword();
        player.getInventory().add(myth);
        player.useItem(myth);
        assertEquals(baseSpeed * 1.5f, player.getSpeed(), 0.001f);

        player.unequip(myth);
        assertEquals(baseSpeed, player.getSpeed(), 0.001f);
    }

    @Test
    void potionIsNotWastedAtFullHealth() {
        Player player = new Player();
        Item potion = player.getInventory().getItems().get(0);

        assertFalse(player.useItem(potion));
        assertTrue(player.getInventory().contains(potion));

        player.takeDamage(30);
        assertTrue(player.useItem(potion));
        assertEquals(Player.MAX_HEALTH - 10, player.getHealth());
        assertFalse(player.getInventory().contains(potion));
    }

    @Test
    void healingNeverExceedsMaximum() {
        Player player = new Player();
        player.takeDamage(5);
        player.heal(100);
        assertEquals(Player.MAX_HEALTH, player.getHealth());
    }

    @Test
    void unequipFailsWhenBackpackIsFull() {
        Player player = new Player();
        while (!player.getInventory().isFull()) player.getInventory().add(Items.healingPotion());
        assertFalse(player.unequip(player.getWeapon()));
        assertEquals(Inventory.CAPACITY, player.getInventory().size());
    }

    @Test
    void cannotWalkThroughWalls() {
        GameWorld world = new GameWorld(new Random(42));
        Player player = world.getPlayer();
        Level level = world.getLevel();
        PlayerInput[] directions = {
            new PlayerInput(-1, 0, false), new PlayerInput(1, 0, false),
            new PlayerInput(0, 1, false), new PlayerInput(0, -1, false),
            new PlayerInput(1, 1, false),
        };
        for (PlayerInput input : directions) {
            for (int i = 0; i < 600; i++) {
                player.update(1 / 60f, input, level);
                assertTrue(level.isAreaWalkable(player.getX(), player.getY(), Player.WIDTH, Player.HEIGHT));
            }
        }
    }

    @Test
    void cannotStartNewAttackUntilPreviousFinishes() {
        Player player = new Player();
        assertTrue(player.tryAttack());
        assertFalse(player.tryAttack());
    }
}
