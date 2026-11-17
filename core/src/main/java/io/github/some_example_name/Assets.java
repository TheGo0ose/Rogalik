package io.github.some_example_name;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;
import io.github.some_example_name.item.Items;

/**
 * Единое место загрузки и владения ресурсами. Каждая текстура/звук загружается
 * один раз и разделяется всеми, кто её использует (раньше каждый слизень
 * и каждый предмет грузили свою копию текстуры).
 */
public final class Assets implements Disposable {
    public static final String PLAYER_WALK = "player2.png";
    public static final String PLAYER_ATTACK = "player_attack.png";
    public static final String SLIME_WALK = "Slime1_Walk_full.png";
    public static final String SLIME_ATTACK = "Slime1_Attack_full.png";
    public static final String SLIME_HURT = "Slime1_Hurt_full.png";
    public static final String SLIME_DEATH = "Slime1_Death_full.png";
    public static final String FLOOR = "floor.png";
    public static final String WALL = "wall.png";
    public static final String MENU_PANEL = "menu_butt.png";
    public static final String MENU_BUTTONS = "butts.png";
    public static final String SETTINGS_BUTTONS = "soundbuttons.png";
    public static final String INVENTORY = "Inventory.png";
    public static final String HEALTH_BAR_FRAME = "healthbar.png";
    public static final String HEALTH_BAR_FILL = "health.png";
    public static final String DEATH_IMAGE = "death_image.png";

    public static final String MUSIC_MENU = "menu_music.wav";
    public static final String MUSIC_GAME = "game_music.wav";
    public static final String MUSIC_DEATH = "death_music.wav";
    public static final String SOUND_CLICK = "button_click.wav";

    public static final String FONT = "fonts/PressStart2P-Regular.ttf";

    private static final String[] TEXTURES = {
        PLAYER_WALK, PLAYER_ATTACK,
        SLIME_WALK, SLIME_ATTACK, SLIME_HURT, SLIME_DEATH,
        FLOOR, WALL,
        MENU_PANEL, MENU_BUTTONS, SETTINGS_BUTTONS, INVENTORY,
        HEALTH_BAR_FRAME, HEALTH_BAR_FILL, DEATH_IMAGE,
    };
    private static final String[] MUSIC = {MUSIC_MENU, MUSIC_GAME, MUSIC_DEATH};

    private final AssetManager manager = new AssetManager();
    private Texture whitePixel;

    /** Синхронно загружает всё. Ресурсов мало, экран загрузки не нужен. */
    public void loadAll() {
        for (String path : TEXTURES) manager.load(path, Texture.class);
        for (String path : Items.ALL_ICONS) manager.load(path, Texture.class);
        for (String path : MUSIC) manager.load(path, Music.class);
        manager.load(SOUND_CLICK, Sound.class);
        manager.finishLoading();

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        whitePixel = new Texture(pixmap);
        pixmap.dispose();
    }

    public Texture texture(String path) {
        return manager.get(path, Texture.class);
    }

    public Music music(String path) {
        return manager.get(path, Music.class);
    }

    public Sound sound(String path) {
        return manager.get(path, Sound.class);
    }

    /** Белый пиксель 1×1 — для полосок здоровья, подсветки и затемнения (красится через batch.setColor). */
    public Texture whitePixel() {
        return whitePixel;
    }

    @Override
    public void dispose() {
        manager.dispose();
        if (whitePixel != null) whitePixel.dispose();
    }
}
