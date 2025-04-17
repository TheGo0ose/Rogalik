package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
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
    private SpriteBatch batch;
    private Player player;
    private Level level;
    private final List<Enemy> enemies;
    private Renderer renderer;
    private final OrthographicCamera camera;

    // Переменные для меню
    private Stage menuStage;
    private Texture menuTexture;
    private Texture soundTexture;
    private Texture buttsTexture;
    private Texture menuBackgroundTexture;

    // Переменные для аудио
    private Music menuMusic;
    private Music gameMusic;
    private Sound buttonClickSound;
    private GameState currentMusicState; // Отслеживание текущего состояния музыки

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
        Gdx.app.log("GameManager", "Starting initialize");
        level = new Level();
        level.generateLevel();
        try {
            menuTexture = new Texture(Gdx.files.internal("menu_butt.png"));
            Gdx.app.log("GameManager", "menuTexture loaded");
        } catch (Exception e) {
            Gdx.app.error("GameManager", "Failed to load menu_butt.png", e);
            throw e;
        }

        player = new Player(level.getLevelMap());
        player.setStartPosition();

        spawnEnemies(3);

        renderer = new Renderer(batch, level, player, enemies);

        // Инициализация аудио
        try {
            menuMusic = Gdx.audio.newMusic(Gdx.files.internal("menu_music.mp3"));
            Gdx.app.log("GameManager", "menuMusic loaded");
        } catch (Exception e) {
            Gdx.app.error("GameManager", "Failed to load menu_music.mp3", e);
            throw e;
        }
        try {
            gameMusic = Gdx.audio.newMusic(Gdx.files.internal("game_music.mp3"));
            Gdx.app.log("GameManager", "gameMusic loaded");
        } catch (Exception e) {
            Gdx.app.error("GameManager", "Failed to load game_music.mp3", e);
            throw e;
        }
        try {
            buttonClickSound = Gdx.audio.newSound(Gdx.files.internal("button_click.mp3"));
            Gdx.app.log("GameManager", "buttonClickSound loaded");
        } catch (Exception e) {
            Gdx.app.error("GameManager", "Failed to load button_click.mp3", e);
            throw e;
        }
        menuMusic.setLooping(true);
        gameMusic.setLooping(true);

        // Инициализация главного меню
        gameState = GameState.MENU;
        currentMusicState = null; // Инициализируем как null, чтобы запустить музыку
        menuStage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(menuStage);

        // Добавляем фоновое изображение
        menuBackgroundTexture = new Texture(Gdx.files.internal("menu_butt.png"));
        Image backgroundImage = new Image(menuBackgroundTexture);
        backgroundImage.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        backgroundImage.setPosition(200, 70);
        backgroundImage.setSize(240, 347); // Пример
        menuStage.addActor(backgroundImage);

        Table menuTable = new Table();
        menuTable.setFillParent(true);
        menuStage.addActor(menuTable);

        try {
            buttsTexture = new Texture("butts.png");
            Gdx.app.log("GameManager", "buttsTexture loaded");
        } catch (Exception e) {
            Gdx.app.error("GameManager", "Failed to load butts.png", e);
            throw e;
        }
        TextureRegion startRegion = new TextureRegion(buttsTexture, 0, 0, 149, 70);
        TextureRegion startDownRegion = new TextureRegion(buttsTexture, 172, 0, 149, 70);
        TextureRegion optionsRegion = new TextureRegion(buttsTexture, 0, 78, 149, 70);
        TextureRegion optionsDownRegion = new TextureRegion(buttsTexture, 172, 79, 149, 70);
        TextureRegion exitRegion = new TextureRegion(buttsTexture, 0, 159, 149, 70);
        TextureRegion exitDownRegion = new TextureRegion(buttsTexture, 172, 158, 149, 70);

        NinePatch startPatch = new NinePatch(startRegion, 10, 10, 10, 10);
        NinePatch optionsPatch = new NinePatch(optionsRegion, 10, 10, 10, 10);
        NinePatch exitPatch = new NinePatch(exitRegion, 10, 10, 10, 10);
        NinePatch startDownPatch = new NinePatch(startDownRegion, 10, 10, 10, 10);
        NinePatch optionsDownPatch = new NinePatch(optionsDownRegion, 10, 10, 10, 10);
        NinePatch exitDownPatch = new NinePatch(exitDownRegion, 10, 10, 10, 10);

        ImageButton.ImageButtonStyle startStyle = new ImageButton.ImageButtonStyle();
        startStyle.up = new NinePatchDrawable(startPatch);
        startStyle.down = new NinePatchDrawable(startDownPatch);

        ImageButton.ImageButtonStyle optionsStyle = new ImageButton.ImageButtonStyle();
        optionsStyle.up = new NinePatchDrawable(optionsPatch);
        optionsStyle.down = new NinePatchDrawable(optionsDownPatch);

        ImageButton.ImageButtonStyle exitStyle = new ImageButton.ImageButtonStyle();
        exitStyle.up = new NinePatchDrawable(exitPatch);
        exitStyle.down = new NinePatchDrawable(exitDownPatch);

        ImageButton startButton = new ImageButton(startStyle);
        ImageButton optionsButton = new ImageButton(optionsStyle);
        ImageButton exitButton = new ImageButton(exitStyle);

        menuTable.add(startButton).width(149).height(70).pad(10);
        menuTable.row();
        menuTable.add(optionsButton).width(149).height(70).pad(10);
        menuTable.row();
        menuTable.add(exitButton).width(149).height(70).pad(10);

        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    if (soundEnabled) buttonClickSound.play(volume);
                    gameState = GameState.PLAYING;
                    Gdx.input.setInputProcessor(null);
                    Gdx.app.log("GameManager", "Start button clicked, gameState: " + gameState);
                } catch (Exception e) {
                    Gdx.app.error("GameManager", "Error in startButton click", e);
                }
            }
        });

        optionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    if (soundEnabled) buttonClickSound.play(volume);
                    gameState = GameState.SETTINGS;
                    settingsVisible = true;
                    Gdx.input.setInputProcessor(settingsStage);
                    Gdx.app.log("GameManager", "Options button clicked, gameState: " + gameState);
                } catch (Exception e) {
                    Gdx.app.error("GameManager", "Error in optionsButton click", e);
                }
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    if (soundEnabled) buttonClickSound.play(volume);
                    Gdx.app.log("GameManager", "Exit button clicked");
                    Gdx.app.exit();
                } catch (Exception e) {
                    Gdx.app.error("GameManager", "Error in exitButton click", e);
                }
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
        try {
            soundTexture = new Texture(Gdx.files.internal("soundbuttons.png"));
            Gdx.app.log("GameManager", "soundTexture loaded");
        } catch (Exception e) {
            Gdx.app.error("GameManager", "Failed to load soundbuttons.png", e);
            throw e;
        }
        TextureRegion soundOnRegion = new TextureRegion(soundTexture, 0, 0, 23, 20);
        TextureRegion soundOffRegion = new TextureRegion(soundTexture, 23, 0, 23, 20);
        TextureRegion soundDownRegion = new TextureRegion(soundTexture, 46, 0, 23, 20);
        TextureRegion arrowLeftRegion = new TextureRegion(soundTexture, 0, 26, 19, 19);
        TextureRegion arrowRightRegion = new TextureRegion(soundTexture, 76, 26, 19, 19);
        TextureRegion arrowLeftDownRegion = new TextureRegion(soundTexture, 19, 26, 19, 19);
        TextureRegion arrowRightDownRegion = new TextureRegion(soundTexture, 95, 26, 19, 19);
        TextureRegion progressBarRegion = new TextureRegion(soundTexture, 24, 26, 47, 15);

        NinePatch soundPatch = new NinePatch(soundEnabled ? soundOnRegion : soundOffRegion, 5, 5, 5, 5);
        NinePatch soundDownPatch = new NinePatch(soundDownRegion, 5, 5, 5, 5);
        NinePatch arrowLeftPatch = new NinePatch(arrowLeftRegion, 5, 5, 5, 5);
        NinePatch arrowRightPatch = new NinePatch(arrowRightRegion, 5, 5, 5, 5);
        NinePatch arrowLeftDownPatch = new NinePatch(arrowLeftDownRegion, 5, 5, 5, 5);
        NinePatch arrowRightDownPatch = new NinePatch(arrowRightDownRegion, 5, 5, 5, 5);
        NinePatch progressBarPatch = new NinePatch(progressBarRegion, 5, 5, 5, 5);

        ImageButton.ImageButtonStyle soundButtonStyle = new ImageButton.ImageButtonStyle();
        soundButtonStyle.up = new NinePatchDrawable(soundPatch);
        soundButtonStyle.down = new NinePatchDrawable(soundDownPatch);

        ImageButton.ImageButtonStyle arrowLeftStyle = new ImageButton.ImageButtonStyle();
        arrowLeftStyle.up = new NinePatchDrawable(arrowLeftPatch);
        arrowLeftStyle.down = new NinePatchDrawable(arrowLeftDownPatch);

        ImageButton.ImageButtonStyle arrowRightStyle = new ImageButton.ImageButtonStyle();
        arrowRightStyle.up = new NinePatchDrawable(arrowRightPatch);
        arrowRightStyle.down = new NinePatchDrawable(arrowRightDownPatch);

        // Сохраняем стиль прогресс-бара
        progressBarStyle = new ImageButton.ImageButtonStyle();
        progressBarStyle.up = new NinePatchDrawable(progressBarPatch);

        // Кнопка включения/выключения звука
        ImageButton soundButton = new ImageButton(soundButtonStyle);
        soundButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    if (soundEnabled) buttonClickSound.play(volume);
                    soundEnabled = !soundEnabled;
                    soundButtonStyle.up = new NinePatchDrawable(new NinePatch(soundEnabled ? soundOnRegion : soundOffRegion, 5, 5, 5, 5));
                    soundButtonStyle.down = new NinePatchDrawable(soundDownPatch);
                    soundButton.setStyle(soundButtonStyle);
                    updateVolume();
                    Gdx.app.log("GameManager", "Sound button clicked, soundEnabled: " + soundEnabled);
                } catch (Exception e) {
                    Gdx.app.error("GameManager", "Error in soundButton click", e);
                }
            }
        });

        // Кнопка уменьшения громкости
        ImageButton decreaseVolumeButton = new ImageButton(arrowLeftStyle);
        decreaseVolumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    if (soundEnabled) buttonClickSound.play(volume);
                    volume = Math.max(0.0f, volume - 0.1f);
                    updateVolume();
                    Gdx.app.log("GameManager", "Decrease volume button clicked, volume: " + volume);
                } catch (Exception e) {
                    Gdx.app.error("GameManager", "Error in decreaseVolumeButton click", e);
                }
            }
        });

        // Прогресс-бар громкости (сохраняем как поле)
        progressBar = new ImageButton(progressBarStyle);
        progressBar.setSize(300 * volume, 45);

        // Кнопка увеличения громкости
        ImageButton increaseVolumeButton = new ImageButton(arrowRightStyle);
        increaseVolumeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    if (soundEnabled) buttonClickSound.play(volume);
                    volume = Math.min(1.0f, volume + 0.1f);
                    updateVolume();
                    Gdx.app.log("GameManager", "Increase volume button clicked, volume: " + volume);
                } catch (Exception e) {
                    Gdx.app.error("GameManager", "Error in increaseVolumeButton click", e);
                }
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
                try {
                    if (soundEnabled) buttonClickSound.play(volume);
                    settingsVisible = false;
                    gameState = GameState.MENU;
                    Gdx.input.setInputProcessor(menuStage);
                    Gdx.app.log("GameManager", "Back button clicked, gameState: " + gameState);
                } catch (Exception e) {
                    Gdx.app.error("GameManager", "Error in backButton click", e);
                }
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
                    try {
                        if (soundEnabled) buttonClickSound.play(volume);
                        useItem(item);
                        Gdx.app.log("GameManager", "Use button clicked for item: " + item.getName());
                    } catch (Exception e) {
                        Gdx.app.error("GameManager", "Error in useButton click", e);
                    }
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
                try {
                    if (soundEnabled) buttonClickSound.play(volume);
                    inventoryVisible = false;
                    Gdx.input.setInputProcessor(null);
                    Gdx.app.log("GameManager", "Close button clicked");
                } catch (Exception e) {
                    Gdx.app.error("GameManager", "Error in closeButton click", e);
                }
            }
        });
        inventoryTable.add(closeButton).colspan(2).pad(10);

        // Начать воспроизведение музыки для начального состояния
        Gdx.app.log("GameManager", "Initial soundEnabled: " + soundEnabled + ", volume: " + volume);
        updateMusic();
        Gdx.app.log("GameManager", "Initialize completed");
    }

    private void updateVolume() {
        progressBar.setSize(300 * volume, 45);
        if (soundEnabled) {
            menuMusic.setVolume(volume);
            gameMusic.setVolume(volume);
            Gdx.app.log("GameManager", "Volume updated to: " + volume);
        } else {
            menuMusic.setVolume(0);
            gameMusic.setVolume(0);
            Gdx.app.log("GameManager", "Sound disabled, volume set to 0");
        }
        updateMusic(); // Обновляем музыку при изменении громкости
    }

    private void updateMusic() {
        Gdx.app.log("GameManager", "Updating music, gameState: " + gameState + ", soundEnabled: " + soundEnabled);

        // Проверяем, нужно ли менять музыку
        if (currentMusicState == gameState && menuMusic.isPlaying() || gameMusic.isPlaying()) {
            return; // Музыка уже играет для текущего состояния
        }

        // Останавливаем текущую музыку
        menuMusic.stop();
        gameMusic.stop();

        // Воспроизводим новую музыку
        if (soundEnabled) {
            if (gameState == GameState.MENU || gameState == GameState.SETTINGS) {
                if (!menuMusic.isPlaying()) {
                    Gdx.app.log("GameManager", "Playing menuMusic, volume: " + volume);
                    menuMusic.setVolume(volume);
                    menuMusic.play();
                }
            } else if (gameState == GameState.PLAYING) {
                if (!gameMusic.isPlaying()) {
                    Gdx.app.log("GameManager", "Playing gameMusic, volume: " + volume);
                    gameMusic.setVolume(volume);
                    gameMusic.play();
                }
            }
        }

        currentMusicState = gameState; // Обновляем текущее состояние музыки
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
                    try {
                        if (soundEnabled) buttonClickSound.play(volume);
                        useItem(item);
                        Gdx.app.log("GameManager", "Use button clicked for item: " + item.getName());
                    } catch (Exception e) {
                        Gdx.app.error("GameManager", "Error in useButton click", e);
                    }
                }
            });
            inventoryTable.add(useButton).pad(5);
            inventoryTable.row();
        }

        TextButton closeButton = new TextButton("Close", buttonStyle);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                try {
                    if (soundEnabled) buttonClickSound.play(volume);
                    inventoryVisible = false;
                    Gdx.input.setInputProcessor(null);
                    Gdx.app.log("GameManager", "Close button clicked");
                } catch (Exception e) {
                    Gdx.app.error("GameManager", "Error in closeButton click", e);
                }
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

        // Вызываем updateMusic только при необходимости
        if (currentMusicState != gameState) {
            updateMusic();
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
        buttsTexture.dispose();
        menuBackgroundTexture.dispose();
        if (menuMusic != null) menuMusic.dispose();
        if (gameMusic != null) gameMusic.dispose();
        if (buttonClickSound != null) buttonClickSound.dispose();
    }
}
