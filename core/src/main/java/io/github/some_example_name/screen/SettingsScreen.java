package io.github.some_example_name.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.some_example_name.Assets;
import io.github.some_example_name.MainGame;
import io.github.some_example_name.audio.AudioManager;
import io.github.some_example_name.ui.UiStyles;

public final class SettingsScreen extends ScreenAdapter {
    /** Кнопки из soundbuttons.png рисуются в 4 раза крупнее. */
    private static final float SCALE = 4f;

    private final MainGame game;
    private final AudioManager audio;
    private final Stage stage;
    private final Label soundState;
    private final Label volumeValue;
    private final ImageButton soundButton;

    public SettingsScreen(MainGame game) {
        this.game = game;
        this.audio = game.getAudio();
        this.stage = new Stage(new ScreenViewport(), game.getBatch());
        UiStyles ui = game.getUi();
        Texture sheet = game.getAssets().texture(Assets.SETTINGS_BUTTONS);

        // soundbuttons.png: верхний ряд — звук (обычная/нажатая) и Back; ниже — стрелки и полоска
        ImageButton.ImageButtonStyle soundStyle = UiStyles.imageButton(sheet, 4, 2, 27, 2, 14, 14);
        soundStyle.checked = soundStyle.down; // выключенный звук = «вдавленная» кнопка
        soundButton = new ImageButton(soundStyle);
        soundButton.addListener(MenuScreen.clickWithSound(audio, () -> {
            audio.setEnabled(!audio.isEnabled());
            refresh();
        }));

        ImageButton quieter = new ImageButton(UiStyles.imageButton(sheet, 3, 29, 3, 49, 14, 14));
        quieter.addListener(MenuScreen.clickWithSound(audio, () -> changeVolume(-1)));
        ImageButton louder = new ImageButton(UiStyles.imageButton(sheet, 79, 29, 79, 49, 14, 14));
        louder.addListener(MenuScreen.clickWithSound(audio, () -> changeVolume(1)));
        ImageButton back = new ImageButton(UiStyles.imageButton(sheet, 47, 2, 83, 2, 30, 14));
        back.addListener(MenuScreen.clickWithSound(audio, game::showMenu));

        VolumeBar volumeBar = new VolumeBar(new TextureRegion(sheet, 26, 29, 44, 14), game.getAssets().whitePixel());

        soundState = new Label("", ui.label);
        volumeValue = new Label("", ui.label);

        Table table = new Table();
        table.setFillParent(true);
        table.defaults().pad(10);
        table.add(new Label("Настройки", ui.title)).colspan(5).padBottom(40).row();

        table.add(new Label("Звук", ui.label)).left();
        table.add(soundButton).size(14 * SCALE);
        table.add(soundState).colspan(3).left().row();

        table.add(new Label("Громкость", ui.label)).left();
        table.add(quieter).size(14 * SCALE);
        table.add(volumeBar).size(44 * SCALE, 14 * SCALE);
        table.add(louder).size(14 * SCALE);
        table.add(volumeValue).width(80).left().row();

        table.add(back).size(30 * SCALE, 14 * SCALE).colspan(5).padTop(40);
        stage.addActor(table);
        refresh();
    }

    private void changeVolume(int steps) {
        audio.changeVolume(steps);
        refresh();
    }

    private void refresh() {
        soundButton.setChecked(!audio.isEnabled());
        soundState.setText(audio.isEnabled() ? "вкл" : "выкл");
        volumeValue.setText(Math.round(audio.getVolume() * 100) + "%");
    }

    @Override
    public void show() {
        refresh();
        Gdx.input.setInputProcessor(stage);
        game.getAudio().play(AudioManager.Track.MENU);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.showMenu();
            return;
        }
        ScreenUtils.clear(0, 0, 0, 1);
        stage.getViewport().apply();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    /** Полоска громкости: рамка из спрайт-листа и заливка пропорционально громкости. */
    private final class VolumeBar extends Actor {
        private final TextureRegionDrawable frame;
        private final Texture pixel;
        private final Color fillColor = new Color(0.55f, 0.65f, 0.95f, 1f);

        VolumeBar(TextureRegion frame, Texture pixel) {
            this.frame = new TextureRegionDrawable(frame);
            this.pixel = pixel;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            frame.draw(batch, getX(), getY(), getWidth(), getHeight());
            float inset = 3 * SCALE;
            float fillWidth = (getWidth() - inset * 2) * audio.getVolume();
            if (fillWidth > 0) {
                batch.setColor(fillColor);
                batch.draw(pixel, getX() + inset, getY() + inset, fillWidth, getHeight() - inset * 2);
                batch.setColor(Color.WHITE);
            }
        }
    }
}
