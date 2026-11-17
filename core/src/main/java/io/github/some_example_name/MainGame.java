package io.github.some_example_name;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.some_example_name.audio.AudioManager;
import io.github.some_example_name.render.Sprites;
import io.github.some_example_name.screen.DeathScreen;
import io.github.some_example_name.screen.GameScreen;
import io.github.some_example_name.screen.MenuScreen;
import io.github.some_example_name.screen.SettingsScreen;
import io.github.some_example_name.ui.UiStyles;

/**
 * Точка входа. Владеет общими ресурсами (SpriteBatch, ассеты, звук, шрифты)
 * и переключает экраны. Сама ничего не рисует и не обновляет.
 */
public class MainGame extends Game {
    private SpriteBatch batch;
    private Assets assets;
    private AudioManager audio;
    private UiStyles ui;
    private Sprites sprites;

    private MenuScreen menuScreen;
    private SettingsScreen settingsScreen;
    private DeathScreen deathScreen;
    private GameScreen gameScreen;

    @Override
    public void create() {
        batch = new SpriteBatch();
        assets = new Assets();
        assets.loadAll();
        audio = new AudioManager(assets);
        ui = new UiStyles();
        sprites = new Sprites(assets);

        menuScreen = new MenuScreen(this);
        settingsScreen = new SettingsScreen(this);
        deathScreen = new DeathScreen(this);
        showMenu();
    }

    public void showMenu() {
        setScreen(menuScreen);
    }

    public void showSettings() {
        setScreen(settingsScreen);
    }

    public void showDeath() {
        setScreen(deathScreen);
    }

    /** Новый забег: новый уровень и новый игрок. Старый экран игры освобождается. */
    public void startNewGame() {
        GameScreen previous = gameScreen;
        gameScreen = new GameScreen(this);
        setScreen(gameScreen);
        if (previous != null) previous.dispose();
    }

    public SpriteBatch getBatch() { return batch; }
    public Assets getAssets() { return assets; }
    public AudioManager getAudio() { return audio; }
    public UiStyles getUi() { return ui; }
    public Sprites getSprites() { return sprites; }

    @Override
    public void dispose() {
        super.dispose();
        menuScreen.dispose();
        settingsScreen.dispose();
        deathScreen.dispose();
        if (gameScreen != null) gameScreen.dispose();
        ui.dispose();
        assets.dispose();
        batch.dispose();
    }
}
