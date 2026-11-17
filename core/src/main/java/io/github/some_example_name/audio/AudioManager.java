package io.github.some_example_name.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import io.github.some_example_name.Assets;

import java.util.EnumMap;
import java.util.Map;

/**
 * Музыка, звуки и их настройки. Громкость и вкл/выкл сохраняются между запусками.
 * Экраны только говорят, какой трек нужен, — переключение и громкость здесь.
 */
public final class AudioManager {
    public enum Track { MENU, GAME, DEATH }

    private static final String PREFS_NAME = "rogalik-settings";
    private static final String KEY_VOLUME = "volume";
    private static final String KEY_ENABLED = "soundEnabled";
    private static final float VOLUME_STEP = 0.1f;

    private final Map<Track, Music> tracks = new EnumMap<>(Track.class);
    private final Sound click;
    private final Preferences prefs;
    private float volume;
    private boolean enabled;
    private Track current;

    public AudioManager(Assets assets) {
        tracks.put(Track.MENU, assets.music(Assets.MUSIC_MENU));
        tracks.put(Track.GAME, assets.music(Assets.MUSIC_GAME));
        tracks.put(Track.DEATH, assets.music(Assets.MUSIC_DEATH));
        tracks.get(Track.MENU).setLooping(true);
        tracks.get(Track.GAME).setLooping(true);
        tracks.get(Track.DEATH).setLooping(false);
        click = assets.sound(Assets.SOUND_CLICK);

        prefs = Gdx.app.getPreferences(PREFS_NAME);
        volume = clampVolume(prefs.getFloat(KEY_VOLUME, 1f));
        enabled = prefs.getBoolean(KEY_ENABLED, true);
    }

    /** Включает трек с начала. Повторный вызов с тем же треком ничего не делает. */
    public void play(Track track) {
        if (track == current) return;
        if (current != null) tracks.get(current).stop();
        current = track;
        startCurrent();
    }

    private void startCurrent() {
        if (current == null || !enabled) return;
        Music music = tracks.get(current);
        music.setVolume(volume);
        music.play();
    }

    public void playClick() {
        if (enabled && volume > 0) click.play(volume);
    }

    public void setEnabled(boolean value) {
        if (enabled == value) return;
        enabled = value;
        if (enabled) {
            startCurrent();
        } else if (current != null) {
            tracks.get(current).pause();
        }
        save();
    }

    public void changeVolume(int steps) {
        volume = clampVolume(volume + steps * VOLUME_STEP);
        for (Music music : tracks.values()) music.setVolume(volume);
        save();
    }

    public boolean isEnabled() { return enabled; }
    public float getVolume() { return volume; }

    /** Округляем до шага, чтобы 0.1 + 0.1 + ... не давало 0.70000005. */
    private static float clampVolume(float value) {
        float rounded = Math.round(value / VOLUME_STEP) * VOLUME_STEP;
        return Math.max(0f, Math.min(1f, rounded));
    }

    private void save() {
        prefs.putFloat(KEY_VOLUME, volume);
        prefs.putBoolean(KEY_ENABLED, enabled);
        prefs.flush();
    }
}
