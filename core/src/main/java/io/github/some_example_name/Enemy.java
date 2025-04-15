package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

public class Enemy {
    private float x, y;
    private float speed = 100f;
    private final Texture enemySheet;
    private final TextureRegion[] walkLeft, walkRight, walkUp, walkDown;
    private float animationTime;
    private int currentFrame;
    private TextureRegion[] currentAnimation;
    private float lastX, lastY;
    private int[][] levelMap;

    public Enemy(int[][] levelMap) {
        this.levelMap = levelMap;
        enemySheet = new Texture("enemy.png");

        do {
            x = MathUtils.random(1, 18) * Level.TILE_SIZE;
            y = MathUtils.random(1, 13) * Level.TILE_SIZE;
        } while (levelMap[(int)(x / Level.TILE_SIZE)][(int)(y / Level.TILE_SIZE)] == 1);

        lastX = x;
        lastY = y;

        walkRight = new TextureRegion[4];
        walkLeft = new TextureRegion[4];
        walkUp = new TextureRegion[4];
        walkDown = new TextureRegion[4];

        for (int i = 0; i < 4; i++) {
            walkRight[i] = new TextureRegion(enemySheet, i * 64, 0, 64, 64);
            walkLeft[i] = new TextureRegion(enemySheet, i * 64, 64, 64, 64);
            walkUp[i] = new TextureRegion(enemySheet, i * 64, 128, 64, 64);
            walkDown[i] = new TextureRegion(enemySheet, i * 64, 192, 64, 64);
        }

        animationTime = 0f;
        currentAnimation = walkDown;
    }

    public void update(float playerX, float playerY) {
        float delta = Gdx.graphics.getDeltaTime();
        float newX = x;
        float newY = y;

        lastX = x;
        lastY = y;

        if (playerX > x) {
            newX += speed * delta;
            currentAnimation = walkRight;
        } else if (playerX < x) {
            newX -= speed * delta;
            currentAnimation = walkLeft;
        }
        if (playerY > y) {
            newY += speed * delta;
            currentAnimation = walkUp;
        } else if (playerY < y) {
            newY -= speed * delta;
            currentAnimation = walkDown;
        }

        if (!checkCollision(newX, newY)) {
            x = newX;
            y = newY;
        }

        if (isMoving()) {
            animationTime += delta;
            currentFrame = (int)(animationTime * 10) % currentAnimation.length;
        } else {
            currentFrame = 2;
        }
    }

    private boolean checkCollision(float x, float y) {
        int mapX = (int)(x / Level.TILE_SIZE);
        int mapY = (int)(y / Level.TILE_SIZE);

        return mapX < 0 || mapX >= 20 || mapY < 0 || mapY >= 15 || levelMap[mapX][mapY] == 1;
    }

    private boolean isMoving() {
        return x != lastX || y != lastY;
    }

    public void render(SpriteBatch batch) {
        batch.draw(currentAnimation[currentFrame], x, y);
    }

    public void dispose() {
        enemySheet.dispose();
    }
}
