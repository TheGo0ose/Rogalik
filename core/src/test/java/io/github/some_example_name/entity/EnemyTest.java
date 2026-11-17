package io.github.some_example_name.entity;

import io.github.some_example_name.world.GameWorld;
import io.github.some_example_name.world.Level;
import io.github.some_example_name.world.Room;
import io.github.some_example_name.world.RoomType;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnemyTest {

    @Test
    void playsDeathAnimationBeforeDisappearing() {
        GameWorld world = new GameWorld(new Random(1));
        Room room = world.getLevel().getStartRoom();
        Enemy enemy = new Enemy(room, room.getX(), room.getY());

        enemy.takeDamage(15);
        assertEquals(Enemy.State.HURT, enemy.getState());
        enemy.takeDamage(15);
        assertEquals(Enemy.State.DYING, enemy.getState());
        assertFalse(enemy.isAlive());
        assertFalse(enemy.isDead(), "сразу после удара должна играть анимация смерти");

        for (int i = 0; i < 60; i++) enemy.update(1 / 60f, world.getPlayer(), world.getLevel());
        assertTrue(enemy.isDead());
    }

    @Test
    void enemiesSpawnOnlyInNormalRooms() {
        for (long seed = 0; seed < 50; seed++) {
            GameWorld world = new GameWorld(new Random(seed));
            for (Enemy enemy : world.getEnemies()) {
                assertEquals(RoomType.NORMAL, enemy.getRoom().getType());
            }
        }
    }

    @Test
    void enemiesAttackPlayerButNeverLeaveTheirRoom() {
        for (long seed = 0; seed < 30; seed++) {
            GameWorld world = new GameWorld(new Random(seed));
            Level level = world.getLevel();
            Room target = level.getRooms().stream()
                .filter(r -> r.getType() == RoomType.NORMAL).findFirst().orElseThrow();
            Player player = world.getPlayer();
            player.placeAtTile(target.getCenterX(), target.getCenterY());

            for (int i = 0; i < 60 * 10; i++) {
                world.update(1 / 60f, PlayerInput.NONE);
                for (Enemy enemy : world.getEnemies()) {
                    Room room = enemy.getRoom();
                    assertTrue(room.containsTile(Level.toTile(enemy.getX()), Level.toTile(enemy.getY())));
                    assertTrue(room.containsTile(Level.toTile(enemy.getX() + Enemy.WIDTH - 0.01f),
                        Level.toTile(enemy.getY() + Enemy.HEIGHT - 0.01f)));
                }
            }
            assertTrue(player.getHealth() < Player.MAX_HEALTH, "seed " + seed + ": слизни должны атаковать");
        }
    }
}
