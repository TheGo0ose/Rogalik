package io.github.some_example_name;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Renderer {
    private SpriteBatch batch;
    private Player player;
    private Level level;
    private java.util.List<Enemy> enemies;

    public Renderer(SpriteBatch batch, Level level, Player player, java.util.List<Enemy> enemies) {
        this.batch = batch;
        this.level = level;
        this.player = player;
        this.enemies = enemies;
    }

    public void render() {
        batch.begin(); // Начинаем рендеринг

        int[][] levelMap = level.getLevelMap();
        int mapWidth = levelMap.length;
        int mapHeight = levelMap[0].length;

        // Отрисовка пола и стен с проверкой значений
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                if (levelMap[x][y] == 0) { // Только пол
                    TextureRegion floorTexture = level.getFloorRegion(x, y);
                    if (floorTexture != null) {
                        batch.draw(floorTexture, x * Level.TILE_SIZE, y * Level.TILE_SIZE, Level.TILE_SIZE, Level.TILE_SIZE);
                    }
                } else if (levelMap[x][y] == 1) { // Только стены
                    TextureRegion wallTexture = level.getWallRegion(x, y);
                    if (wallTexture != null) {
                        batch.draw(wallTexture, x * Level.TILE_SIZE, y * Level.TILE_SIZE, Level.TILE_SIZE, Level.TILE_SIZE);
                    }
                }
            }
        }

        // Отрисовка врагов
        for (Enemy enemy : enemies) {
            enemy.render(batch);
        }

        // Отрисовка игрока (увеличиваем размер до 96x96 пикселей)
        batch.draw(player.getCurrentFrame(), player.getX(), player.getY(), 96, 96);

        // Отрисовка экипированного меча (32x32) ближе к персонажу
        Texture weaponTexture = player.getEquippedWeaponTexture();
        if (weaponTexture != null) {
            float weaponX = player.getX() + 22; // Смещение на 22 пикселя справа от персонажа
            float weaponY = player.getY() + (96 - 32) / 2; // Центрируем по вертикали
            batch.draw(weaponTexture, weaponX, weaponY, 32, 32); // Уменьшаем размер меча до 32x32
        }

        batch.end(); // Завершаем рендеринг
    }
}
