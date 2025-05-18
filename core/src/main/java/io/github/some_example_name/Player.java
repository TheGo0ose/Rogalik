package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Player {
    private float x, y;
    private float speed = 200f;
    private float baseSpeed = 200f;
    private Texture playerSheet;
    private Texture playerAttackSheet;
    private TextureRegion[] walkLeft, walkRight, walkUp, walkDown;
    private TextureRegion[] attackRight, attackLeft, attackUp, attackDown;
    private float animationTime;
    private int currentFrame;
    private boolean isAttacking;
    private float attackAnimationTimer;
    private final float ATTACK_ANIMATION_DURATION = 0.6f;
    private int[][] levelMap;
    private Level level;

    private int health = 100;
    private int maxHealth = 100;
    private int baseDamage = 10;
    private int damage;
    private int defense;
    private Inventory inventory;

    private Item equippedWeapon;
    private Item equippedArmor;
    private float speedEffectTimer;
    private float speedEffectMultiplier;

    public enum Direction {
        RIGHT, LEFT, UP, DOWN
    }
    private Direction currentDirection = Direction.DOWN;
    private static final int WALK_FRAME_WIDTH = 48;
    private static final int WALK_FRAME_HEIGHT = 64;
    private static final int ATTACK_FRAME_WIDTH = 48;
    private static final int ATTACK_FRAME_HEIGHT = 64;
    private static final int FRAMES_PER_WALK = 4;
    private static final int FRAMES_PER_ATTACK = 8;

    public Player(int[][] levelMap, Level level) {
        this.levelMap = levelMap;
        this.level = level;
        try {
            playerSheet = new Texture("player2.png");
            playerAttackSheet = new Texture("player_attack.png");
            Gdx.app.log("Player", "Loaded player2.png, size=" + playerSheet.getWidth() + "x" + playerSheet.getHeight());
            Gdx.app.log("Player", "Loaded player_attack.png, size=" + playerAttackSheet.getWidth() + "x" + playerAttackSheet.getHeight());
        } catch (Exception e) {
            Gdx.app.error("Player", "Failed to load textures", e);
            throw e;
        }
        animationTime = 0f;
        isAttacking = false;
        attackAnimationTimer = 0f;

        walkRight = new TextureRegion[FRAMES_PER_WALK];
        walkLeft = new TextureRegion[FRAMES_PER_WALK];
        walkUp = new TextureRegion[FRAMES_PER_WALK];
        walkDown = new TextureRegion[FRAMES_PER_WALK];
        attackRight = new TextureRegion[FRAMES_PER_ATTACK];
        attackLeft = new TextureRegion[FRAMES_PER_ATTACK];
        attackUp = new TextureRegion[FRAMES_PER_ATTACK];
        attackDown = new TextureRegion[FRAMES_PER_ATTACK];

        for (int i = 0; i < FRAMES_PER_WALK; i++) {
            walkRight[i] = new TextureRegion(playerSheet, i * WALK_FRAME_WIDTH, 211, WALK_FRAME_WIDTH, WALK_FRAME_HEIGHT);
            walkUp[i] = new TextureRegion(playerSheet, i * WALK_FRAME_WIDTH, 150, WALK_FRAME_WIDTH, WALK_FRAME_HEIGHT);
            walkLeft[i] = new TextureRegion(playerSheet, i * WALK_FRAME_WIDTH, 85, WALK_FRAME_WIDTH, WALK_FRAME_HEIGHT);
            walkDown[i] = new TextureRegion(playerSheet, i * WALK_FRAME_WIDTH, 26, WALK_FRAME_WIDTH, WALK_FRAME_HEIGHT);
        }

        loadAttackAnimations();

        inventory = new Inventory();
        Item rustySword = new Item("Железный меч", "Меч который дается всем воинам, перед войной", Item.ItemType.WEAPON, 5, 0, 0, "base_sword.png");
        inventory.addItem(rustySword);
        equipWeapon(rustySword);

        this.damage = baseDamage + (equippedWeapon != null ? equippedWeapon.getDamageBonus() : 0);
        this.defense = 0;
        this.speedEffectTimer = 0;
        this.speedEffectMultiplier = 1;
    }

    private void loadAttackAnimations() {
        int sheetWidth = playerAttackSheet.getWidth();
        int sheetHeight = playerAttackSheet.getHeight();
        if (sheetWidth < ATTACK_FRAME_WIDTH * FRAMES_PER_ATTACK || sheetHeight < ATTACK_FRAME_HEIGHT * 4) {
            Gdx.app.error("Player", "player_attack.png must be at least " +
                (ATTACK_FRAME_WIDTH * FRAMES_PER_ATTACK) + "x" + (ATTACK_FRAME_HEIGHT * 4) +
                ", but is " + sheetWidth + "x" + sheetHeight);
            for (int i = 0; i < FRAMES_PER_ATTACK; i++) {
                attackRight[i] = walkDown[i % FRAMES_PER_WALK];
                attackLeft[i] = walkDown[i % FRAMES_PER_WALK];
                attackUp[i] = walkDown[i % FRAMES_PER_WALK];
                attackDown[i] = walkDown[i % FRAMES_PER_WALK];
            }
            return;
        }

        attackRight[0] = new TextureRegion(playerAttackSheet, 24, 145, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);
        attackRight[1] = new TextureRegion(playerAttackSheet, 89, 145, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);
        attackRight[2] = new TextureRegion(playerAttackSheet, 153, 145, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);
        attackRight[3] = new TextureRegion(playerAttackSheet, 218, 145, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);
        attackRight[4] = new TextureRegion(playerAttackSheet, 278, 145, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);
        attackRight[5] = new TextureRegion(playerAttackSheet, 344, 145, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);
        attackRight[6] = new TextureRegion(playerAttackSheet, 409, 145, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);
        attackRight[7] = new TextureRegion(playerAttackSheet, 474, 145, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);

        attackLeft[0] = new TextureRegion(playerAttackSheet, 24, 81, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);
        attackLeft[1] = new TextureRegion(playerAttackSheet, 89, 81, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);
        attackLeft[2] = new TextureRegion(playerAttackSheet, 153, 81, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);
        attackLeft[3] = new TextureRegion(playerAttackSheet, 218, 81, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);
        attackLeft[4] = new TextureRegion(playerAttackSheet, 278, 81, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);
        attackLeft[5] = new TextureRegion(playerAttackSheet, 344, 81, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);
        attackLeft[6] = new TextureRegion(playerAttackSheet, 409, 81, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);
        attackLeft[7] = new TextureRegion(playerAttackSheet, 474, 81, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);

        attackUp[0] = new TextureRegion(playerAttackSheet, 24, 208, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);
        attackUp[1] = new TextureRegion(playerAttackSheet, 89, 208, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);
        attackUp[2] = new TextureRegion(playerAttackSheet, 153, 208, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);
        attackUp[3] = new TextureRegion(playerAttackSheet, 218, 208, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);
        attackUp[4] = new TextureRegion(playerAttackSheet, 278, 208, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);
        attackUp[5] = new TextureRegion(playerAttackSheet, 344, 208, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);
        attackUp[6] = new TextureRegion(playerAttackSheet, 409, 208, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);
        attackUp[7] = new TextureRegion(playerAttackSheet, 474, 208, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);

        attackDown[0] = new TextureRegion(playerAttackSheet, 24, 26, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);
        attackDown[1] = new TextureRegion(playerAttackSheet, 89, 26, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);
        attackDown[2] = new TextureRegion(playerAttackSheet, 153, 26, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);
        attackDown[3] = new TextureRegion(playerAttackSheet, 218, 26, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);
        attackDown[4] = new TextureRegion(playerAttackSheet, 278, 26, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);
        attackDown[5] = new TextureRegion(playerAttackSheet, 344, 26, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);
        attackDown[6] = new TextureRegion(playerAttackSheet, 409, 26, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);
        attackDown[7] = new TextureRegion(playerAttackSheet, 474, 26, ATTACK_FRAME_WIDTH, ATTACK_FRAME_HEIGHT);

        Gdx.app.log("Player", "Loaded attack animations with explicit coordinates");
    }

    public void setStartPosition() {
        Room startRoom = null;
        for (Room room : level.rooms) {
            if (!room.neighbors.isEmpty()) {
                startRoom = room;
                break;
            }
        }

        if (startRoom != null) {
            boolean found = false;
            for (int i = startRoom.x + 1; i < startRoom.x + startRoom.width - 1; i++) {
                for (int j = startRoom.y + 1; j < startRoom.y + startRoom.height - 1; j++) {
                    if (levelMap[i][j] == 0) {
                        x = i * Level.TILE_SIZE;
                        y = j * Level.TILE_SIZE;
                        found = true;
                        Gdx.app.log("Player", "Starting position set at (" + x + "," + y + ") in room at (" + startRoom.x + "," + startRoom.y + ")");
                        break;
                    }
                }
                if (found) break;
            }
            if (!found) {
                Gdx.app.log("Player", "Не удалось найти свободную клетку в комнате (" + startRoom.x + "," + startRoom.y + ")");
            }
        } else {
            for (int i = 1; i < levelMap.length; i++) {
                for (int j = 1; j < levelMap[0].length; j++) {
                    if (levelMap[i][j] == 0) {
                        x = i * Level.TILE_SIZE;
                        y = j * Level.TILE_SIZE;
                        Gdx.app.log("Player", "Fallback starting position set at (" + x + "," + y + ")");
                        return;
                    }
                }
            }
            Gdx.app.log("Player", "Не удалось найти подходящую стартовую позицию!");
        }
    }

    public void handleInput() {
        float newX = x;
        float newY = y;
        float delta = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            newX -= speed * delta;
            currentDirection = Direction.LEFT;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            newX += speed * delta;
            currentDirection = Direction.RIGHT;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            newY += speed * delta;
            currentDirection = Direction.UP;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            newY -= speed * delta;
            currentDirection = Direction.DOWN;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            triggerAttack();
        }

        if (!checkCollision(newX, newY)) {
            x = newX;
            y = newY;
        }

        updateAnimation();
        updateEffects(delta);
    }

    private void updateAnimation() {
        if (isMoving() || isAttacking) {
            animationTime += Gdx.graphics.getDeltaTime();
        }

        TextureRegion[] currentAnimation;
        int frameCount;

        if (isAttacking) {
            currentAnimation = switch (currentDirection) {
                case RIGHT -> attackRight;
                case LEFT -> attackLeft;
                case UP -> attackUp;
                case DOWN -> attackDown;
            };
            frameCount = FRAMES_PER_ATTACK;

            attackAnimationTimer -= Gdx.graphics.getDeltaTime();
            if (attackAnimationTimer <= 0) {
                isAttacking = false;
                animationTime = 0f;
                Gdx.app.log("Player", "Attack animation ended");
            }
        } else {
            currentAnimation = walkDown;
            if (Gdx.input.isKeyPressed(Input.Keys.A)) currentAnimation = walkLeft;
            else if (Gdx.input.isKeyPressed(Input.Keys.D)) currentAnimation = walkRight;
            else if (Gdx.input.isKeyPressed(Input.Keys.W)) currentAnimation = walkUp;
            else if (Gdx.input.isKeyPressed(Input.Keys.S)) currentAnimation = walkDown;
            frameCount = FRAMES_PER_WALK;
        }

        if (isMoving() || isAttacking) {
            currentFrame = (int)(animationTime * 10) % frameCount;
        } else {
            currentFrame = 2;
        }
        Gdx.app.log("Player", "Animation: isAttacking=" + isAttacking + ", Frame=" + currentFrame + ", Direction=" + currentDirection);
    }

    private boolean checkCollision(float x, float y) {
        float hitboxWidth = 24;
        float hitboxHeight = 24;
        float offsetX = (60 - hitboxWidth) / 2;
        float offsetY = (60 - hitboxHeight) / 2;

        float hitboxX = x + offsetX;
        float hitboxY = y + offsetY;

        int[] checkX = new int[]{(int)hitboxX / Level.TILE_SIZE, (int)(hitboxX + hitboxWidth - 1) / Level.TILE_SIZE};
        int[] checkY = new int[]{(int)hitboxY / Level.TILE_SIZE, (int)(hitboxY + hitboxHeight - 1) / Level.TILE_SIZE};

        for (int cx : checkX) {
            for (int cy : checkY) {
                if (cx < 0 || cx >= levelMap.length || cy < 0 || cy >= levelMap[0].length || levelMap[cx][cy] == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isMoving() {
        return Gdx.input.isKeyPressed(Input.Keys.A) ||
            Gdx.input.isKeyPressed(Input.Keys.D) ||
            Gdx.input.isKeyPressed(Input.Keys.W) ||
            Gdx.input.isKeyPressed(Input.Keys.S);
    }

    private void updateEffects(float delta) {
        if (speedEffectTimer > 0) {
            speedEffectTimer -= delta;
            if (speedEffectTimer <= 0) {
                speed = baseSpeed;
                speedEffectMultiplier = 1;
                Gdx.app.log("Player", "Speed effect ended");
            }
        }
    }

    public void triggerAttack() {
        isAttacking = true;
        attackAnimationTimer = ATTACK_ANIMATION_DURATION;
        animationTime = 0f;
        Gdx.app.log("Player", "Triggered attack animation for " + currentDirection);
    }

    public void equipWeapon(Item weapon) {
        if (weapon.getType() == Item.ItemType.WEAPON) {
            equippedWeapon = weapon;
            damage = baseDamage + weapon.getDamageBonus();
            if (weapon.getSpeedMultiplier() > 0 && speedEffectTimer <= 0) {
                speedEffectMultiplier = weapon.getSpeedMultiplier();
                speed = baseSpeed * speedEffectMultiplier;
                speedEffectTimer = weapon.getEffectDuration();
                Gdx.app.log("Player", "Equipped weapon with speed boost: " + weapon.getName());
            }
            Gdx.app.log("Player", "Equipped weapon: " + weapon.getName() + ", Damage: " + damage);
        }
    }

    public void equipArmor(Item armor) {
        if (armor.getType() == Item.ItemType.ARMOR) {
            equippedArmor = armor;
            defense = armor.getDefenseBonus();
            Gdx.app.log("Player", "Equipped armor: " + armor.getName() + ", Defense: " + defense);
        }
    }

    public void usePotion(Item potion) {
        if (potion.getType() == Item.ItemType.POTION) {
            if (potion.getHealthRestore() > 0) {
                setHealth(health + potion.getHealthRestore());
                Gdx.app.log("Player", "Used potion: " + potion.getName() + ", Restored " + potion.getHealthRestore() + " HP");
            }
            if (potion.getSpeedMultiplier() > 0) {
                speedEffectMultiplier = potion.getSpeedMultiplier();
                speed = baseSpeed * speedEffectMultiplier;
                speedEffectTimer = potion.getEffectDuration();
                Gdx.app.log("Player", "Used potion: " + potion.getName() + ", Speed increased to " + speed + " for " + potion.getEffectDuration() + " seconds");
            }
        }
    }

    public boolean isDead() {
        return health <= 0;
    }

    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public int getDamage() { return damage; }
    public int getDefense() { return defense; }
    public float getSpeed() { return speed; }
    public Inventory getInventory() { return inventory; }
    public Texture getEquippedWeaponTexture() { return equippedWeapon != null ? equippedWeapon.getTexture() : null; }

    public void setHealth(int health) { this.health = Math.max(0, Math.min(health, maxHealth)); }
    public void setDamage(int damage) { this.damage = damage; }
    public void setSpeed(float speed) { this.speed = speed; }

    public float getX() { return x; }
    public float getY() { return y; }

    public TextureRegion getCurrentFrame() {
        if (isAttacking) {
            return switch (currentDirection) {
                case RIGHT -> attackRight[currentFrame];
                case LEFT -> attackLeft[currentFrame];
                case UP -> attackUp[currentFrame];
                case DOWN -> attackDown[currentFrame];
            };
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) return walkLeft[currentFrame];
        if (Gdx.input.isKeyPressed(Input.Keys.D)) return walkRight[currentFrame];
        if (Gdx.input.isKeyPressed(Input.Keys.W)) return walkUp[currentFrame];
        if (Gdx.input.isKeyPressed(Input.Keys.S)) return walkDown[currentFrame];
        return walkDown[currentFrame];
    }

    public void render(SpriteBatch batch) {
        TextureRegion currentFrame = getCurrentFrame();
        batch.draw(currentFrame, x, y, 48, 64); // Отрисовка с размерами 48x64
        Gdx.app.log("Player", "Rendering frame: x=" + x + ", y=" + y + ", health=" + health);
    }

    public void dispose() {
        playerSheet.dispose();
        playerAttackSheet.dispose();
        if (equippedWeapon != null) equippedWeapon.dispose();
        if (equippedArmor != null) equippedArmor.dispose();
    }
}
