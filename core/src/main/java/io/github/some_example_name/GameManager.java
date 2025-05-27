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
import com.badlogic.gdx.math.Matrix4;
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
import java.util.Iterator;
import java.util.List;

public class GameManager {
    private SpriteBatch batch;
    private Player player;
    private Level level;
    private final List<Enemy> enemies;
    private Renderer renderer;
    private final OrthographicCamera camera;

    private Stage menuStage;
    private Texture menuTexture;
    private Texture soundTexture;
    private Texture buttsTexture;
    private Texture menuBackgroundTexture;

    private Music menuMusic;
    private Music gameMusic;
    private Music deathMusic;
    private Sound buttonClickSound;
    private GameState currentMusicState;

    private Stage inventoryStage;
    private Table inventoryTable;
    private boolean inventoryVisible = false;
    private Texture inventoryBackgroundTexture; // Текстура фона инвентаря

    private Stage settingsStage;
    private Table settingsTable;
    private boolean settingsVisible = false;
    private float volume = 1.0f;
    private boolean soundEnabled = true;
    private ImageButton progressBar;
    private ImageButton.ImageButtonStyle progressBarStyle;

    private float attackCooldown = 0f;
    private final float ATTACK_COOLDOWN_DURATION = 0.5f;

    // Хитбар
    private Texture healthBarFrameTexture;
    private Texture healthBarFillTexture;
    private float frameWidth = 308f;
    private float frameHeight = 96f;
    private float fillWidth = 280f;
    private float fillHeight = 20f;
    private final float HEALTH_BAR_X = 10f;
    private final float HEALTH_BAR_Y_OFFSET = 10f;
    private float fillYOffset = -20f;

    // Константы для расположения слотов инвентаря
    private static final int SLOTS_PER_ROW = 4;
    private static final int SLOTS_PER_COLUMN = 3;
    private static final float SLOT_SIZE = 64f; // Увеличиваем размер слота под новый масштаб
    private static final float INVENTORY_X = 100f; // Сдвигаем ближе к левому краю
    private static final float INVENTORY_Y = 50f; // Сдвигаем ниже
    private static final float INVENTORY_SCALE = 1.5f; // Масштаб инвентаря

    public enum GameState { MENU, PLAYING, SETTINGS, DEATH }
    private GameState gameState;
    private GameState previousState;

    public GameManager(OrthographicCamera camera) {
        this.camera = camera;
        batch = new SpriteBatch();
        enemies = new ArrayList<>();
    }

    public Player getPlayer() {
        if (player == null) {
            Gdx.app.error("GameManager", "Player is null in getPlayer!");
        }
        return player;
    }

