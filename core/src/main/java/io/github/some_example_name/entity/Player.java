package io.github.some_example_name.entity;

import com.badlogic.gdx.math.Rectangle;
import io.github.some_example_name.item.Inventory;
import io.github.some_example_name.item.Item;
import io.github.some_example_name.item.Items;
import io.github.some_example_name.world.Level;

/**
 * Игрок: позиция, здоровье, экипировка и состояние анимации.
 * Не знает ни о текстурах, ни о клавиатуре — их обрабатывают рендерер и экран.
 * Позиция (x, y) — левый нижний угол хитбокса ног.
 */
public final class Player {
    public static final float WIDTH = 24f;
    public static final float HEIGHT = 16f;
    /** Примерная высота тела — для попаданий и центра персонажа. */
    public static final float BODY_HEIGHT = 48f;
    public static final int MAX_HEALTH = 100;
    public static final float ATTACK_DURATION = 0.48f;

    private static final int BASE_DAMAGE = 10;
    private static final float BASE_SPEED = 200f;
    private static final float ATTACK_REACH = 52f;

    private float x, y;
    private Direction direction = Direction.DOWN;
    private boolean moving;
    private float stateTime;
    private float attackTimer;

    private int health = MAX_HEALTH;
    private final Inventory inventory = new Inventory();
    private Item weapon;
    private Item armor;
    private float potionSpeedMultiplier = 1f;
    private float potionSpeedTimer;

    public Player() {
        weapon = Items.ironSword();
        inventory.add(Items.healingPotion());
    }

    public void placeAtTile(int tileX, int tileY) {
        x = tileX * Level.TILE_SIZE + (Level.TILE_SIZE - WIDTH) / 2f;
        y = tileY * Level.TILE_SIZE + (Level.TILE_SIZE - HEIGHT) / 2f;
    }

    public void update(float delta, PlayerInput input, Level level) {
        stateTime += delta;
        if (attackTimer > 0) {
            attackTimer -= delta;
            if (attackTimer <= 0) stateTime = 0;
        }
        if (potionSpeedTimer > 0) {
            potionSpeedTimer -= delta;
            if (potionSpeedTimer <= 0) potionSpeedMultiplier = 1f;
        }

        float mx = input.moveX();
        float my = input.moveY();
        boolean nowMoving = mx != 0 || my != 0;
        if (nowMoving != moving && !isAttacking()) stateTime = 0;
        moving = nowMoving;
        if (!moving) return;

        float length = (float) Math.sqrt(mx * mx + my * my); // по диагонали не быстрее, чем по прямой
        mx /= length;
        my /= length;
        if (!isAttacking()) direction = Direction.fromVector(mx, my);

        float step = getSpeed() * delta;
        Movement.Occupancy occupancy = (px, py) -> level.isAreaWalkable(px, py, WIDTH, HEIGHT);
        x = Movement.moveX(x, y, mx * step, occupancy);
        y = Movement.moveY(x, y, my * step, occupancy);
    }

    /** Начинает удар, если предыдущий уже закончился. */
    public boolean tryAttack() {
        if (isAttacking() || isDead()) return false;
        attackTimer = ATTACK_DURATION;
        stateTime = 0;
        return true;
    }

    /** Область удара мечом перед персонажем. */
    public Rectangle getAttackArea(Rectangle out) {
        float cx = getCenterX();
        return switch (direction) {
            case RIGHT -> out.set(x + WIDTH, y - 8, ATTACK_REACH, BODY_HEIGHT + 8);
            case LEFT -> out.set(x - ATTACK_REACH, y - 8, ATTACK_REACH, BODY_HEIGHT + 8);
            case UP -> out.set(cx - 32, y + HEIGHT, 64, ATTACK_REACH + 16);
            case DOWN -> out.set(cx - 32, y - ATTACK_REACH, 64, ATTACK_REACH + 8);
        };
    }

    public void takeDamage(int rawDamage) {
        int dealt = Math.max(1, rawDamage - getDefense());
        health = Math.max(0, health - dealt);
    }

    public void heal(int amount) {
        health = Math.min(MAX_HEALTH, health + amount);
    }

    /**
     * Применяет предмет из рюкзака: зелье выпивается, оружие/броня экипируется
     * (предыдущий предмет возвращается в рюкзак).
     *
     * @return {@code false}, если предмет применить нельзя (например, здоровье уже полное)
     */
    public boolean useItem(Item item) {
        if (!inventory.contains(item)) return false;
        switch (item.getType()) {
            case POTION -> {
                boolean heals = item.getHealthRestore() > 0 && health < MAX_HEALTH;
                boolean speeds = item.getSpeedMultiplier() != 1f && item.getEffectDuration() > 0;
                if (!heals && !speeds) return false;
                heal(item.getHealthRestore());
                if (speeds) {
                    potionSpeedMultiplier = item.getSpeedMultiplier();
                    potionSpeedTimer = item.getEffectDuration();
                }
                inventory.remove(item);
            }
            case WEAPON -> {
                inventory.remove(item);
                if (weapon != null) inventory.add(weapon);
                weapon = item;
            }
            case ARMOR -> {
                inventory.remove(item);
                if (armor != null) inventory.add(armor);
                armor = item;
            }
        }
        return true;
    }

    /** Снимает экипированный предмет в рюкзак. */
    public boolean unequip(Item item) {
        if (item == null || inventory.isFull()) return false;
        if (item == weapon) {
            weapon = null;
        } else if (item == armor) {
            armor = null;
        } else {
            return false;
        }
        inventory.add(item);
        return true;
    }

    public int getDamage() {
        return BASE_DAMAGE + (weapon != null ? weapon.getDamageBonus() : 0);
    }

    public int getDefense() {
        return armor != null ? armor.getDefenseBonus() : 0;
    }

    public float getSpeed() {
        float weaponMultiplier = weapon != null ? weapon.getSpeedMultiplier() : 1f;
        return BASE_SPEED * weaponMultiplier * potionSpeedMultiplier;
    }

    public boolean isAttacking() { return attackTimer > 0; }
    public boolean isMoving() { return moving; }
    public boolean isDead() { return health <= 0; }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getCenterX() { return x + WIDTH / 2f; }
    public float getBodyCenterY() { return y + BODY_HEIGHT / 3f; }
    public Direction getDirection() { return direction; }
    public float getStateTime() { return stateTime; }
    public int getHealth() { return health; }
    public Inventory getInventory() { return inventory; }
    public Item getWeapon() { return weapon; }
    public Item getArmor() { return armor; }
}
