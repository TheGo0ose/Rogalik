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

    private Stage settingsStage;
    private Table settingsTable;
    private boolean settingsVisible = false;
    private float volume = 1.0f;
    private boolean soundEnabled = true;
    private ImageButton progressBar;
    private ImageButton.ImageButtonStyle progressBarStyle;

    private float attackCooldown = 0f;
    private final float ATTACK_COOLDOWN_DURATION = 0.5f;

    public enum GameState { MENU, PLAYING, SETTINGS, DEATH }
    private GameState gameState;
    private GameState previousState;

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

        player = new Player(level.getLevelMap(), level);
        player.setStartPosition();

        Item healingPotion = new Item("Healing Potion", "Restores 20 HP", Item.ItemType.POTION, 20, 0, 0, "healing_potion.png");
        player.getInventory().addItem(healingPotion);

        spawnEnemies(3);

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
        } else if (item.getType() == Item.ItemType.WEAPON) {
            player.equipWeapon(item);
        } else if (item.getType() == Item.ItemType.ARMOR) {
            player.equipArmor(item);
        }
        updateInventoryUI();
    }

    private void updateInventoryUI() {
        inventoryTable.clear();
        Label.LabelStyle labelStyle = new Label.LabelStyle(new BitmapFont(), Color.WHITE);
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = new BitmapFont();
        buttonStyle.fontColor = Color.WHITE;

        inventoryTable.add(new Label("Character Stats", labelStyle)).colspan(3).pad(10);
        inventoryTable.row();
        inventoryTable.add(new Label("Health: " + player.getHealth() + "/" + player.getMaxHealth(), labelStyle)).pad(5);
        inventoryTable.row();
        inventoryTable.add(new Label("Damage: " + player.getDamage(), labelStyle)).pad(5);
        inventoryTable.row();
        inventoryTable.add(new Label("Defense: " + player.getDefense(), labelStyle)).pad(5);
        inventoryTable.row();
        inventoryTable.add(new Label("Speed: " + player.getSpeed(), labelStyle)).pad(5);
        inventoryTable.row();

        inventoryTable.add(new Label("Inventory (" + player.getInventory().getItems().size() + "/10)", labelStyle)).colspan(3).pad(10);
        inventoryTable.row();
        for (Item item : player.getInventory().getItems()) {
            TextureRegion itemRegion = new TextureRegion(item.getTexture());
            inventoryTable.add(new Image(itemRegion)).size(48, 48).pad(5);

            String itemInfo = item.getName() + " (" + item.getType() + "): " + item.getDescription();
            if (item.getType() == Item.ItemType.WEAPON) {
                itemInfo += " | Damage: +" + item.getDamageBonus();
                if (item.getSpeedMultiplier() > 0) {
                    itemInfo += " | Speed x" + item.getSpeedMultiplier() + " for " + item.getEffectDuration() + "s";
                }
            } else if (item.getType() == Item.ItemType.ARMOR) {
                itemInfo += " | Defense: +" + item.getDefenseBonus();
            } else if (item.getType() == Item.ItemType.POTION) {
                if (item.getHealthRestore() > 0) {
                    itemInfo += " | Restores: " + item.getHealthRestore() + " HP";
                }
                if (item.getSpeedMultiplier() > 0) {
                    itemInfo += " | Speed x" + item.getSpeedMultiplier() + " for " + item.getEffectDuration() + "s";
                }
            }
            inventoryTable.add(new Label(itemInfo, labelStyle)).pad(5);

            TextButton actionButton = new TextButton(item.getType() == Item.ItemType.POTION ? "Use" : "Equip", buttonStyle);
            actionButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (soundEnabled) buttonClickSound.play(volume);
                    useItem(item);
                }
            });
            inventoryTable.add(actionButton).pad(5);
            inventoryTable.row();
        }

        TextButton closeButton = new TextButton("Close", buttonStyle);
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (soundEnabled) buttonClickSound.play(volume);
                inventoryVisible = false;
                Gdx.input.setInputProcessor(null);
            }
        });
        inventoryTable.add(closeButton).colspan(3).pad(10);
    }

    private void spawnEnemies(int count) {
        Gdx.app.log("GameManager", "Spawning " + count + " enemies");
        for (int i = 0; i < count; i++) {
            Enemy enemy = new Enemy(level.getLevelMap(), level);
            enemies.add(enemy);
            Gdx.app.log("GameManager", "Enemy " + (i + 1) + " spawned at (" + enemy.getX() + "," + enemy.getY() + ")");
        }
    }

    public void update() {
        if (gameState == GameState.PLAYING) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
                inventoryVisible = !inventoryVisible;
                if (inventoryVisible) {
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
                if (gameState != GameState.PLAYING) {
                    Gdx.app.log("GameManager", "SPACE pressed but ignored (gameState=" + gameState + ")");
                } else if (inventoryVisible) {
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
                    if (enemy.isDead()) {
                        List<Item> droppedItems = enemy.dropItems();
                        for (Item item : droppedItems) {
                            if (player.getInventory().addItem(item)) {
                                if (item.getType() == Item.ItemType.WEAPON) {
                                    player.equipWeapon(item);
                                }
                            }
                        }
                        enemyIterator.remove();
                        enemy.dispose();
                    }
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

        if (currentMusicState != gameState) {
            updateMusic();
        }
    }

    public Player getPlayer() {
        return player;
    }

    public void setGameState(GameState state) {
        if (gameState != GameState.DEATH && state == GameState.DEATH) {
            previousState = gameState;
        } else if (gameState == GameState.DEATH && state != GameState.DEATH) {
            // После смерти возвращаем предыдущее состояние музыки
            gameState = state;
            updateMusic();
            return;
        }
        gameState = state;
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
        if (deathMusic != null) deathMusic.dispose();
        if (buttonClickSound != null) buttonClickSound.dispose();
    }
}
