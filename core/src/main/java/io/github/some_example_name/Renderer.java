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

        // Отрисовка пола
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                TextureRegion floorTexture = level.getFloorRegion(x, y);
                if (floorTexture != null) {
                    batch.draw(floorTexture, x * Level.TILE_SIZE, y * Level.TILE_SIZE);
                }
            }
        }

        // Отрисовка стен
        for (int x = 0; x < mapWidth; x++) {
            for (int y = 0; y < mapHeight; y++) {
                TextureRegion wallTexture = level.getWallRegion(x, y);
                if (wallTexture != null) {
                    batch.draw(wallTexture, x * Level.TILE_SIZE, y * Level.TILE_SIZE);
                }
            }
        }

        // Отрисовка врагов
        for (Enemy enemy : enemies) {
            enemy.render(batch);
        }

        // Отрисовка игрока
        batch.draw(player.getCurrentFrame(), player.getX(), player.getY());

        // Отрисовка экипированного меча (48x48) справа от персонажа
        Texture weaponTexture = player.getEquippedWeaponTexture();
        if (weaponTexture != null) {
            float weaponX = player.getX() + Level.TILE_SIZE; // 64 пикселя (ширина персонажа)
            float weaponY = player.getY() + (Level.TILE_SIZE - 48) / 2; // Центрируем по вертикали
            batch.draw(weaponTexture, weaponX, weaponY, 48, 48);
        }

        batch.end(); // Завершаем рендеринг
    }

    public void dispose() {
        // Освобождение ресурсов, если нужно
        batch.dispose();
    }
}
