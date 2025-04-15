package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.ArrayList;
import java.util.List;

public class GameManager {
    private SpriteBatch batch;
    private Player player;
    private Level level;
    private List<Enemy> enemies;
    private Renderer renderer;
    private OrthographicCamera camera;

    // Переменные для меню
    private Stage stage;
    private Table table;
    private Texture menuTexture;
    private enum GameState { MENU, PLAYING }
    private GameState gameState;

    public GameManager(OrthographicCamera camera) {
        this.camera = camera;
        batch = new SpriteBatch();
        enemies = new ArrayList<>();
    }

    public void initialize() {
        // Инициализация игровых объектов
        level = new Level();
        level.generateLevel();

        player = new Player(level.getLevelMap());
        player.setStartPosition();

        spawnEnemies(5);

        renderer = new Renderer(batch, level, player, enemies);

        // Инициализация меню
        gameState = GameState.MENU;
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Загрузка текстуры меню
        menuTexture = new Texture("menu_butt.png");

        // Извлечение регионов кнопок из текстуры (предполагается вертикальное расположение)
        TextureRegion startRegion = new TextureRegion(menuTexture, 0, 47, 200, 50); // "Start"
        TextureRegion optionsRegion = new TextureRegion(menuTexture, 0, 131, 200, 50); // "Options"
        TextureRegion exitRegion = new TextureRegion(menuTexture, 0, 211, 200, 50);    // "Exit"

        // Создание NinePatch для корректного масштабирования кнопок
        NinePatch startPatch = new NinePatch(startRegion, 0, 0, 0, 0);
        NinePatch optionsPatch = new NinePatch(optionsRegion, 0, 0, 0, 0);
        NinePatch exitPatch = new NinePatch(exitRegion, 0, 0, 0, 0);

        // Определение стилей кнопок
        ImageButton.ImageButtonStyle startStyle = new ImageButton.ImageButtonStyle();
        startStyle.up = new NinePatchDrawable(startPatch);

        ImageButton.ImageButtonStyle optionsStyle = new ImageButton.ImageButtonStyle();
        optionsStyle.up = new NinePatchDrawable(optionsPatch);

        ImageButton.ImageButtonStyle exitStyle = new ImageButton.ImageButtonStyle();
        exitStyle.up = new NinePatchDrawable(exitPatch);

        // Создание кнопок
        ImageButton startButton = new ImageButton(startStyle);
        ImageButton optionsButton = new ImageButton(optionsStyle);
        ImageButton exitButton = new ImageButton(exitStyle);

        // Размещение кнопок в таблице
        table.add(startButton).width(200).height(50).pad(10);
        table.row();
        table.add(optionsButton).width(200).height(50).pad(10);
        table.row();
        table.add(exitButton).width(200).height(50).pad(10);

        // Обработчики нажатий на кнопки
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameState = GameState.PLAYING;
                Gdx.input.setInputProcessor(null); // Отключаем ввод для меню
            }
        });

        optionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("Menu", "Options clicked (placeholder)");
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });
    }

    private void spawnEnemies(int count) {
        for (int i = 0; i < count; i++) {
            enemies.add(new Enemy(level.getLevelMap()));
        }
    }

    public void update() {
        if (gameState == GameState.PLAYING) {
            player.handleInput();
            for (Enemy enemy : enemies) {
                enemy.update(player.getX(), player.getY());
            }
            camera.position.set(player.getX() + Level.TILE_SIZE / 2, player.getY() + Level.TILE_SIZE / 2, 0);
            camera.update();
            batch.setProjectionMatrix(camera.combined);
        } else {
            stage.act(Gdx.graphics.getDeltaTime());
        }
    }

    public void render() {
        if (gameState == GameState.MENU) {
            stage.draw();
        } else {
            renderer.render();
        }
    }

    public void dispose() {
        batch.dispose();
        player.dispose();
        level.dispose();
        for (Enemy enemy : enemies) {
            enemy.dispose();
        }
        stage.dispose();
        menuTexture.dispose();
    }
}
