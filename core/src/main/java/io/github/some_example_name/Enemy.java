package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Enemy {
    private float x, y;
    private int[][] levelMap;
    private int health = 50;
    private boolean isDead = false;
    private Texture enemySheet;
    private Texture enemyAttackSheet;
    private Texture enemyHurtSheet;
    private TextureRegion[] walkFrames;
    private TextureRegion[] attackFrames;
    private TextureRegion[] damageFrames;
    private float animationTime;
    private int currentFrame;
    private boolean isAttacking;
    private boolean takingDamage;
    private float attackCooldown;
    private float attackAnimationTimer;
    private float damageAnimationTimer;
    private final float ATTACK_COOLDOWN_DURATION = 1.0f;
    private final float ATTACK_ANIMATION_DURATION = 0.4f;
    private final float DAMAGE_ANIMATION_DURATION = 0.3f;
    private int damage = 8;
    private Level level;

    public Enemy(int[][] levelMap, Level level) {
        this.levelMap = levelMap;
        this.level = level;
        try {
            enemySheet = new Texture("Slime1_Walk_full.png");
            enemyAttackSheet = new Texture("Slime1_Attack_full.png");
            enemyHurtSheet = new Texture("Slime1_Hurt_full.png");
            Gdx.app.log("Enemy", "Loaded enemy.png, size=" + enemySheet.getWidth() + "x" + enemySheet.getHeight());
        } catch (Exception e) {
            Gdx.app.error("Enemy", "Failed to load enemy.png", e);
            throw e;
        }
        animationTime = 0f;
        isAttacking = false;
        takingDamage = false;
        attackCooldown = 0f;
        attackAnimationTimer = 0f;
        damageAnimationTimer = 0f;

        walkFrames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            walkFrames[i] = new TextureRegion(enemySheet, i * 64, 0, 64, 64);
            Gdx.app.log("Enemy", "Loaded walk frame " + i + ": x=" + (i * 64) + ", y=0");
        }

        attackFrames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            attackFrames[i] = new TextureRegion(enemyAttackSheet, i * 64, 0, 64, 64);
            Gdx.app.log("Enemy", "Loaded attack frame " + i + ": x=" + (i * 64) + ", y=64");
        }

        damageFrames = new TextureRegion[4];
        for (int i = 0; i < 4; i++) {
            damageFrames[i] = new TextureRegion(enemyHurtSheet, i * 64, 0, 64, 64);
            Gdx.app.log("Enemy", "Loaded damage frame " + i + ": x=" + (i * 64) + ", y=128");
        }

        spawnInNormalRoom();
    }

    private void spawnInNormalRoom() {
        Random random = new Random();
        List<Room> normalRooms = new ArrayList<>();
        Room startRoom = null;

        if (!level.rooms.isEmpty()) {
            startRoom = level.rooms.get(0);
            Gdx.app.log("Enemy", "Start room identified at (" + startRoom.x + "," + startRoom.y + "), type=" + startRoom.type);
        } else {
            Gdx.app.log("Enemy", "No rooms found in level.rooms!");
        }

        for (Room room : level.rooms) {
            Gdx.app.log("Enemy", "Room at (" + room.x + "," + room.y + "), type=" + room.type + ", neighbors=" + room.neighbors.size());
        }

        for (Room room : level.rooms) {
            if (room.type == RoomType.NORMAL && room != startRoom) {
                normalRooms.add(room);
                Gdx.app.log("Enemy", "Added normal room at (" + room.x + "," + room.y + "), type=" + room.type);
            }
        }

        if (normalRooms.isEmpty()) {
            Gdx.app.log("Enemy", "No normal rooms found, defaulting to any non-start room");
            for (Room room : level.rooms) {
                if (room != startRoom) {
                    normalRooms.add(room);
                    Gdx.app.log("Enemy", "Fallback: Added room at (" + room.x + "," + room.y + "), type=" + room.type);
                }
            }
        }

        if (!normalRooms.isEmpty()) {
            Room spawnRoom = normalRooms.get(random.nextInt(normalRooms.size()));
            boolean spawned = false;
            for (int i = spawnRoom.x + 1; i < spawnRoom.x + spawnRoom.width - 1; i++) {
                for (int j = spawnRoom.y + 1; j < spawnRoom.y + spawnRoom.height - 1; j++) {
                    if (levelMap[i][j] == 0 && random.nextFloat() < 0.1f) {
                        x = i * Level.TILE_SIZE;
                        y = j * Level.TILE_SIZE;
                        Gdx.app.log("Enemy", "Spawned in normal room at (" + x + ", " + y + "), room type=" + spawnRoom.type);
                        spawned = true;
                        break;
                    }
                }
                if (spawned) break;
            }
            if (!spawned) {
                Gdx.app.log("Enemy", "No spawn point found in normal room at (" + spawnRoom.x + "," + spawnRoom.y + "), defaulting to (0, 0)");
                x = 0;
                y = 0;
            }
        } else {
            Gdx.app.log("Enemy", "No valid rooms for spawn, defaulting to (0, 0)");
            x = 0;
            y = 0;
        }
    }

    public void update(float playerX, float playerY, Player player, float delta) {
        if (isDead) {
            return;
        }

        animationTime += delta;
        if (attackCooldown > 0) {
            attackCooldown -= delta;
        }
        if (attackAnimationTimer > 0) {
            attackAnimationTimer -= delta;
            if (attackAnimationTimer <= 0) {
                isAttacking = false;
                animationTime = 0f;
                Gdx.app.log("Enemy", "Attack animation ended");
            }
        }
        if (damageAnimationTimer > 0) {
            damageAnimationTimer -= delta;
            if (damageAnimationTimer <= 0) {
                takingDamage = false;
                animationTime = 0f;
                Gdx.app.log("Enemy", "Damage animation ended");
            }
        }

        if (player == null) {
            Gdx.app.error("Enemy", "Player is null, cannot process attack");
            return;
        }

        if (checkCollision(playerX, playerY)) {
            if (attackCooldown <= 0) {
                isAttacking = true;
                attackAnimationTimer = ATTACK_ANIMATION_DURATION;
                int playerDefense = player.getDefense();
                int rawDamage = damage - playerDefense;
                int damageDealt = Math.max(1, rawDamage);
                int previousHealth = player.getHealth();
                player.setHealth(previousHealth - damageDealt);
                Gdx.app.log("Enemy", "Attack: Enemy Damage=" + damage + ", Player Defense=" + playerDefense +
                        ", Raw Damage=" + rawDamage + ", Dealt=" + damageDealt +
                        ", Player HP (before)=" + previousHealth + ", HP (after)=" + player.getHealth());
                attackCooldown = ATTACK_COOLDOWN_DURATION;
            }
        }

        updateAnimation();
    }

    public boolean checkCollision(float playerX, float playerY) {
        float enemyCenterX = x + 32;
        float enemyCenterY = y + 32;
        float playerCenterX = playerX + 32;
        float playerCenterY = playerY + 32;
        float distance = (float)Math.sqrt(Math.pow(playerCenterX - enemyCenterX, 2) + Math.pow(playerCenterY - enemyCenterY, 2));
        boolean collision = distance < 64;
        Gdx.app.log("Enemy", "Collision check: " +
                "EnemyCenter=(" + enemyCenterX + "," + enemyCenterY + "), " +
                "PlayerCenter=(" + playerCenterX + "," + playerCenterY + "), " +
                "Distance=" + distance + ", Collision=" + collision);
        return collision;
    }

    private void updateAnimation() {
        TextureRegion[] currentAnimation;
        if (takingDamage) {
            currentAnimation = damageFrames;
            Gdx.app.log("Enemy", "Playing damage animation");
        } else if (isAttacking) {
            currentAnimation = attackFrames;
            Gdx.app.log("Enemy", "Playing attack animation");
        } else {
            currentAnimation = walkFrames;
            Gdx.app.log("Enemy", "Playing walk animation");
        }
        currentFrame = (int)(animationTime * 10) % currentAnimation.length;
        Gdx.app.log("Enemy", "Animation: isAttacking=" + isAttacking + ", takingDamage=" + takingDamage + ", Frame=" + currentFrame);
    }

    public List<Item> dropItems() {
        List<Item> droppedItems = new ArrayList<>();
        Random random = new Random();
        if (random.nextFloat() < 0.1f) {
            droppedItems.add(new Item("Редкий меч", "Прочный и острый", Item.ItemType.WEAPON, 10, 0, 0, "rare_sword.png"));
        }
        if (random.nextFloat() < 0.05f) {
            droppedItems.add(new Item("Мифический меч", "Пылающий клинок", Item.ItemType.WEAPON, 15, 1.5f, 10, "myth_sword.png"));
        }
        return droppedItems;
    }

    public void render(SpriteBatch batch) {
        if (!isDead) {
            TextureRegion currentFrame = takingDamage ? damageFrames[this.currentFrame] :
                    isAttacking ? attackFrames[this.currentFrame] :
                            walkFrames[this.currentFrame];
            batch.draw(currentFrame, x, y, 96, 96);
            Gdx.app.log("Enemy", "Rendering frame: x=" + x + ", y=" + y + ", frame=" + this.currentFrame);
        }
    }

    public void dispose() {
        enemySheet.dispose();
    }

    public boolean isDead() {
        return isDead;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getHealth() {
        return health;
    }

    public void takeDamage(int damage) {
        health -= damage;
        takingDamage = true;
        damageAnimationTimer = DAMAGE_ANIMATION_DURATION;
        animationTime = 0f;
        Gdx.app.log("Enemy", "Taking damage: " + damage + ", HP=" + health);
        if (health <= 0) {
            isDead = true;
            Gdx.app.log("Enemy", "Enemy died!");
        }
    }
}
