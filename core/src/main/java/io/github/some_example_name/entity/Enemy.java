package io.github.some_example_name.entity;

import com.badlogic.gdx.math.Rectangle;
import io.github.some_example_name.world.Level;
import io.github.some_example_name.world.Room;

/**
 * Слизень. Живёт в своей комнате: преследует игрока, только пока тот в этой комнате,
 * и никогда её не покидает. Позиция (x, y) — левый нижний угол хитбокса.
 */
public final class Enemy {
    public enum State { IDLE, CHASE, ATTACK, HURT, DYING, DEAD }

    public static final float WIDTH = 30f;
    public static final float HEIGHT = 16f;
    public static final float BODY_HEIGHT = 30f;
    public static final int MAX_HEALTH = 20;

    public static final float ATTACK_DURATION = 0.6f;
    public static final float HURT_DURATION = 0.3f;
    public static final float DEATH_DURATION = 0.8f;

    private static final float SPEED = 50f;
    private static final int DAMAGE = 1;
    private static final float ATTACK_RANGE = 40f;
    private static final float ATTACK_COOLDOWN = 1.0f;

    private final Room room;
    private float x, y;
    private Direction direction = Direction.DOWN;
    private State state = State.IDLE;
    private float stateTime;
    private float attackCooldown;
    private int health = MAX_HEALTH;

    public Enemy(Room room, int tileX, int tileY) {
        this.room = room;
        this.x = tileX * Level.TILE_SIZE + (Level.TILE_SIZE - WIDTH) / 2f;
        this.y = tileY * Level.TILE_SIZE + (Level.TILE_SIZE - HEIGHT) / 2f;
    }

    public void update(float delta, Player player, Level level) {
        stateTime += delta;
        if (attackCooldown > 0) attackCooldown -= delta;

        switch (state) {
            case DEAD -> { }
            case DYING -> {
                if (stateTime >= DEATH_DURATION) setState(State.DEAD);
            }
            case HURT -> {
                if (stateTime >= HURT_DURATION) setState(State.IDLE);
            }
            case ATTACK -> {
                if (stateTime >= ATTACK_DURATION) setState(State.IDLE);
            }
            case IDLE, CHASE -> think(delta, player, level);
        }
    }

    private void think(float delta, Player player, Level level) {
        if (player.isDead() || !isPlayerInMyRoom(player)) {
            setWalkingState(State.IDLE);
            return;
        }

        float dx = player.getCenterX() - getCenterX();
        float dy = player.getBodyCenterY() - getCenterY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        direction = Direction.fromVector(dx, dy);

        if (distance <= ATTACK_RANGE) {
            if (attackCooldown <= 0) {
                setState(State.ATTACK);
                attackCooldown = ATTACK_COOLDOWN;
                player.takeDamage(DAMAGE);
            } else {
                setWalkingState(State.IDLE);
            }
            return;
        }

        setWalkingState(State.CHASE);
        float step = SPEED * delta;
        Movement.Occupancy occupancy = (px, py) -> canOccupy(px, py, level);
        x = Movement.moveX(x, y, dx / distance * step, occupancy);
        y = Movement.moveY(x, y, dy / distance * step, occupancy);
    }

    private boolean isPlayerInMyRoom(Player player) {
        int tx = Level.toTile(player.getCenterX());
        int ty = Level.toTile(player.getY() + Player.HEIGHT / 2f);
        return room.containsTile(tx, ty);
    }

    /** Хитбокс целиком на полу и внутри своей комнаты. */
    private boolean canOccupy(float px, float py, Level level) {
        return level.isAreaWalkable(px, py, WIDTH, HEIGHT)
            && room.containsTile(Level.toTile(px), Level.toTile(py))
            && room.containsTile(Level.toTile(px + WIDTH - 0.001f), Level.toTile(py + HEIGHT - 0.001f));
    }

    public void takeDamage(int amount) {
        if (!isAlive()) return;
        health = Math.max(0, health - amount);
        setState(health == 0 ? State.DYING : State.HURT);
    }

    private void setState(State newState) {
        state = newState;
        stateTime = 0;
    }

    /** IDLE и CHASE используют одну анимацию — не сбрасываем её при переключении между ними. */
    private void setWalkingState(State newState) {
        state = newState;
    }

    public Rectangle getBody(Rectangle out) {
        return out.set(x, y, WIDTH, BODY_HEIGHT);
    }

    /** Жив и может участвовать в бою (не умирает и не умер). */
    public boolean isAlive() { return state != State.DYING && state != State.DEAD; }
    /** Анимация смерти закончилась — можно убирать из мира. */
    public boolean isDead() { return state == State.DEAD; }
    public State getState() { return state; }
    public float getStateTime() { return stateTime; }
    public Direction getDirection() { return direction; }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getCenterX() { return x + WIDTH / 2f; }
    public float getCenterY() { return y + HEIGHT / 2f; }
    public int getHealth() { return health; }
    public Room getRoom() { return room; }
}
