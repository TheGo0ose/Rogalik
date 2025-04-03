package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

import java.util.ArrayList;
import java.util.List;

public class MainGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture playerSheet;
    private Texture wallSheet;
    private Texture enemySheet;
    private TextureRegion wallRegion;
    private TextureRegion floorRegion;
    private TextureRegion[] walkLeft, walkRight, walkUp, walkDown;
    private float animationTime;
    private int currentFrame;
    private OrthographicCamera camera;
    private List<Enemy> enemies;

    private float playerX = 100;
    private float playerY = 100;
    private float playerSpeed = 200f;

    private static final int TILE_SIZE = 64; // Исправлено на 64 для соответствия текстурам

    private int[][] levelMap;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        camera.update();
        batch = new SpriteBatch();

        playerSheet = new Texture("player.png");
        wallSheet = new Texture("wall.png");
        enemySheet = new Texture("enemy.png");

        // Анимации игрока
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

        wallRegion = new TextureRegion(wallSheet, 128, 256, 64, 64);
        floorRegion = new TextureRegion(wallSheet, 0, 384, 64, 64);

        animationTime = 0f;

        generateLevel();

        // Инициализация врагов
        enemies = new ArrayList<>();
        spawnEnemies(3); // Создаём 3 врагов
    }

    private void generateLevel() {
        levelMap = new int[20][15];
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 15; y++) {
                if (x == 0 || x == 19 || y == 0 || y == 14) {
                    levelMap[x][y] = 1;
                } else {
                    levelMap[x][y] = Math.random() < 0.2 ? 1 : 0;
                }
            }
        }
        levelMap[3][3] = 0; // Свободная стартовая позиция
    }

    private void spawnEnemies(int count) {
        for (int i = 0; i < count; i++) {
            float x, y;
            do {
                x = MathUtils.random(1, 18) * TILE_SIZE; // Случайная позиция
                y = MathUtils.random(1, 13) * TILE_SIZE;
            } while (levelMap[(int)(x / TILE_SIZE)][(int)(y / TILE_SIZE)] == 1); // Пока не найдём свободное место
            enemies.add(new Enemy(enemySheet, x, y));
        }
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Обработка ввода игрока
        handleInput();

        // Обновление врагов
        for (Enemy enemy : enemies) {
            enemy.update(playerX, playerY, levelMap);
        }

        camera.position.set(playerX + TILE_SIZE / 2, playerY + TILE_SIZE / 2, 0);
        camera.update();

        batch.setProjectionMatrix(camera.combined);

        TextureRegion[] currentAnimation = walkDown;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) currentAnimation = walkLeft;
        else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) currentAnimation = walkRight;
        else if (Gdx.input.isKeyPressed(Input.Keys.UP)) currentAnimation = walkUp;
        else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) currentAnimation = walkDown;

        if (isMoving()) {
            animationTime += Gdx.graphics.getDeltaTime();
            currentFrame = (int)(animationTime * 10) % currentAnimation.length;
        } else {
            currentFrame = 2; // Стоящий кадр
        }

        batch.begin();
        // Отрисовка пола
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 15; y++) {
                if (levelMap[x][y] == 0) {
                    batch.draw(floorRegion, x * TILE_SIZE, y * TILE_SIZE);
                }
            }
        }
        // Отрисовка стен
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 15; y++) {
                if (levelMap[x][y] == 1) {
                    batch.draw(wallRegion, x * TILE_SIZE, y * TILE_SIZE);
                }
            }
        }
        // Отрисовка врагов
        for (Enemy enemy : enemies) {
            enemy.render(batch);
        }
        // Отрисовка игрока
        batch.draw(currentAnimation[currentFrame], playerX, playerY);
        batch.end();
    }

    private void handleInput() {
        float newX = playerX;
        float newY = playerY;
        float delta = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            newX -= playerSpeed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            newX += playerSpeed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            newY += playerSpeed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            newY -= playerSpeed * delta;
        }

        if (!checkCollision(newX, newY)) {
            playerX = newX;
            playerY = newY;
        }
    }

    private boolean checkCollision(float x, float y) {
        int mapX = (int)(x / TILE_SIZE);
        int mapY = (int)(y / TILE_SIZE);

        if (mapX < 0 || mapX >= 20 || mapY < 0 || mapY >= 15) {
            return true;
        }

        return levelMap[mapX][mapY] == 1;
    }

    private boolean isMoving() {
        return Gdx.input.isKeyPressed(Input.Keys.LEFT) ||
            Gdx.input.isKeyPressed(Input.Keys.RIGHT) ||
            Gdx.input.isKeyPressed(Input.Keys.UP) ||
            Gdx.input.isKeyPressed(Input.Keys.DOWN);
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (playerSheet != null) playerSheet.dispose();
        if (wallSheet != null) wallSheet.dispose();
        if (enemySheet != null) enemySheet.dispose();
    }

    private class Enemy {
        private float x, y;
        private float speed = 100f; // Скорость врага
        private TextureRegion[] walkLeft, walkRight, walkUp, walkDown;
        private float animationTime;
        private int currentFrame;
        private TextureRegion[] currentAnimation;

        public Enemy(Texture sheet, float x, float y) {
            this.x = x;
            this.y = y;

            // Анимации врага
            walkRight = new TextureRegion[4];
            walkLeft = new TextureRegion[4];
            walkUp = new TextureRegion[4];
            walkDown = new TextureRegion[4];

            for (int i = 0; i < 4; i++) {
                walkRight[i] = new TextureRegion(sheet, i * 64, 0, 64, 64);
                walkLeft[i] = new TextureRegion(sheet, i * 64, 64, 64, 64);
                walkUp[i] = new TextureRegion(sheet, i * 64, 128, 64, 64);
                walkDown[i] = new TextureRegion(sheet, i * 64, 192, 64, 64);
            }

            animationTime = 0f;
            currentAnimation = walkDown; // По умолчанию вниз
        }

        public void update(float playerX, float playerY, int[][] levelMap) {
            float delta = Gdx.graphics.getDeltaTime();
            float newX = x;
            float newY = y;

            // Движение к игроку
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

            // Проверка коллизий
            if (!checkCollision(newX, newY, levelMap)) {
                x = newX;
                y = newY;
            }

            // Анимация
            animationTime += delta;
            currentFrame = (int)(animationTime * 10) % currentAnimation.length;
        }

        private boolean checkCollision(float x, float y, int[][] levelMap) {
            int mapX = (int)(x / TILE_SIZE);
            int mapY = (int)(y / TILE_SIZE);

            if (mapX < 0 || mapX >= 20 || mapY < 0 || mapY >= 15) {
                return true;
            }

            return levelMap[mapX][mapY] == 1;
        }

        public void render(SpriteBatch batch) {
            batch.draw(currentAnimation[currentFrame], x, y);
        }
    }
}
