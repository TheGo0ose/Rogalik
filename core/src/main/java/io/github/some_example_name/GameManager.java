package io.github.some_example_name;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

public class GameManager {
    private SpriteBatch batch;
    private Player player;
    private Level level;
    private List<Enemy> enemies;
    private Renderer renderer;
    private OrthographicCamera camera;

    public GameManager(OrthographicCamera camera) {
        this.camera = camera;
        batch = new SpriteBatch();
        enemies = new ArrayList<>();
    }

    public void initialize() {
        level = new Level();
        level.generateLevel();

        player = new Player(level.getLevelMap());
        player.setStartPosition();

        spawnEnemies(3);

        renderer = new Renderer(batch, level, player, enemies);
    }

    private void spawnEnemies(int count) {
        for (int i = 0; i < count; i++) {
            enemies.add(new Enemy(level.getLevelMap()));
        }
    }

    public void update() {
        player.handleInput();
        for (Enemy enemy : enemies) {
            enemy.update(player.getX(), player.getY());
        }
        camera.position.set(player.getX() + Level.TILE_SIZE / 2, player.getY() + Level.TILE_SIZE / 2, 0);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
    }

    public void render() {
        renderer.render();
    }

    public void dispose() {
        batch.dispose();
        player.dispose();
        level.dispose();
        for (Enemy enemy : enemies) {
            enemy.dispose();
        }
    }
}
