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

        // Отрисовка пола
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 15; y++) {
                if (level.getLevelMap()[x][y] == 0) {
                    batch.draw(level.getFloorRegion(), x * Level.TILE_SIZE, y * Level.TILE_SIZE);
                }
            }
        }

        // Отрисовка стен
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 15; y++) {
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
