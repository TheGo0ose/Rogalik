package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import java.util.ArrayList;
import java.util.List;

public class GameManager {
    private final SpriteBatch batch;
    private Player player;
    private Level level;
    private final List<Enemy> enemies;
    private Renderer renderer;
    private final OrthographicCamera camera;

    // Переменные для меню
    private Stage menuStage;
    private Texture menuTexture;

    // Переменные для инвентаря и статистики
    private Stage inventoryStage;
    private Table inventoryTable;
    private boolean inventoryVisible = false;

    private enum GameState { MENU, PLAYING }
    private GameState gameState;

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

        // Инициализация меню
        gameState = GameState.MENU;
        menuStage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(menuStage);

        Table menuTable = new Table();
        menuTable.setFillParent(true);
        menuStage.addActor(menuTable);

        menuTexture = new Texture("menu_butt.png");
        TextureRegion startRegion = new TextureRegion(menuTexture, 0, 51, 200, 50);
        TextureRegion optionsRegion = new TextureRegion(menuTexture, 0, 131, 200, 50);
        TextureRegion exitRegion = new TextureRegion(menuTexture, 0, 211, 200, 50);

        NinePatch startPatch = new NinePatch(startRegion, 10, 10, 10, 10);
        NinePatch optionsPatch = new NinePatch(optionsRegion, 10, 10, 10, 10);
        NinePatch exitPatch = new NinePatch(exitRegion, 10, 10, 10, 10);

        ImageButton.ImageButtonStyle startStyle = new ImageButton.ImageButtonStyle();
        startStyle.up = new NinePatchDrawable(startPatch);

        ImageButton.ImageButtonStyle optionsStyle = new ImageButton.ImageButtonStyle();
        optionsStyle.up = new NinePatchDrawable(optionsPatch);

        ImageButton.ImageButtonStyle exitStyle = new ImageButton.ImageButtonStyle();
        exitStyle.up = new NinePatchDrawable(exitPatch);

        ImageButton startButton = new ImageButton(startStyle);
        ImageButton optionsButton = new ImageButton(optionsStyle);
        ImageButton exitButton = new ImageButton(exitStyle);

        menuTable.add(startButton).width(200).height(50).pad(10);
        menuTable.row();
        menuTable.add(optionsButton).width(200).height(50).pad(10);
        menuTable.row();
        menuTable.add(exitButton).width(200).height(50).pad(10);

        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameState = GameState.PLAYING;
                Gdx.input.setInputProcessor(null);
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

        // Инициализация интерфейса инвентаря
        inventoryStage = new Stage(new ScreenViewport());
        inventoryTable = new Table();
        inventoryTable.setFillParent(true);
        inventoryStage.addActor(inventoryTable);

        // Стили для интерфейса
        Label.LabelStyle labelStyle = new Label.LabelStyle(new BitmapFont(), Color.WHITE);
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = new BitmapFont();
        buttonStyle.fontColor = Color.WHITE;

        // Добавляем статы персонажа
        inventoryTable.add(new Label("Character Stats", labelStyle)).colspan(2).pad(10);
        inventoryTable.row();
        inventoryTable.add(new Label("Health: " + player.getHealth() + "/" + player.getMaxHealth(), labelStyle)).pad(5);
        inventoryTable.row();
        inventoryTable.add(new Label("Damage: " + player.getDamage(), labelStyle)).pad(5);
        inventoryTable.row();
        inventoryTable.add(new Label("Speed: " + player.getSpeed(), labelStyle)).pad(5);
        inventoryTable.row();

