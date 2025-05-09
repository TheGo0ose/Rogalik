package io.github.some_example_name;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.List;

public class Renderer {
    private final SpriteBatch batch;
    private final Level level;
    private final Player player;
    private final List<Enemy> enemies;

    public Renderer(SpriteBatch batch, Level level, Player player, List<Enemy> enemies) {
        this.batch = batch;
        this.level = level;
        this.player = player;
        this.enemies = enemies;
    }

    public void render() {
        batch.begin();

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

        batch.end();
    }
}
