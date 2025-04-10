package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Player {
    private float x, y;
    private float speed = 200f;
    private Texture playerSheet;
    private TextureRegion[] walkLeft, walkRight, walkUp, walkDown;
    private float animationTime;
    private int currentFrame;
    private int[][] levelMap;

    public Player(int[][] levelMap) {
        this.levelMap = levelMap;
        playerSheet = new Texture("player.png");
        animationTime = 0f;

        walkRight = new TextureRegion[4];
        walkLeft = new TextureRegion[4];
        walkUp = new TextureRegion[4];
        walkDown = new TextureRegion[4];

        for (int i = 0; i < 4; i++) {
            walkRight[i] = new TextureRegion(playerSheet, i * 64, 192, 64, 64);
            walkLeft[i] = new TextureRegion(playerSheet, i * 64, 128, 64, 64);
            walkUp[i] = new TextureRegion(playerSheet, i * 64, 64, 64, 64);
            walkDown[i] = new TextureRegion(playerSheet, i * 64, 0, 64, 64);
        }
    }

    public void setStartPosition() {
        int startX = 3;
        int startY = 3;
        x = startX * Level.TILE_SIZE;
        y = startY * Level.TILE_SIZE;

        if (levelMap[startX][startY] != 0) {
            for (int i = 1; i < 19; i++) {
                for (int j = 1; j < 14; j++) {
                    if (levelMap[i][j] == 0) {
                        x = i * Level.TILE_SIZE;
                        y = j * Level.TILE_SIZE;
                        return;
                    }
                }
            }
        }
    }

    public void handleInput() {
        float newX = x;
        float newY = y;
        float delta = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            newX -= speed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            newX += speed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            newY += speed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            newY -= speed * delta;
        }

        if (!checkCollision(newX, newY)) {
            x = newX;
            y = newY;
        }

        updateAnimation();
    }

    private void updateAnimation() {
        TextureRegion[] currentAnimation = walkDown;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) currentAnimation = walkLeft;
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
        int[] checkX = new int[]{(int)x / Level.TILE_SIZE, (int)(x + Level.TILE_SIZE - 1) / Level.TILE_SIZE};
        int[] checkY = new int[]{(int)y / Level.TILE_SIZE, (int)(y + Level.TILE_SIZE - 1) / Level.TILE_SIZE};

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
    }
}
