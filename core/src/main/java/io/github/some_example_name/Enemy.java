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
    private int health = 20;
    private boolean isDead = false;
    private Texture enemyWalkSheet;
    private Texture enemyAttackSheet;
    private Texture enemyHurtSheet;
    private Texture enemyDeathSheet;
    private TextureRegion[] walkDown, walkLeft, walkRight, walkUp;
    private TextureRegion[] attackDown, attackLeft, attackRight, attackUp;
    private TextureRegion[] damageDown, damageLeft, damageRight, damageUp;
    private TextureRegion[] deathDown, deathLeft, deathRight, deathUp;
    private float animationTime;
    private int currentFrame;
    private boolean isAttacking;
    private boolean takingDamage;
    private float attackCooldown;
    private float attackAnimationTimer;
    private float damageAnimationTimer;
    private float deathAnimationTimer;
    private final float ATTACK_COOLDOWN_DURATION = 1.0f;
    private final float ATTACK_ANIMATION_DURATION = 0.4f;
    private final float DAMAGE_ANIMATION_DURATION = 0.3f;
    private final float DEATH_ANIMATION_DURATION = 0.5f;
    private int damage = 1;
    private Level level;
    private Room assignedRoom;
    private float speed = 50f;
    private Direction currentDirection = Direction.DOWN;

    public enum Direction {
        LEFT, RIGHT, UP, DOWN
    }

    public Enemy(int[][] levelMap, Level level, Room assignedRoom) {
        this.levelMap = levelMap;
        this.level = level;
        this.assignedRoom = assignedRoom;
        try {
            enemyWalkSheet = new Texture("Slime1_Walk_full.png");
            enemyAttackSheet = new Texture("Slime1_Attack_full.png");
            enemyHurtSheet = new Texture("Slime1_Hurt_full.png");
            enemyDeathSheet = new Texture("Slime1_Death_full.png");
            Gdx.app.log("Enemy", "Loaded Slime1_Walk_full.png, size=" + enemyWalkSheet.getWidth() + "x" + enemyWalkSheet.getHeight());
            Gdx.app.log("Enemy", "Loaded Slime1_Attack_full.png, size=" + enemyAttackSheet.getWidth() + "x" + enemyAttackSheet.getHeight());
            Gdx.app.log("Enemy", "Loaded Slime1_Hurt_full.png, size=" + enemyHurtSheet.getWidth() + "x" + enemyHurtSheet.getHeight());
            Gdx.app.log("Enemy", "Loaded Slime1_Death_full.png, size=" + enemyDeathSheet.getWidth() + "x" + enemyDeathSheet.getHeight());
        } catch (Exception e) {
            Gdx.app.error("Enemy", "Failed to load enemy sheets", e);
            throw e;
        }
        animationTime = 0f;
        isAttacking = false;
        takingDamage = false;
        attackCooldown = 0f;
        attackAnimationTimer = 0f;
        damageAnimationTimer = 0f;
        deathAnimationTimer = 0f;

        walkDown = new TextureRegion[4];
        walkLeft = new TextureRegion[4];
        walkRight = new TextureRegion[4];
        walkUp = new TextureRegion[4];
        loadWalkAnimations();

        attackDown = new TextureRegion[4];
        attackLeft = new TextureRegion[4];
        attackRight = new TextureRegion[4];
        attackUp = new TextureRegion[4];
        loadAttackAnimations();

        damageDown = new TextureRegion[4];
        damageLeft = new TextureRegion[4];
        damageRight = new TextureRegion[4];
        damageUp = new TextureRegion[4];
        loadDamageAnimations();

        deathDown = new TextureRegion[4];
        deathLeft = new TextureRegion[4];
        deathRight = new TextureRegion[4];
        deathUp = new TextureRegion[4];
        loadDeathAnimations();

        spawnInRoom();
    }

    private void loadWalkAnimations() {
        for (int i = 0; i < 4; i++) {
            walkDown[i] = new TextureRegion(enemyWalkSheet, i * 64, 0, 64, 64);
            walkLeft[i] = new TextureRegion(enemyWalkSheet, i * 64, 64, 64, 64);
            walkRight[i] = new TextureRegion(enemyWalkSheet, i * 64, 128, 64, 64);
            walkUp[i] = new TextureRegion(enemyWalkSheet, i * 64, 192, 64, 64);
            Gdx.app.log("Enemy", "Loaded walk frame " + i + ": Down=(" + (i * 64) + ",0), Left=(" + (i * 64) + ",64), Right=(" + (i * 64) + ",128), Up=(" + (i * 64) + ",192)");
        }
    }

    private void loadAttackAnimations() {
        for (int i = 0; i < 4; i++) {
            attackDown[i] = new TextureRegion(enemyAttackSheet, i * 64, 0, 64, 64);
            attackLeft[i] = new TextureRegion(enemyAttackSheet, i * 64, 64, 64, 64);
            attackRight[i] = new TextureRegion(enemyAttackSheet, i * 64, 128, 64, 64);
            attackUp[i] = new TextureRegion(enemyAttackSheet, i * 64, 192, 64, 64);
            Gdx.app.log("Enemy", "Loaded attack frame " + i + ": Down=(" + (i * 64) + ",0), Left=(" + (i * 64) + ",64), Right=(" + (i * 64) + ",128), Up=(" + (i * 64) + ",192)");
        }
    }

    private void loadDamageAnimations() {
        for (int i = 0; i < 4; i++) {
            damageDown[i] = new TextureRegion(enemyHurtSheet, i * 64, 0, 64, 64);
            damageLeft[i] = new TextureRegion(enemyHurtSheet, i * 64, 64, 64, 64);
            damageRight[i] = new TextureRegion(enemyHurtSheet, i * 64, 128, 64, 64);
            damageUp[i] = new TextureRegion(enemyHurtSheet, i * 64, 192, 64, 64);
            Gdx.app.log("Enemy", "Loaded damage frame " + i + ": Down=(" + (i * 64) + ",0), Left=(" + (i * 64) + ",64), Right=(" + (i * 64) + ",128), Up=(" + (i * 64) + ",192)");
        }
    }

    private void loadDeathAnimations() {
        for (int i = 0; i < 4; i++) {
            deathDown[i] = new TextureRegion(enemyDeathSheet, i * 64, 0, 64, 64);
            deathLeft[i] = new TextureRegion(enemyDeathSheet, i * 64, 64, 64, 64);
            deathRight[i] = new TextureRegion(enemyDeathSheet, i * 64, 128, 64, 64);
            deathUp[i] = new TextureRegion(enemyDeathSheet, i * 64, 192, 64, 64);
            Gdx.app.log("Enemy", "Loaded death frame " + i + ": Down=(" + (i * 64) + ",0), Left=(" + (i * 64) + ",64), Right=(" + (i * 64) + ",128), Up=(" + (i * 64) + ",192)");
        }
    }

    private void spawnInRoom() {
        Random random = new Random();
        boolean spawned = false;
        List<int[]> freeTiles = new ArrayList<>();
        for (int i = assignedRoom.x + 1; i < assignedRoom.x + assignedRoom.width - 1; i++) {
            for (int j = assignedRoom.y + 1; j < assignedRoom.y + assignedRoom.height - 1; j++) {
                if (levelMap[i][j] == 0) {
                    freeTiles.add(new int[]{i, j});
                }
            }
        }
        if (!freeTiles.isEmpty()) {
            int[] tile = freeTiles.get(random.nextInt(freeTiles.size()));
            x = tile[0] * Level.TILE_SIZE;
            y = tile[1] * Level.TILE_SIZE;
            spawned = true;
            Gdx.app.log("Enemy", "Spawned in room at (" + x + ", " + y + "), room type=" + assignedRoom.type + ", room coords (" + assignedRoom.x + "," + assignedRoom.y + ")");
        }
        if (!spawned) {
            Gdx.app.log("Enemy", "No spawn point found in room at (" + assignedRoom.x + "," + assignedRoom.y + "), defaulting to (0, 0)");
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
        if (deathAnimationTimer > 0) {
            deathAnimationTimer -= delta;
            if (deathAnimationTimer <= 0) {
                isDead = true;
                Gdx.app.log("Enemy", "Death animation ended");
            }
        }

        if (player == null) {
            Gdx.app.error("Enemy", "Player is null, cannot process attack");
            return;
        }

        if (isPlayerInRoom(playerX, playerY) && !isAttacking) {
            moveTowardsPlayer(playerX, playerY, delta);
            updateDirection(playerX, playerY);
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

    private boolean isPlayerInRoom(float playerX, float playerY) {
        if (assignedRoom == null) return false;
        int playerTileX = (int)(playerX / Level.TILE_SIZE);
        int playerTileY = (int)(playerY / Level.TILE_SIZE);
        boolean inRoom = playerTileX >= assignedRoom.x && playerTileX < assignedRoom.x + assignedRoom.width &&
            playerTileY >= assignedRoom.y && playerTileY < assignedRoom.y + assignedRoom.height;
        Gdx.app.log("Enemy", "Player in room check: Player at (" + playerTileX + "," + playerTileY + "), Room at (" +
            assignedRoom.x + "," + assignedRoom.y + ") to (" + (assignedRoom.x + assignedRoom.width - 1) + "," +
            (assignedRoom.y + assignedRoom.height - 1) + "), In room=" + inRoom);
        return inRoom;
    }

    private void moveTowardsPlayer(float playerX, float playerY, float delta) {
        float dx = playerX - x;
        float dy = playerY - y;
        float distance = (float)Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            float moveX = (dx / distance) * speed * delta;
            float moveY = (dy / distance) * speed * delta;

            float newX = x + moveX;
            float newY = y + moveY;

            int tileX = (int)(newX / Level.TILE_SIZE);
            int tileY = (int)(newY / Level.TILE_SIZE);
            if (tileX >= assignedRoom.x + 1 && tileX < assignedRoom.x + assignedRoom.width - 1 &&
                tileY >= assignedRoom.y + 1 && tileY < assignedRoom.y + assignedRoom.height - 1 &&
                levelMap[tileX][tileY] == 0) {
                x = newX;
                y = newY;
            } else {
                x = Math.max(assignedRoom.x + 1, Math.min(assignedRoom.x + assignedRoom.width - 2, tileX)) * Level.TILE_SIZE;
                y = Math.max(assignedRoom.y + 1, Math.min(assignedRoom.y + assignedRoom.height - 2, tileY)) * Level.TILE_SIZE;
                Gdx.app.log("Enemy", "Enemy hit room boundary, adjusted to (" + x + "," + y + ")");
            }

            Gdx.app.log("Enemy", "Moving towards player: from (" + x + "," + y + ") to player at (" + playerX + "," + playerY + ")");
        }
    }

    private void updateDirection(float playerX, float playerY) {
        float dx = playerX - x;
        float dy = playerY - y;
        if (Math.abs(dx) > Math.abs(dy)) {
            currentDirection = dx < 0 ? Direction.LEFT : Direction.RIGHT;
        } else {
            currentDirection = dy < 0 ? Direction.DOWN : Direction.UP;
        }
        Gdx.app.log("Enemy", "Updated direction to " + currentDirection);
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
        if (isDead && deathAnimationTimer > 0) {
            currentAnimation = switch (currentDirection) {
                case LEFT -> deathLeft;
                case RIGHT -> deathRight;
                case UP -> deathUp;
                case DOWN -> deathDown;
            };
        } else if (takingDamage) {
            currentAnimation = switch (currentDirection) {
                case LEFT -> damageLeft;
                case RIGHT -> damageRight;
                case UP -> damageUp;
                case DOWN -> damageDown;
            };
        } else if (isAttacking) {
            currentAnimation = switch (currentDirection) {
                case LEFT -> attackLeft;
                case RIGHT -> attackRight;
                case UP -> attackUp;
                case DOWN -> attackDown;
            };
        } else {
            currentAnimation = switch (currentDirection) {
                case LEFT -> walkLeft;
                case RIGHT -> walkRight;
                case UP -> walkUp;
                case DOWN -> walkDown;
            };
        }
        currentFrame = (int)(animationTime * 10) % currentAnimation.length;
        Gdx.app.log("Enemy", "Animation: isAttacking=" + isAttacking + ", takingDamage=" + takingDamage + ", isDead=" + isDead + ", Frame=" + currentFrame + ", Direction=" + currentDirection);
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
        if (!isDead || (isDead && deathAnimationTimer > 0)) {
            TextureRegion currentFrame = takingDamage ? switch (currentDirection) {
                case LEFT -> damageLeft[this.currentFrame];
                case RIGHT -> damageRight[this.currentFrame];
                case UP -> damageUp[this.currentFrame];
                case DOWN -> damageDown[this.currentFrame];
            } : isAttacking ? switch (currentDirection) {
                case LEFT -> attackLeft[this.currentFrame];
                case RIGHT -> attackRight[this.currentFrame];
                case UP -> attackUp[this.currentFrame];
                case DOWN -> attackDown[this.currentFrame];
            } : isDead ? switch (currentDirection) {
                case LEFT -> deathLeft[this.currentFrame];
                case RIGHT -> deathRight[this.currentFrame];
                case UP -> deathUp[this.currentFrame];
                case DOWN -> deathDown[this.currentFrame];
            } : switch (currentDirection) {
                case LEFT -> walkLeft[this.currentFrame];
                case RIGHT -> walkRight[this.currentFrame];
                case UP -> walkUp[this.currentFrame];
                case DOWN -> walkDown[this.currentFrame];
            };
            batch.draw(currentFrame, x, y, 96, 96);
            Gdx.app.log("Enemy", "Rendering frame: x=" + x + ", y=" + y + ", frame=" + this.currentFrame);
        }
    }

    public void dispose() {
        enemyWalkSheet.dispose();
        enemyAttackSheet.dispose();
        enemyHurtSheet.dispose();
        enemyDeathSheet.dispose();
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
        if (health <= 0 && !isDead) {
            isDead = true;
            deathAnimationTimer = DEATH_ANIMATION_DURATION;
            animationTime = 0f;
            Gdx.app.log("Enemy", "Enemy died, starting death animation");
        }
    }

    public boolean isInAssignedRoom() {
        if (assignedRoom == null) return false;
        int tileX = (int)(x / Level.TILE_SIZE);
        int tileY = (int)(y / Level.TILE_SIZE);
        return tileX >= assignedRoom.x && tileX < assignedRoom.x + assignedRoom.width &&
            tileY >= assignedRoom.y && tileY < assignedRoom.y + assignedRoom.height;
    }
}