    public void initialize() {
        Gdx.app.log("GameManager", "Starting initialize");
        level = new Level();
        level.generateLevel();
        enemies.addAll(level.getEnemies());
        Gdx.app.log("GameManager", "Loaded " + enemies.size() + " enemies from Level");

        try {
            menuTexture = new Texture(Gdx.files.internal("menu_butt.png"));
            Gdx.app.log("GameManager", "menuTexture loaded");
        } catch (Exception e) {
            Gdx.app.error("GameManager", "Failed to load menu_butt.png", e);
            throw e;
        }

        player = new Player(level.getLevelMap(), level);
        player.setStartPosition();

        Item healingPotion = new Item("Healing Potion", "Restores 20 HP", Item.ItemType.POTION, 20, 0, 0, "healing_potion.png");
        player.getInventory().addItem(healingPotion);

        renderer = new Renderer(batch, level, player, enemies);

        try {
            menuMusic = Gdx.audio.newMusic(Gdx.files.internal("menu_music.wav"));
            Gdx.app.log("GameManager", "menuMusic loaded");
        } catch (Exception e) {
            Gdx.app.error("GameManager", "Failed to load menu_music.wav", e);
            throw e;
        }
        try {
            gameMusic = Gdx.audio.newMusic(Gdx.files.internal("game_music.wav"));
            Gdx.app.log("GameManager", "gameMusic loaded");
        } catch (Exception e) {
            Gdx.app.error("GameManager", "Failed to load game_music.wav", e);
            throw e;
        }
        try {
            deathMusic = Gdx.audio.newMusic(Gdx.files.internal("death_music.wav"));
            Gdx.app.log("GameManager", "deathMusic loaded");
        } catch (Exception e) {
            Gdx.app.error("GameManager", "Failed to load death_music.wav", e);
            throw e;
        }
        try {
            buttonClickSound = Gdx.audio.newSound(Gdx.files.internal("button_click.wav"));
            Gdx.app.log("GameManager", "buttonClickSound loaded");
        } catch (Exception e) {
            Gdx.app.error("GameManager", "Failed to load button_click.wav", e);
            throw e;
        }
        menuMusic.setLooping(true);
        gameMusic.setLooping(true);
        deathMusic.setLooping(false);

        gameState = GameState.MENU;
        currentMusicState = null;
        previousState = GameState.MENU;
        menuStage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(menuStage);

        menuBackgroundTexture = new Texture(Gdx.files.internal("menu_butt.png"));
        Image backgroundImage = new Image(menuBackgroundTexture);
        backgroundImage.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        backgroundImage.setPosition(200, 70);
        backgroundImage.setSize(240, 347);
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

        inventoryStage = new Stage(new ScreenViewport());
        inventoryTable = new Table();
        inventoryTable.setFillParent(true);
        inventoryStage.addActor(inventoryTable);

        // Загрузка фона инвентаря
        try {
            inventoryBackgroundTexture = new Texture(Gdx.files.internal("inventory.png"));
            Image inventoryBackground = new Image(inventoryBackgroundTexture);
            inventoryBackground.setPosition(INVENTORY_X, INVENTORY_Y);
            inventoryBackground.setScale(INVENTORY_SCALE); // Увеличиваем размер
            inventoryStage.addActor(inventoryBackground);
        } catch (Exception e) {
            Gdx.app.error("GameManager", "Failed to load inventory.png", e);
            throw e;
        }

        settingsStage = new Stage(new ScreenViewport());
        settingsTable = new Table();
        settingsTable.setFillParent(true);
        settingsStage.addActor(settingsTable);

        Label.LabelStyle labelStyle = new Label.LabelStyle(new BitmapFont(), Color.WHITE);
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = new BitmapFont();
        buttonStyle.fontColor = Color.WHITE;

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
        ImageButton.ImageButtonStyle arrowLeftStyle = new ImageButton.ImageButtonStyle();
        ImageButton.ImageButtonStyle arrowRightStyle = new ImageButton.ImageButtonStyle();
        soundButtonStyle.up = new NinePatchDrawable(soundPatch);
        soundButtonStyle.down = new NinePatchDrawable(soundDownPatch);
        arrowLeftStyle.up = new NinePatchDrawable(arrowLeftPatch);
        arrowLeftStyle.down = new NinePatchDrawable(arrowLeftDownPatch);
        arrowRightStyle.up = new NinePatchDrawable(arrowRightPatch);
        arrowRightStyle.down = new NinePatchDrawable(arrowRightDownPatch);

        progressBarStyle = new ImageButton.ImageButtonStyle();
        progressBarStyle.up = new NinePatchDrawable(progressBarPatch);

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

        progressBar = new ImageButton(progressBarStyle);
        progressBar.setSize(300 * volume, 45);

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

        settingsTable.add(new Label("Settings", labelStyle)).colspan(5).pad(10);
        settingsTable.row();
        settingsTable.add(new Label("Sound:", labelStyle)).pad(5);
        settingsTable.add(soundButton).size(69, 60).pad(5);
        settingsTable.add(decreaseVolumeButton).size(57, 57).pad(5);
        settingsTable.add(progressBar).size(300 * volume, 45).pad(5);
        settingsTable.add(increaseVolumeButton).size(57, 57).pad(5);
        settingsTable.row();

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

        try {
            healthBarFrameTexture = new Texture(Gdx.files.internal("healthbar.png"));
            healthBarFillTexture = new Texture(Gdx.files.internal("health.png"));
            Gdx.app.log("GameManager", "Health bar textures loaded");
        } catch (Exception e) {
            Gdx.app.error("GameManager", "Failed to load health bar textures", e);
            throw e;
        }

        updateInventoryUI();

        Gdx.app.log("GameManager", "Initial soundEnabled: " + soundEnabled + ", volume: " + volume);
        updateMusic();
        Gdx.app.log("GameManager", "Initialize completed");
    }

