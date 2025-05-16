package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Player {
    private float x, y;
    private float speed = 200f;
    private float baseSpeed = 200f;
    private Texture playerSheet;
    private TextureRegion[] walkLeft, walkRight, walkUp, walkDown;
    private float animationTime;
    private int currentFrame;
    private int[][] levelMap;

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

    public Player(int[][] levelMap) {
        this.levelMap = levelMap;
        playerSheet = new Texture("player2.png");
        animationTime = 0f;

        walkRight = new TextureRegion[4];
        walkLeft = new TextureRegion[4];
        walkUp = new TextureRegion[4];
        walkDown = new TextureRegion[4];

        for (int i = 0; i < 4; i++) {
            walkRight[i] = new TextureRegion(playerSheet, i * 48, 192, 64, 64);
            walkUp[i] = new TextureRegion(playerSheet, i * 48, 128, 64, 64);
            walkLeft[i] = new TextureRegion(playerSheet, i * 48, 64, 64, 64);
            walkDown[i] = new TextureRegion(playerSheet, i * 48, 0, 64, 64);
        }

        inventory = new Inventory();
        // Добавляем базовый меч
        Item rustySword = new Item("Железный меч", "Меч который дается всем воинам, перед войной", Item.ItemType.WEAPON, 5, 0, 0, "base_sword.png");
        inventory.addItem(rustySword);
        equipWeapon(rustySword); // Автоматически экипируем базовый меч

        this.damage = baseDamage + (equippedWeapon != null ? equippedWeapon.getDamageBonus() : 0);
        this.defense = 0;
        this.speedEffectTimer = 0;
        this.speedEffectMultiplier = 1;
    }

    public void setStartPosition() {
        int startX = 10;
        int startY = 7;

        if (levelMap[startX][startY] == 0 && isValidStartPosition(startX, startY)) {
            x = startX * Level.TILE_SIZE;
            y = startY * Level.TILE_SIZE;
        } else {
            for (int i = 1; i < 19; i++) {
                for (int j = 1; j < 14; j++) {
                    if (levelMap[i][j] == 0 && isValidStartPosition(i, j)) {
                        x = i * Level.TILE_SIZE;
                        y = j * Level.TILE_SIZE;
                        return;
                    }
                }
            }
            Gdx.app.log("Player", "Не удалось найти подходящую стартовую позицию!");
        }
    }

    private boolean isValidStartPosition(int x, int y) {
        int openPaths = 0;
        if (x > 0 && levelMap[x - 1][y] == 0) openPaths++;
        if (x < 19 && levelMap[x + 1][y] == 0) openPaths++;
        if (y > 0 && levelMap[x][y - 1] == 0) openPaths++;
        if (y < 14 && levelMap[x][y + 1] == 0) openPaths++;
        return openPaths >= 2;
    }

    public void handleInput() {
        float newX = x;
        float newY = y;
        float delta = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(Input.Keys.A)) newX -= speed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) newX += speed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) newY += speed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) newY -= speed * delta;

        if (!checkCollision(newX, newY)) {
            x = newX;
            y = newY;
        }

        updateAnimation();
        updateEffects(delta);
    }

    private void updateAnimation() {
        TextureRegion[] currentAnimation = walkDown;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) currentAnimation = walkLeft;
        else if (Gdx.input.isKeyPressed(Input.Keys.D)) currentAnimation = walkRight;
        else if (Gdx.input.isKeyPressed(Input.Keys.W)) currentAnimation = walkUp;
        else if (Gdx.input.isKeyPressed(Input.Keys.S)) currentAnimation = walkDown;

        if (isMoving()) {
            animationTime += Gdx.graphics.getDeltaTime();
            currentFrame = (int)(animationTime * 10) % currentAnimation.length;
        } else {
            currentFrame = 2;
        }
    }

    private boolean checkCollision(float x, float y) {
        // Учитываем новый размер персонажа (96x96)
        int[] checkX = new int[]{(int)x / Level.TILE_SIZE, (int)(x + 96 - 1) / Level.TILE_SIZE};
        int[] checkY = new int[]{(int)y / Level.TILE_SIZE, (int)(y + 96 - 1) / Level.TILE_SIZE};

        for (int cx : checkX) {
            for (int cy : checkY) {
                if (cx < 0 || cx >= 20 || cy < 0 || cy >= 15 || levelMap[cx][cy] == 1) {
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
        if (Gdx.input.isKeyPressed(Input.Keys.A)) return walkLeft[currentFrame];
        if (Gdx.input.isKeyPressed(Input.Keys.D)) return walkRight[currentFrame];
        if (Gdx.input.isKeyPressed(Input.Keys.W)) return walkUp[currentFrame];
        if (Gdx.input.isKeyPressed(Input.Keys.S)) return walkDown[currentFrame];
        return walkDown[currentFrame];
    }

    public void dispose() {
        playerSheet.dispose();
        if (equippedWeapon != null) equippedWeapon.dispose();
        if (equippedArmor != null) equippedArmor.dispose();
    }
}