        // Добавляем список предметов
        inventoryTable.add(new Label("Inventory", labelStyle)).colspan(2).pad(10);
        inventoryTable.row();
        for (Item item : player.getInventory().getItems()) {
            inventoryTable.add(new Label(item.getName() + ": " + item.getDescription(), labelStyle)).pad(5);
            TextButton useButton = new TextButton("Use", buttonStyle);
            useButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    useItem(item);
                }
            });
            inventoryTable.add(useButton).pad(5);
            inventoryTable.row();
        }

        // Кнопка закрытия инвентаря
        TextButton closeButton = new TextButton("Close", buttonStyle);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                inventoryVisible = false;
                Gdx.input.setInputProcessor(null);
            }
        });
        inventoryTable.add(closeButton).colspan(2).pad(10);
    }

    private void useItem(Item item) {
        if (item.getName().equals("Potion")) {
            player.setHealth(player.getHealth() + 20);
            player.getInventory().removeItem(item);
            updateInventoryUI();
        } else if (item.getName().equals("Sword")) {
            player.setDamage(player.getDamage() + 5);
            Gdx.app.log("Inventory", "Equipped " + item.getName());
        }
    }

    private void updateInventoryUI() {
        inventoryTable.clear();
        Label.LabelStyle labelStyle = new Label.LabelStyle(new BitmapFont(), Color.WHITE);
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = new BitmapFont();
        buttonStyle.fontColor = Color.WHITE;

        inventoryTable.add(new Label("Character Stats", labelStyle)).colspan(2).pad(10);
        inventoryTable.row();
        inventoryTable.add(new Label("Health: " + player.getHealth() + "/" + player.getMaxHealth(), labelStyle)).pad(5);
        inventoryTable.row();
        inventoryTable.add(new Label("Damage: " + player.getDamage(), labelStyle)).pad(5);
        inventoryTable.row();
        inventoryTable.add(new Label("Speed: " + player.getSpeed(), labelStyle)).pad(5);
        inventoryTable.row();

        inventoryTable.add(new Label("Inventory", labelStyle)).colspan(2).pad(10);
        inventoryTable.row();
        for (Item item : player.getInventory().getItems()) {
            inventoryTable.add(new Label(item.getName() + ": " + item.getDescription(), labelStyle)).pad(5);
            TextButton useButton = new TextButton("Use", buttonStyle);
            useButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    useItem(item);
                }
            });
            inventoryTable.add(useButton).pad(5);
            inventoryTable.row();
        }

        TextButton closeButton = new TextButton("Close", buttonStyle);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                inventoryVisible = false;
                Gdx.input.setInputProcessor(null);
            }
        });
        inventoryTable.add(closeButton).colspan(2).pad(10);
    }

    private void spawnEnemies(int count) {
        for (int i = 0; i < count; i++) {
            enemies.add(new Enemy(level.getLevelMap()));
        }
    }

    public void update() {
        if (gameState == GameState.PLAYING) {
            // Проверка нажатия клавиши для открытия/закрытия инвентаря
            if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
                inventoryVisible = !inventoryVisible;
                if (inventoryVisible) {
                    Gdx.input.setInputProcessor(inventoryStage);
                } else {
                    Gdx.input.setInputProcessor(null);
                }
            }

            if (!inventoryVisible) {
                player.handleInput();
                for (Enemy enemy : enemies) {
                    enemy.update(player.getX(), player.getY());
                }
                camera.position.set(player.getX() + Level.TILE_SIZE / 2, player.getY() + Level.TILE_SIZE / 2, 0);
                camera.update();
                batch.setProjectionMatrix(camera.combined);
            } else {
                inventoryStage.act(Gdx.graphics.getDeltaTime());
            }
        } else {
            menuStage.act(Gdx.graphics.getDeltaTime());
        }
    }

    public void render() {
        if (gameState == GameState.MENU) {
            menuStage.draw();
        } else {
            renderer.render();
            if (inventoryVisible) {
                inventoryStage.draw();
            }
        }
    }

    public void dispose() {
        batch.dispose();
        player.dispose();
        level.dispose();
        for (Enemy enemy : enemies) {
            enemy.dispose();
        }
        menuStage.dispose();
        inventoryStage.dispose();
        menuTexture.dispose();
    }
}
