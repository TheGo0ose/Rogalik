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
    private Texture soundTexture;

    // Переменные для инвентаря и статистики
    private Stage inventoryStage;
    private Table inventoryTable;
    private boolean inventoryVisible = false;

    // Переменные для настроек
    private Stage settingsStage;
    private Table settingsTable;
    private boolean settingsVisible = false;
    private float volume = 1.0f;
    private boolean soundEnabled = true;
    private ImageButton progressBar;
    private ImageButton.ImageButtonStyle progressBarStyle;

    private enum GameState { MENU, PLAYING, SETTINGS }
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

        // Инициализация главного меню
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
                gameState = GameState.SETTINGS;
                settingsVisible = true;
                Gdx.input.setInputProcessor(settingsStage);
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

        // Инициализация интерфейса настроек
        settingsStage = new Stage(new ScreenViewport());
        settingsTable = new Table();
        settingsTable.setFillParent(true);
        settingsStage.addActor(settingsTable);

        // Стили для интерфейса
        Label.LabelStyle labelStyle = new Label.LabelStyle(new BitmapFont(), Color.WHITE);
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = new BitmapFont();
        buttonStyle.fontColor = Color.WHITE;

        // Текстуры для элементов управления звуком
        soundTexture = new Texture(Gdx.files.internal("soundbuttons.png"));
        TextureRegion soundOnRegion = new TextureRegion(soundTexture, 0, 0, 23, 20);
        TextureRegion soundOffRegion = new TextureRegion(soundTexture, 23, 0, 23, 20);
        TextureRegion arrowLeftRegion = new TextureRegion(soundTexture, 0, 26, 19, 19);
        TextureRegion arrowRightRegion = new TextureRegion(soundTexture, 76, 26, 19, 19);
        TextureRegion progressBarRegion = new TextureRegion(soundTexture, 24, 26, 47, 15);

        NinePatch soundPatch = new NinePatch(soundEnabled ? soundOnRegion : soundOffRegion, 5, 5, 5, 5);
        NinePatch arrowLeftPatch = new NinePatch(arrowLeftRegion, 5, 5, 5, 5);
        NinePatch arrowRightPatch = new NinePatch(arrowRightRegion, 5, 5, 5, 5);
        NinePatch progressBarPatch = new NinePatch(progressBarRegion, 5, 5, 5, 5);

        ImageButton.ImageButtonStyle soundButtonStyle = new ImageButton.ImageButtonStyle();
        soundButtonStyle.up = new NinePatchDrawable(soundPatch);

        ImageButton.ImageButtonStyle arrowLeftStyle = new ImageButton.ImageButtonStyle();
        arrowLeftStyle.up = new NinePatchDrawable(arrowLeftPatch);

        ImageButton.ImageButtonStyle arrowRightStyle = new ImageButton.ImageButtonStyle();
        arrowRightStyle.up = new NinePatchDrawable(arrowRightPatch);

        // Сохраняем стиль прогресс-бара
        progressBarStyle = new ImageButton.ImageButtonStyle();
        progressBarStyle.up = new NinePatchDrawable(progressBarPatch);

        // Кнопка включения/выключения звука
        ImageButton soundButton = new ImageButton(soundButtonStyle);
        soundButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                soundEnabled = !soundEnabled;
                soundButtonStyle.up = new NinePatchDrawable(new NinePatch(soundEnabled ? soundOnRegion : soundOffRegion, 5, 5, 5, 5));
                soundButton.setStyle(soundButtonStyle);
                updateVolume();
            }
        });

        // Кнопка уменьшения громкости
        ImageButton decreaseVolumeButton = new ImageButton(arrowLeftStyle);
        decreaseVolumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                volume = Math.max(0.0f, volume - 0.1f);
                updateVolume();
            }
        });

        // Прогресс-бар громкости (сохраняем как поле)
        progressBar = new ImageButton(progressBarStyle);
        progressBar.setSize(300 * volume, 45); // Начальный размер (увеличен)

        // Кнопка увеличения громкости
        ImageButton increaseVolumeButton = new ImageButton(arrowRightStyle);
        increaseVolumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                volume = Math.min(1.0f, volume + 0.1f);
                updateVolume();
            }
        });

        // Добавление элементов в таблицу настроек
        settingsTable.add(new Label("Settings", labelStyle)).colspan(5).pad(10);
        settingsTable.row();
        settingsTable.add(new Label("Sound:", labelStyle)).pad(5);
        settingsTable.add(soundButton).size(69, 60).pad(5);
        settingsTable.add(decreaseVolumeButton).size(57, 57).pad(5);
        settingsTable.add(progressBar).size(300 * volume, 45).pad(5);
        settingsTable.add(increaseVolumeButton).size(57, 57).pad(5);
        settingsTable.row();

        // Кнопка возврата в главное меню
        TextButton backButton = new TextButton("Back", buttonStyle);
        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settingsVisible = false;
                gameState = GameState.MENU;
                Gdx.input.setInputProcessor(menuStage);
            }
        });
        settingsTable.add(backButton).colspan(5).pad(10);

        // Добавляем статы персонажа в инвентарь
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

    private void updateVolume() {
        // Обновляем только размер прогресс-бара
        progressBar.setSize(300 * volume, 45); // Масштабируем только ширину

        // Здесь можно добавить код для изменения громкости звуков
        // Например: sound.setVolume(soundId, soundEnabled ? volume : 0);
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
        } else if (gameState == GameState.MENU) {
            menuStage.act(Gdx.graphics.getDeltaTime());
        } else if (gameState == GameState.SETTINGS) {
            settingsStage.act(Gdx.graphics.getDeltaTime());
        }
    }

    public void render() {
        if (gameState == GameState.MENU) {
            menuStage.draw();
        } else if (gameState == GameState.SETTINGS) {
            settingsStage.draw();
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
        settingsStage.dispose();
        menuTexture.dispose();
        soundTexture.dispose();
    }
}
