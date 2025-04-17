package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

public class Level {
    public static final int TILE_SIZE = 64;
    private int[][] levelMap;
    private Texture wallSheet;
    private TextureRegion[] wallRegion;
    private TextureRegion floorRegion;

    public Level() {
        levelMap = new int[20][15];
        wallSheet = new Texture("wall.png");

        wallRegion = new TextureRegion[10];
        for (int i = 0; i < 10; i++) {
            wallRegion[i] = new TextureRegion(wallSheet, i * 64, 0, 64, 64);
        }
        floorRegion = new TextureRegion(wallSheet, 0, 640, 64, 64);
    }

    public void generateLevel() {
        // Заполняем всё стенами
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 15; y++) {
                levelMap[x][y] = 1; // 1 — стена
            }
        }

        // Создаём границы уровня
        for (int x = 0; x < 20; x++) {
            levelMap[x][0] = 1;
            levelMap[x][14] = 1;
        }
        for (int y = 0; y < 15; y++) {
            levelMap[0][y] = 1;
            levelMap[19][y] = 1;
        }

        // Генерация уровня с помощью Random Walk
        randomWalkGeneration(10, 7, 300); // Начинаем с центра, 300 шагов
    }

    private void randomWalkGeneration(int startX, int startY, int steps) {
        int x = startX;
        int y = startY;

        // Устанавливаем начальную точку как пол
        levelMap[x][y] = 0;

        // Выполняем случайное блуждание
        for (int i = 0; i < steps; i++) {
            // Выбираем случайное направление
            int direction = MathUtils.random(3);
            switch (direction) {
                case 0: // Вверх
                    if (y + 1 < 14) y++;
                    break;
                case 1: // Вниз
                    if (y - 1 > 0) y--;
                    break;
                case 2: // Влево
                    if (x - 1 > 0) x--;
                    break;
                case 3: // Вправо
                    if (x + 1 < 19) x++;
                    break;
            }

            // Устанавливаем текущую точку как пол
            levelMap[x][y] = 0;
        }

        // Убедимся, что начальная точка открыта
        ensureOpenStartArea(startX, startY);
    }

    private void ensureOpenStartArea(int startX, int startY) {
        // Создаём открытую зону 3x3 вокруг стартовой позиции
        for (int x = startX - 1; x <= startX + 1; x++) {
            for (int y = startY - 1; y <= startY + 1; y++) {
                if (x > 0 && x < 19 && y > 0 && y < 14) {
                    levelMap[x][y] = 0;
                }
            }
        }
    }

    public TextureRegion getWallRegion(int x, int y) {
        if (levelMap[x][y] != 1) return null;

        boolean top = y < 14 && levelMap[x][y + 1] == 1;
        boolean bottom = y > 0 && levelMap[x][y - 1] == 1;
        boolean left = x > 0 && levelMap[x - 1][y] == 1;
        boolean right = x < 19 && levelMap[x + 1][y] == 1;

        if (top && bottom && left && right) return wallRegion[0];
        else if (top && bottom && left) return wallRegion[1];
        else if (top && bottom && right) return wallRegion[2];
        else if (top && bottom) return wallRegion[3];
        else if (left && right) return wallRegion[4];
        else if (top && left) return wallRegion[5];
        else if (top && right) return wallRegion[6];
        else if (bottom && left) return wallRegion[7];
        else if (bottom && right) return wallRegion[8];
        else return wallRegion[9];
    }

    public int[][] getLevelMap() { return levelMap; }
    public TextureRegion getFloorRegion() { return floorRegion; }

    public void dispose() {
        wallSheet.dispose();
    }
}
