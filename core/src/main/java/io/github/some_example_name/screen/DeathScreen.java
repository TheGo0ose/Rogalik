package io.github.some_example_name.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.some_example_name.Assets;
import io.github.some_example_name.MainGame;
import io.github.some_example_name.audio.AudioManager;

/** Экран смерти: картинка плавно проявляется, затем можно начать заново или выйти в меню. */
public final class DeathScreen extends ScreenAdapter {
    private static final float FADE_SPEED = 0.5f;
    /** Небольшая задержка, чтобы случайное нажатие в момент смерти не пропустило экран. */
    private static final float INPUT_DELAY = 0.75f;

    private final MainGame game;
    private final Texture image;
    private final FitViewport imageViewport;
    private final ScreenViewport textViewport = new ScreenViewport();
    private float time;

    public DeathScreen(MainGame game) {
        this.game = game;
        this.image = game.getAssets().texture(Assets.DEATH_IMAGE);
        this.imageViewport = new FitViewport(image.getWidth(), image.getHeight());
    }

    @Override
    public void show() {
        time = 0;
        Gdx.input.setInputProcessor(null);
        game.getAudio().play(AudioManager.Track.DEATH);
    }

    @Override
    public void render(float delta) {
        time += delta;
        if (time >= INPUT_DELAY) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
                game.startNewGame();
                return;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                game.showMenu();
                return;
            }
        }

        SpriteBatch batch = game.getBatch();
        float alpha = Math.min(1f, time * FADE_SPEED);
        ScreenUtils.clear(0, 0, 0, 1);

        imageViewport.apply();
        batch.setProjectionMatrix(imageViewport.getCamera().combined);
        batch.begin();
        batch.setColor(1, 1, 1, alpha);
        batch.draw(image, 0, 0, imageViewport.getWorldWidth(), imageViewport.getWorldHeight());
        batch.setColor(Color.WHITE);
        batch.end();

        if (time >= INPUT_DELAY) {
            textViewport.apply();
            batch.setProjectionMatrix(textViewport.getCamera().combined);
            batch.begin();
            BitmapFont font = game.getUi().font;
            font.draw(batch, "R — начать заново     ESC — в меню",
                0, 48, textViewport.getWorldWidth(), Align.center, false);
            batch.end();
        }
    }

    @Override
    public void resize(int width, int height) {
        imageViewport.update(width, height, true);
        textViewport.update(width, height, true);
    }
}