    private void updateVolume() {
        progressBar.setSize(300 * volume, 45);
        if (soundEnabled) {
            menuMusic.setVolume(volume);
            gameMusic.setVolume(volume);
            deathMusic.setVolume(volume);
            Gdx.app.log("GameManager", "Volume updated to: " + volume);
        } else {
            menuMusic.setVolume(0);
            gameMusic.setVolume(0);
            deathMusic.setVolume(0);
            Gdx.app.log("GameManager", "Sound disabled, volume set to 0");
        }
        updateMusic();
    }

    private void updateMusic() {
        Gdx.app.log("GameManager", "Updating music, gameState: " + gameState + ", soundEnabled: " + soundEnabled);

        if (currentMusicState == gameState && (menuMusic.isPlaying() || gameMusic.isPlaying() || deathMusic.isPlaying())) {
            return;
        }

        menuMusic.stop();
        gameMusic.stop();
        deathMusic.stop();

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
            } else if (gameState == GameState.DEATH) {
                if (!deathMusic.isPlaying()) {
                    Gdx.app.log("GameManager", "Playing deathMusic, volume: " + volume);
                    deathMusic.setVolume(volume);
                    deathMusic.play();
                }
            }
        }

        currentMusicState = gameState;
    }

    private void useItem(Item item) {
        if (item.getType() == Item.ItemType.POTION) {
            player.usePotion(item);
            player.getInventory().removeItem(item);
            Gdx.app.log("GameManager", "Used potion: " + item.getName() + ", removed from inventory");
        } else if (item.getType() == Item.ItemType.WEAPON) {
            player.equipWeapon(item);
            Gdx.app.log("GameManager", "Equipped weapon: " + item.getName());
        } else if (item.getType() == Item.ItemType.ARMOR) {
            player.equipArmor(item);
            Gdx.app.log("GameManager", "Equipped armor: " + item.getName());
        }
        updateInventoryUI(); // Обновляем UI после использования
    }

    private void updateInventoryUI() {
        inventoryTable.clear();
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = new BitmapFont();
        buttonStyle.fontColor = Color.WHITE;

        // Отображение предметов в слотах (4x3)
        List<Item> items = player.getInventory().getItems();
        for (int i = 0; i < SLOTS_PER_COLUMN; i++) {
            for (int j = 0; j < SLOTS_PER_ROW; j++) {
                int index = i * SLOTS_PER_ROW + j;
                if (index < items.size()) {
                    Item item = items.get(index);
                    Texture itemTexture = item.getTexture();
                    TextureRegion itemRegion = new TextureRegion(itemTexture);

                    // Создаём кнопку с текстурой предмета
                    ImageButton.ImageButtonStyle itemButtonStyle = new ImageButton.ImageButtonStyle();
                    itemButtonStyle.up = new NinePatchDrawable(new NinePatch(itemRegion, 5, 5, 5, 5));
                    ImageButton itemButton = new ImageButton(itemButtonStyle);
                    // Учитываем масштаб инвентаря при позиционировании
                    itemButton.setPosition(
                        INVENTORY_X + j * SLOT_SIZE * INVENTORY_SCALE + 10 * INVENTORY_SCALE,
                        INVENTORY_Y + (SLOTS_PER_COLUMN - 1 - i) * SLOT_SIZE * INVENTORY_SCALE + 10 * INVENTORY_SCALE
                    );
                    itemButton.setSize(SLOT_SIZE * INVENTORY_SCALE, SLOT_SIZE * INVENTORY_SCALE); // Увеличиваем размер кнопки
                    int finalJ = j;
                    int finalI = i;
                    itemButton.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            if (soundEnabled) buttonClickSound.play(volume);
                            useItem(item);
                            Gdx.app.log("GameManager", "Clicked item: " + item.getName() + " at slot (" + finalJ + "," + finalI + ")");
                        }
                    });

                    // Добавляем кнопку в таблицу (для управления слоями)
                    inventoryTable.addActor(itemButton);
                }
            }
        }

        // Кнопка "Закрыть"
        TextButton closeButton = new TextButton("Close", buttonStyle);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (soundEnabled) buttonClickSound.play(volume);
                inventoryVisible = false;
                Gdx.input.setInputProcessor(null);
                Gdx.app.log("GameManager", "Inventory closed");
            }
        });
        // Позиционируем кнопку "Close" ниже инвентаря
        closeButton.setPosition(
            INVENTORY_X + (inventoryBackgroundTexture.getWidth() * INVENTORY_SCALE - closeButton.getWidth()) / 2,
            INVENTORY_Y - 50
        );
        inventoryTable.addActor(closeButton);
    }

    public void update() {
        Gdx.app.log("GameManager", "Updating, gameState: " + gameState + ", player HP: " + player.getHealth());
        if (gameState == GameState.PLAYING) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
                inventoryVisible = !inventoryVisible;
                if (inventoryVisible) {
                    updateInventoryUI(); // Обновляем UI при открытии
                    Gdx.input.setInputProcessor(inventoryStage);
                    Gdx.app.log("GameManager", "Inventory opened");
                } else {
                    Gdx.input.setInputProcessor(null);
                    Gdx.app.log("GameManager", "Inventory closed");
                }
            }

            if (attackCooldown > 0) {
                attackCooldown -= Gdx.graphics.getDeltaTime();
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                if (inventoryVisible) {
                    Gdx.app.log("GameManager", "SPACE pressed but ignored (inventoryVisible=true)");
                } else if (attackCooldown > 0) {
                    Gdx.app.log("GameManager", "SPACE pressed but ignored (attackCooldown=" + attackCooldown + ")");
                } else {
                    Gdx.app.log("GameManager", "Player attack triggered at (" + player.getX() + "," + player.getY() + ")");
                    player.triggerAttack();
                    for (Enemy enemy : enemies) {
                        if (enemy.isDead()) continue;
                        boolean collision = enemy.checkCollision(player.getX(), player.getY());
                        if (collision) {
                            int playerDamage = player.getDamage();
                            enemy.takeDamage(playerDamage);
                            attackCooldown = ATTACK_COOLDOWN_DURATION;
                            Gdx.app.log("GameManager", "Player hit enemy at (" + enemy.getX() + "," + enemy.getY() + "). " +
                                "Dealt=" + playerDamage + ", Enemy HP=" + enemy.getHealth());
                            break;
                        } else {
                            Gdx.app.log("GameManager", "No collision with enemy at (" + enemy.getX() + "," + enemy.getY() + ")");
                        }
                    }
                }
            }

            if (!inventoryVisible) {
                player.handleInput();
                float delta = Gdx.graphics.getDeltaTime();
                Iterator<Enemy> enemyIterator = enemies.iterator();
                while (enemyIterator.hasNext()) {
                    Enemy enemy = enemyIterator.next();
                    enemy.update(player.getX(), player.getY(), player, delta);
                    if (enemy.isDead() && enemy.getHealth() <= 0) {
                        List<Item> droppedItems = enemy.dropItems();
                        for (Item item : droppedItems) {
                            if (player.getInventory().addItem(item)) {
                                Gdx.app.log("GameManager", "Dropped item " + item.getName() + " added to inventory");
                            } else {
                                Gdx.app.log("GameManager", "Inventory full, cannot add dropped item " + item.getName());
                            }
                        }
                        enemyIterator.remove();
                        enemy.dispose();
                        Gdx.app.log("GameManager", "Enemy removed after death");
                    }
                }

                if (player.isDead()) {
                    Gdx.app.log("GameManager", "Player is dead, calling setGameState(DEATH)");
                    setGameState(GameState.DEATH);
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
        } else if (gameState == GameState.DEATH) {
            Gdx.app.log("GameManager", "In DEATH state, waiting for restart or exit");
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                Gdx.app.log("GameManager", "Restarting game from DEATH state");
                restartGame();
            } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                Gdx.app.log("GameManager", "Exiting from DEATH state");
                Gdx.app.exit();
            }
        }

        if (currentMusicState != gameState) {
            updateMusic();
        }
    }

    private void restartGame() {
        Gdx.app.log("GameManager", "Restarting game...");
        try {
            if (player != null) {
                player.setHealth(player.getMaxHealth());
                player.setStartPosition();
                Gdx.app.log("GameManager", "Player reset: HP=" + player.getHealth() + ", Pos=(" + player.getX() + "," + player.getY() + ")");
            } else {
                Gdx.app.error("GameManager", "Player is null during restart!");
                return;
            }

            for (Enemy enemy : enemies) {
                if (enemy != null) {
                    enemy.dispose();
                }
            }
            enemies.clear();
            Gdx.app.log("GameManager", "Enemies cleared");

            if (level != null) {
                level.dispose();
            }
            level = new Level();
            level.generateLevel();
            enemies.addAll(level.getEnemies());
            Gdx.app.log("GameManager", "New level generated, enemies: " + enemies.size());

            if (batch != null) {
                renderer = new Renderer(batch, level, player, enemies);
                Gdx.app.log("GameManager", "Renderer updated");
            } else {
                Gdx.app.error("GameManager", "SpriteBatch is null during restart!");
                return;
            }

            inventoryVisible = false;
            Gdx.input.setInputProcessor(null);

            setGameState(GameState.PLAYING);
            Gdx.app.log("GameManager", "Game restarted, state set to PLAYING");
        } catch (Exception e) {
            Gdx.app.error("GameManager", "Error during restartGame", e);
        }
    }

    public void setGameState(GameState state) {
        Gdx.app.log("GameManager", "Attempting to set gameState to: " + state + ", current: " + gameState);
        if (gameState != GameState.DEATH && state == GameState.DEATH) {
            previousState = gameState;
            Gdx.app.log("GameManager", "Saved previousState: " + previousState);
        } else if (gameState == GameState.DEATH && state != GameState.DEATH) {
            Gdx.app.log("GameManager", "Exiting DEATH state to: " + state);
            gameState = state;
            updateMusic();
            return;
        }
        gameState = state;
        Gdx.app.log("GameManager", "gameState set to: " + gameState);
        updateMusic();
    }

    public GameState getGameState() {
        return gameState;
    }

    public void render() {
        if (gameState == GameState.MENU) {
            menuStage.draw();
        } else if (gameState == GameState.SETTINGS) {
            settingsStage.draw();
        } else if (gameState == GameState.DEATH) {
            batch.begin();
            BitmapFont font = new BitmapFont();
            font.setColor(Color.RED);
            font.getData().setScale(2);
            font.draw(batch, "Game Over! Press R to Restart, ESC to Exit",
                Gdx.graphics.getWidth() / 2 - 200, Gdx.graphics.getHeight() / 2);
            font.dispose();
            batch.end();
        } else {
            renderer.render();
            if (inventoryVisible) {
                inventoryStage.draw();
            }
            if (gameState == GameState.PLAYING && player != null) {
                batch.begin();
                batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
                float healthBarY = Gdx.graphics.getHeight() - frameHeight - HEALTH_BAR_Y_OFFSET;
                float healthRatio = (float) player.getHealth() / player.getMaxHealth();
                float scaledFillWidth = fillWidth * healthRatio;
                float fillX = HEALTH_BAR_X + (frameWidth - fillWidth) / 2;
                float baseFillY = healthBarY + (frameHeight - fillHeight) / 2;
                float effectiveFillYOffset = fillYOffset;
                float fillY = baseFillY + effectiveFillYOffset;
                batch.draw(healthBarFillTexture, fillX, fillY, scaledFillWidth, fillHeight);
                batch.draw(healthBarFrameTexture, HEALTH_BAR_X, healthBarY, frameWidth, frameHeight);
                BitmapFont font = new BitmapFont();
                font.setColor(Color.WHITE);
                font.getData().setScale(1);
                font.draw(batch, "Frame Y: " + healthBarY + ", Fill Y: " + fillY + ", fillOffset: " + effectiveFillYOffset,
                    HEALTH_BAR_X, healthBarY + frameHeight + 40);
                font.draw(batch, "Frame: " + frameWidth + "x" + frameHeight + ", Fill: " + scaledFillWidth + "x" + fillHeight,
                    HEALTH_BAR_X, healthBarY + frameHeight + 20);
                font.dispose();
                batch.end();
                Gdx.app.log("GameManager", "Rendering health bar: frameY=" + healthBarY + ", fillY=" + fillY + ", effectiveFillOffset=" + effectiveFillYOffset);
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
        healthBarFrameTexture.dispose();
        healthBarFillTexture.dispose();
        if (inventoryBackgroundTexture != null) inventoryBackgroundTexture.dispose();
        if (menuMusic != null) menuMusic.dispose();
        if (gameMusic != null) gameMusic.dispose();
        if (deathMusic != null) deathMusic.dispose();
        if (buttonClickSound != null) buttonClickSound.dispose();
    }
}
