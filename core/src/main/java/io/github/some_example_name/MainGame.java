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
    private TextureRegion[] wallRegion;
    private TextureRegion floorRegion;
    private TextureRegion[] walkLeft, walkRight, walkUp, walkDown;
    private float animationTime;
    private int currentFrame;
    private OrthographicCamera camera;
    private List<Enemy> enemies;

    private float playerX; // Убраны жёсткие значения
    private float playerY; // Убраны жёсткие значения
    private float playerSpeed = 200f;

    private static final int TILE_SIZE = 64;

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

        // Загружаем текстуры стен (первые 5 тайлов в верхнем ряду)
        wallRegion = new TextureRegion[5];
        for (int i = 0; i < 5; i++) {
            wallRegion[i] = new TextureRegion(wallSheet, i * 64, 0, 64, 64);
        }

        // Пол (возьмём из нижнего ряда, например, первый тайл)
        floorRegion = new TextureRegion(wallSheet, 0, 384, 64, 64);

        animationTime = 0f;

        generateLevel();

        // Устанавливаем начальную позицию игрока
        setPlayerStartPosition();

        // Инициализация врагов
        enemies = new ArrayList<>();
        spawnEnemies(3); // Создаём 3 врагов
    }

    private void generateLevel() {
        levelMap = new int[20][15];
        // Заполняем всё полом
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 15; y++) {
                levelMap[x][y] = 0; // 0 — пол
            }
        }

        // Создаём границы уровня
        for (int x = 0; x < 20; x++) {
            levelMap[x][0] = 1; // Нижняя стена
            levelMap[x][14] = 1; // Верхняя стена
        }
        for (int y = 0; y < 15; y++) {
            levelMap[0][y] = 1; // Левая стена
            levelMap[19][y] = 1; // Правая стена
        }

        // Создаём несколько комнат
        generateRoom(2, 2, 5, 5); // Комната 1
        generateRoom(8, 8, 5, 5); // Комната 2
        generateRoom(14, 2, 4, 4); // Комната 3

        // Соединяем комнаты коридорами
        connectRooms(4, 4, 10, 10); // Коридор между комнатами 1 и 2
        connectRooms(10, 10, 16, 4); // Коридор между комнатами 2 и 3

        // Убедимся, что стартовая позиция свободна
        levelMap[3][3] = 0;
    }

    private void generateRoom(int startX, int startY, int width, int height) {
        // Создаём стены комнаты
        for (int x = startX; x < startX + width; x++) {
            for (int y = startY; y < startY + height; y++) {
                if (x == startX || x == startX + width - 1 || y == startY || y == startY + height - 1) {
                    levelMap[x][y] = 1; // Стены
                } else {
                    levelMap[x][y] = 0; // Пол внутри комнаты
                }
            }
        }
    }

    private void connectRooms(int x1, int y1, int x2, int y2) {
        // Горизонтальный коридор
        int startX = Math.min(x1, x2);
        int endX = Math.max(x1, x2);
        for (int x = startX; x <= endX; x++) {
            levelMap[x][y1] = 0; // Пол коридора
        }

        // Вертикальный коридор
        int startY = Math.min(y1, y2);
        int endY = Math.max(y1, y2);
        for (int y = startY; y <= endY; y++) {
            levelMap[x2][y] = 0; // Пол коридора
        }
    }

    private void setPlayerStartPosition() {
        // Устанавливаем игрока в клетку [3][3], которая гарантированно свободна
        int startX = 3; // Координата X в тайлах
        int startY = 3; // Координата Y в тайлах
        playerX = startX * TILE_SIZE;
        playerY = startY * TILE_SIZE;

        // Дополнительная проверка на всякий случай
        if (levelMap[startX][startY] != 0) {
            // Если клетка [3][3] почему-то занята, ищем ближайшую свободную
            for (int x = 1; x < 19; x++) {
                for (int y = 1; y < 14; y++) {
                    if (levelMap[x][y] == 0) {
                        playerX = x * TILE_SIZE;
                        playerY = y * TILE_SIZE;
                        return;
                    }
                }
            }
        }
    }

    private void spawnEnemies(int count) {
        for (int i = 0; i < count; i++) {
            float x, y;
            do {
                x = MathUtils.random(1, 18) * TILE_SIZE;
                y = MathUtils.random(1, 13) * TILE_SIZE;
            } while (levelMap[(int)(x / TILE_SIZE)][(int)(y / TILE_SIZE)] == 1);
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
        if (Gdx.input.isKeyPressed(Input.Keys.A)) currentAnimation = walkLeft;
        else if (Gdx.input.isKeyPressed(Input.Keys.D)) currentAnimation = walkRight;
        else if (Gdx.input.isKeyPressed(Input.Keys.W)) currentAnimation = walkUp;
        else if (Gdx.input.isKeyPressed(Input.Keys.S)) currentAnimation = walkDown;

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
        // Отрисовка стен с выбором текстуры
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 15; y++) {
                if (levelMap[x][y] == 1) {
                    // Выбираем текстуру стены в зависимости от соседей
                    TextureRegion wallTexture = chooseWallTexture(x, y);
                    batch.draw(wallTexture, x * TILE_SIZE, y * TILE_SIZE);
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

        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            newX -= playerSpeed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            newX += playerSpeed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            newY += playerSpeed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            newY -= playerSpeed * delta;
        }

        if (!checkCollision(newX, newY)) {
            playerX = newX;
            playerY = newY;
        }
    }

    private TextureRegion chooseWallTexture(int x, int y) {
        // Простая логика выбора текстуры стены на основе соседей
        boolean top = y < 14 && levelMap[x][y + 1] == 1;
        boolean bottom = y > 0 && levelMap[x][y - 1] == 1;
        boolean left = x > 0 && levelMap[x - 1][y] == 1;
        boolean right = x < 19 && levelMap[x + 1][y] == 1;

        if (top && bottom && left && right) {
            return wallRegion[0]; // Полностью окружённая стена
        } else if (top && bottom && left) {
            return wallRegion[1]; // Угловая стена (например, правая сторона открыта)
        } else if (top && bottom && right) {
            return wallRegion[2]; // Угловая стена (левая сторона открыта)
        } else if (top && bottom) {
            return wallRegion[3]; // Вертикальная стена
        } else {
            return wallRegion[4]; // Одиночный блок или горизонтальная стена
        }
    }

    private boolean checkCollision(float x, float y) {
        int mapX = (int)(x / TILE_SIZE);
        int mapY = (int)(y / TILE_SIZE);

        int[] checkX = new int[]{(int)x / TILE_SIZE, (int)(x + TILE_SIZE - 1) / TILE_SIZE};
        int[] checkY = new int[]{(int)y / TILE_SIZE, (int)(y + TILE_SIZE - 1) / TILE_SIZE};

        for (int cx : checkX) {
            for (int cy : checkY) {
                if (cx < 0 || cx >= 20 || cy < 0 || cy >= 15) {
                    return true;
                }
                if (levelMap[cx][cy] == 1) {
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

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (playerSheet != null) playerSheet.dispose();
        if (wallSheet != null) wallSheet.dispose();
        if (enemySheet != null) enemySheet.dispose();
    }

    private class Enemy {
        private float x, y;
        private float speed = 100f;
        private TextureRegion[] walkD, walkA, walkW, walkS;
        private float animationTime;
        private int currentFrame;
        private TextureRegion[] currentAnimation;
        private float lastX, lastY; // Для отслеживания движения

        public Enemy(Texture sheet, float x, float y) {
            this.x = x;
            this.y = y;
            this.lastX = x;
            this.lastY = y;

            walkD = new TextureRegion[4];
            walkA = new TextureRegion[4];
            walkW = new TextureRegion[4];
            walkS = new TextureRegion[4];

            for (int i = 0; i < 4; i++) {
                walkD[i] = new TextureRegion(sheet, i * 64, 0, 64, 64);
                walkA[i] = new TextureRegion(sheet, i * 64, 64, 64, 64);
                walkW[i] = new TextureRegion(sheet, i * 64, 128, 64, 64);
                walkS[i] = new TextureRegion(sheet, i * 64, 192, 64, 64);
            }

            animationTime = 0f;
            currentAnimation = walkS;
        }

        public void update(float playerX, float playerY, int[][] levelMap) {
            float delta = Gdx.graphics.getDeltaTime();
            float newX = x;
            float newY = y;

            // Сохраняем предыдущие координаты для проверки движения
            lastX = x;
            lastY = y;

            // Движение к игроку
            if (playerX > x) {
                newX += speed * delta;
                currentAnimation = walkD;
            } else if (playerX < x) {
                newX -= speed * delta;
                currentAnimation = walkA;
            }
            if (playerY > y) {
                newY += speed * delta;
                currentAnimation = walkW;
            } else if (playerY < y) {
                newY -= speed * delta;
                currentAnimation = walkS;
            }

            // Проверка коллизий
            if (!checkCollision(newX, newY, levelMap)) {
                x = newX;
                y = newY;
            }

            // Анимация
            if (isMoving()) {
                animationTime += delta;
                currentFrame = (int)(animationTime * 10) % currentAnimation.length;
            } else {
                currentFrame = 2; // Стоящий кадр, как у игрока
            }
        }

        private boolean checkCollision(float x, float y, int[][] levelMap) {
            int mapX = (int)(x / TILE_SIZE);
            int mapY = (int)(y / TILE_SIZE);

            if (mapX < 0 || mapX >= 20 || mapY < 0 || mapY >= 15) {
                return true;
            }

            return levelMap[mapX][mapY] == 1;
        }

        private boolean isMoving() {
            return x != lastX || y != lastY;
        }

        public void render(SpriteBatch batch) {
            batch.draw(currentAnimation[currentFrame], x, y);
        }
    }
}
