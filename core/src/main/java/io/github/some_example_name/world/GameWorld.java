package io.github.some_example_name.world;

import com.badlogic.gdx.math.Rectangle;
import io.github.some_example_name.entity.Enemy;
import io.github.some_example_name.entity.Player;
import io.github.some_example_name.entity.PlayerInput;
import io.github.some_example_name.item.Item;
import io.github.some_example_name.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Состояние одного забега: уровень, игрок, враги и правила боя/лута.
 * Чистая логика — без графики, звука и ввода, поэтому её можно тестировать.
 */
public final class GameWorld {
    /** События мира, на которые может реагировать интерфейс (сообщения, звуки). */
    public interface Listener {
        default void onItemLooted(Item item) { }
        default void onInventoryFull(Item item) { }
    }

    private static final int MIN_ENEMIES_PER_ROOM = 2;
    private static final int MAX_ENEMIES_PER_ROOM = 4;
    private static final float RARE_SWORD_CHANCE = 0.05f;
    private static final float MYTH_SWORD_CHANCE = 0.01f;

    private final Level level;
    private final Player player = new Player();
    private final List<Enemy> enemies = new ArrayList<>();
    private final Random random;
    private Listener listener = new Listener() { };

    private final Rectangle attackArea = new Rectangle();
    private final Rectangle enemyBody = new Rectangle();

    public GameWorld(Random random) {
        this(new LevelGenerator(random).generate(), random);
    }

    public GameWorld(Level level, Random random) {
        this.level = level;
        this.random = random;
        Room start = level.getStartRoom();
        player.placeAtTile(start.getCenterX(), start.getCenterY());
        spawnEnemies();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private void spawnEnemies() {
        for (Room room : level.getRooms()) {
            if (room.getType() != RoomType.NORMAL) continue;

            List<int[]> freeTiles = new ArrayList<>();
            for (int tx = room.getX(); tx < room.getX() + room.getWidth(); tx++) {
                for (int ty = room.getY(); ty < room.getY() + room.getHeight(); ty++) {
                    if (level.isWalkable(tx, ty)) freeTiles.add(new int[]{tx, ty});
                }
            }
            Collections.shuffle(freeTiles, random);
            int count = MIN_ENEMIES_PER_ROOM + random.nextInt(MAX_ENEMIES_PER_ROOM - MIN_ENEMIES_PER_ROOM + 1);
            for (int i = 0; i < Math.min(count, freeTiles.size()); i++) {
                int[] tile = freeTiles.get(i);
                enemies.add(new Enemy(room, tile[0], tile[1]));
            }
        }
    }

    public void update(float delta, PlayerInput input) {
        if (player.isDead()) return;

        player.update(delta, input, level);
        if (input.attack() && player.tryAttack()) {
            player.getAttackArea(attackArea);
            for (Enemy enemy : enemies) {
                if (enemy.isAlive() && enemy.getBody(enemyBody).overlaps(attackArea)) {
                    enemy.takeDamage(player.getDamage());
                }
            }
        }

        Iterator<Enemy> it = enemies.iterator();
        while (it.hasNext()) {
            Enemy enemy = it.next();
            enemy.update(delta, player, level);
            if (enemy.isDead()) {
                it.remove();
                dropLoot();
            }
        }
    }

    private void dropLoot() {
        if (random.nextFloat() < RARE_SWORD_CHANCE) giveToPlayer(Items.rareSword());
        if (random.nextFloat() < MYTH_SWORD_CHANCE) giveToPlayer(Items.mythSword());
    }

    private void giveToPlayer(Item item) {
        if (player.getInventory().add(item)) {
            listener.onItemLooted(item);
        } else {
            listener.onInventoryFull(item);
        }
    }

    public Level getLevel() { return level; }
    public Player getPlayer() { return player; }
    public List<Enemy> getEnemies() { return Collections.unmodifiableList(enemies); }
}
