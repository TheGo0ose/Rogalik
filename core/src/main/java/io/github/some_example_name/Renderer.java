package io.github.some_example_name;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.List;

public class Renderer {
    private SpriteBatch batch;
    private Level level;
    private Player player;
    private List<Enemy> enemies;

    public Renderer(SpriteBatch batch, Level level, Player player, List<Enemy> enemies) {
        this.batch = batch;
        this.level = level;
        this.player = player;
        this.enemies = enemies;
    }

    public void render() {
        batch.begin();
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 15; y++) {
                if (level.getLevelMap()[x][y] == 0) {
                    batch.draw(level.getFloorRegion(), x * Level.TILE_SIZE, y * Level.TILE_SIZE);
                } else if (level.getLevelMap()[x][y] == 1) {
                    batch.draw(level.getWallRegion(x, y), x * Level.TILE_SIZE, y * Level.TILE_SIZE);
                }
            }
        }
        for (Enemy enemy : enemies) {
            enemy.render(batch);
        }
        batch.draw(player.getCurrentFrame(), player.getX(), player.getY());
        batch.end();
    }
}
