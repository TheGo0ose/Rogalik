package io.github.some_example_name.render;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.some_example_name.Assets;
import io.github.some_example_name.entity.Direction;
import io.github.some_example_name.entity.Enemy;
import io.github.some_example_name.entity.Player;
import io.github.some_example_name.item.Item;
import io.github.some_example_name.world.GameWorld;
import io.github.some_example_name.world.Level;
import io.github.some_example_name.world.Tile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Рисует уровень и сущности. Только чтение состояния мира — никакой логики. */
public final class WorldRenderer {
    private static final float PLAYER_SCALE = 2f;
    private static final float SLIME_SCALE = 1.5f;
    private static final float WEAPON_ICON_SIZE = 32f;
    private static final Color HP_BACK = new Color(0.25f, 0.05f, 0.05f, 0.9f);
    private static final Color HP_FILL = new Color(0.85f, 0.15f, 0.15f, 1f);

    private final Assets assets;
    private final Sprites sprites;
    private final TextureRegion floor;
    private final TextureRegion wall;
    private final Texture pixel;
    /** Сортировка по Y: кто ниже на экране — тот ближе и рисуется позже. */
    private final List<Enemy> sortedEnemies = new ArrayList<>();
    private static final Comparator<Enemy> BY_Y_DESC = (a, b) -> Float.compare(b.getY(), a.getY());

    public WorldRenderer(Assets assets, Sprites sprites) {
        this.assets = assets;
        this.sprites = sprites;
        this.floor = new TextureRegion(assets.texture(Assets.FLOOR));
        this.wall = new TextureRegion(assets.texture(Assets.WALL));
        this.pixel = assets.whitePixel();
    }

    /** Вызывать между batch.begin() и batch.end() с проекцией мировой камеры. */
    public void render(SpriteBatch batch, OrthographicCamera camera, GameWorld world) {
        drawTiles(batch, camera, world.getLevel());

        Player player = world.getPlayer();
        sortedEnemies.clear();
        sortedEnemies.addAll(world.getEnemies());
        sortedEnemies.sort(BY_Y_DESC);

        boolean playerDrawn = false;
        for (Enemy enemy : sortedEnemies) {
            if (!playerDrawn && enemy.getY() < player.getY()) {
                drawPlayer(batch, player);
                playerDrawn = true;
            }
            drawEnemy(batch, enemy);
        }
        if (!playerDrawn) drawPlayer(batch, player);

        for (Enemy enemy : sortedEnemies) {
            drawEnemyHealth(batch, enemy);
        }
    }

    private void drawTiles(SpriteBatch batch, OrthographicCamera camera, Level level) {
        float halfW = camera.viewportWidth * camera.zoom / 2f;
        float halfH = camera.viewportHeight * camera.zoom / 2f;
        int x0 = Math.max(0, Level.toTile(camera.position.x - halfW) - 1);
        int x1 = Math.min(level.getWidth() - 1, Level.toTile(camera.position.x + halfW) + 1);
        int y0 = Math.max(0, Level.toTile(camera.position.y - halfH) - 1);
        int y1 = Math.min(level.getHeight() - 1, Level.toTile(camera.position.y + halfH) + 1);

        int size = Level.TILE_SIZE;
        for (int tx = x0; tx <= x1; tx++) {
            for (int ty = y0; ty <= y1; ty++) {
                Tile tile = level.getTile(tx, ty);
                if (tile == Tile.FLOOR) {
                    batch.draw(floor, tx * size, ty * size, size, size);
                } else if (tile == Tile.WALL) {
                    batch.draw(wall, tx * size, ty * size, size, size);
                }
            }
        }
    }

    private void drawPlayer(SpriteBatch batch, Player player) {
        float footX = player.getCenterX();
        float footY = player.getY();
        Direction dir = player.getDirection();

        if (player.isAttacking()) {
            // меч уже нарисован в анимации удара
            sprites.playerAttack.draw(batch, dir, player.getStateTime(), footX, footY, PLAYER_SCALE);
            return;
        }

        float time = player.isMoving() ? player.getStateTime() : 0f;
        if (dir == Direction.UP) drawWeapon(batch, player); // меч за спиной
        sprites.playerWalk.draw(batch, dir, time, footX, footY, PLAYER_SCALE);
        if (dir != Direction.UP) drawWeapon(batch, player);
    }

    private void drawWeapon(SpriteBatch batch, Player player) {
        Item weapon = player.getWeapon();
        if (weapon == null) return;
        Texture icon = assets.texture(weapon.getIconPath());
        boolean left = player.getDirection() == Direction.LEFT;
        float x = left ? player.getCenterX() - 6 - WEAPON_ICON_SIZE : player.getCenterX() + 6;
        float y = player.getY() + 10;
        batch.draw(icon, x, y, WEAPON_ICON_SIZE, WEAPON_ICON_SIZE,
            0, 0, icon.getWidth(), icon.getHeight(), left, false);
    }

    private void drawEnemy(SpriteBatch batch, Enemy enemy) {
        DirectionalAnimation animation = switch (enemy.getState()) {
            case ATTACK -> sprites.slimeAttack;
            case HURT -> sprites.slimeHurt;
            case DYING, DEAD -> sprites.slimeDeath;
            case IDLE, CHASE -> sprites.slimeWalk;
        };
        animation.draw(batch, enemy.getDirection(), enemy.getStateTime(),
            enemy.getCenterX(), enemy.getY(), SLIME_SCALE);
    }

    private void drawEnemyHealth(SpriteBatch batch, Enemy enemy) {
        if (!enemy.isAlive() || enemy.getHealth() >= Enemy.MAX_HEALTH) return;
        float width = 36f;
        float x = enemy.getCenterX() - width / 2f;
        float y = enemy.getY() + 34f;
        float ratio = (float) enemy.getHealth() / Enemy.MAX_HEALTH;
        batch.setColor(HP_BACK);
        batch.draw(pixel, x - 1, y - 1, width + 2, 6);
        batch.setColor(HP_FILL);
        batch.draw(pixel, x, y, width * ratio, 4);
        batch.setColor(Color.WHITE);
    }
}
