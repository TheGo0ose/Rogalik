package io.github.some_example_name.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.some_example_name.MainGame;
import io.github.some_example_name.audio.AudioManager;
import io.github.some_example_name.entity.Player;
import io.github.some_example_name.entity.PlayerInput;
import io.github.some_example_name.item.Item;
import io.github.some_example_name.render.WorldRenderer;
import io.github.some_example_name.ui.Hud;
import io.github.some_example_name.ui.InventoryWindow;
import io.github.some_example_name.world.GameWorld;

import java.util.Random;

/** Один забег. Связывает ввод, логику мира, отрисовку и интерфейс. */
public final class GameScreen extends ScreenAdapter {
    /** Ограничение шага: после подвисания персонажи не «телепортируются» сквозь стены. */
    private static final float MAX_DELTA = 1 / 30f;

    private final MainGame game;
    private final SpriteBatch batch;
    private final GameWorld world;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport viewport = new ScreenViewport(camera);
    private final WorldRenderer worldRenderer;
    private final Hud hud;
    private final InventoryWindow inventory;
    private boolean paused;

    public GameScreen(MainGame game) {
        this.game = game;
        this.batch = game.getBatch();
        this.world = new GameWorld(new Random());
        this.worldRenderer = new WorldRenderer(game.getAssets(), game.getSprites());
        this.hud = new Hud(game.getAssets(), game.getUi());
        this.inventory = new InventoryWindow(batch, game.getAssets(), game.getUi(), game.getAudio(),
            world.getPlayer(), game.getSprites().playerPortrait(), hud::showMessage);

        world.setListener(new GameWorld.Listener() {
            @Override
            public void onItemLooted(Item item) {
                hud.showMessage("Получено: " + item.getName());
                inventory.refresh();
            }

            @Override
            public void onInventoryFull(Item item) {
                hud.showMessage("Рюкзак полон, потеряно: " + item.getName());
            }
        });
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(inventory.getStage());
        game.getAudio().play(AudioManager.Track.GAME);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    /** Окно потеряло фокус или свёрнуто — ставим игру на паузу. */
    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, MAX_DELTA);
        if (handleScreenKeys()) return;

        if (!paused && !inventory.isVisible()) {
            world.update(delta, readPlayerInput());
        }
        hud.update(delta);

        Player player = world.getPlayer();
        if (player.isDead()) {
            game.showDeath();
            return;
        }

        camera.position.set(Math.round(player.getCenterX()), Math.round(player.getBodyCenterY()), 0);
        camera.update();

        ScreenUtils.clear(0, 0, 0, 1);
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        worldRenderer.render(batch, camera, world);
        batch.end();

        hud.render(batch, player, paused);
        inventory.render(delta);
    }

    /** @return {@code true}, если экран сменился и кадр рисовать не нужно. */
    private boolean handleScreenKeys() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (inventory.isVisible()) {
                inventory.setVisible(false);
            } else {
                paused = !paused;
            }
        }
        if (paused) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
                game.showMenu();
                return true;
            }
            return false;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            inventory.toggle();
        }
        return false;
    }

    private static PlayerInput readPlayerInput() {
        float x = 0;
        float y = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) x += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) y -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) y += 1;
        boolean attack = Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
        return new PlayerInput(x, y, attack);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        hud.resize(width, height);
        inventory.resize(width, height);
    }

    @Override
    public void dispose() {
        inventory.dispose();
    }
}
