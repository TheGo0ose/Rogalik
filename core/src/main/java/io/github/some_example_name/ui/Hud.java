package io.github.some_example_name.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Align;
import io.github.some_example_name.Assets;
import io.github.some_example_name.entity.Player;

/** Интерфейс поверх игры: здоровье, подсказки, всплывающие сообщения, пауза. */
public final class Hud {
    private static final float SCALE = 4f;
    private static final float MARGIN = 12f;
    /** Положение слота заливки внутри healthbar.png (в пикселях исходника, от левого нижнего угла). */
    private static final int FILL_SLOT_X = 26;
    private static final int FILL_SLOT_Y = 17;
    private static final float MESSAGE_DURATION = 2.5f;
    private static final String CONTROLS = "WASD — ходьба   ПРОБЕЛ — удар   I — инвентарь   ESC — пауза";

    private final OrthographicCamera camera = new OrthographicCamera();
    private final Texture frame;
    private final Texture fill;
    private final Texture pixel;
    private final BitmapFont font;
    private final BitmapFont titleFont;
    private final GlyphLayout layout = new GlyphLayout();

    private String message;
    private float messageTimer;

    public Hud(Assets assets, UiStyles ui) {
        frame = assets.texture(Assets.HEALTH_BAR_FRAME);
        fill = assets.texture(Assets.HEALTH_BAR_FILL);
        pixel = assets.whitePixel();
        font = ui.font;
        titleFont = ui.titleFont;
    }

    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    public void showMessage(String text) {
        message = text;
        messageTimer = MESSAGE_DURATION;
    }

    public void update(float delta) {
        if (messageTimer > 0) messageTimer -= delta;
    }

    public void render(SpriteBatch batch, Player player, boolean paused) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawHealth(batch, player);
        font.setColor(0.8f, 0.8f, 0.8f, 1f);
        font.draw(batch, CONTROLS, MARGIN, MARGIN + font.getCapHeight());
        font.setColor(Color.WHITE);

        if (messageTimer > 0 && message != null) {
            float alpha = Math.min(1f, messageTimer / 0.5f);
            font.setColor(1f, 0.9f, 0.4f, alpha);
            font.draw(batch, message, 0, camera.viewportHeight * 0.75f, camera.viewportWidth, Align.center, false);
            font.setColor(Color.WHITE);
        }

        if (paused) drawPause(batch);
        batch.end();
    }

    private void drawHealth(SpriteBatch batch, Player player) {
        float frameW = frame.getWidth() * SCALE;
        float frameH = frame.getHeight() * SCALE;
        float x = MARGIN;
        float y = camera.viewportHeight - MARGIN - frameH;
        batch.draw(frame, x, y, frameW, frameH);

        float ratio = (float) player.getHealth() / Player.MAX_HEALTH;
        int srcWidth = Math.round(fill.getWidth() * ratio);
        if (srcWidth > 0) {
            // рисуем левую часть текстуры, а не сжимаем её целиком
            batch.draw(fill, x + FILL_SLOT_X * SCALE, y + FILL_SLOT_Y * SCALE,
                srcWidth * SCALE, fill.getHeight() * SCALE,
                0, 0, srcWidth, fill.getHeight(), false, false);
        }

        String text = player.getHealth() + "/" + Player.MAX_HEALTH;
        font.draw(batch, text, x + FILL_SLOT_X * SCALE, y + FILL_SLOT_Y * SCALE - 8);
    }

    private void drawPause(SpriteBatch batch) {
        batch.setColor(0, 0, 0, 0.6f);
        batch.draw(pixel, 0, 0, camera.viewportWidth, camera.viewportHeight);
        batch.setColor(Color.WHITE);

        float centerY = camera.viewportHeight / 2f;
        layout.setText(titleFont, "ПАУЗА");
        titleFont.draw(batch, layout, (camera.viewportWidth - layout.width) / 2f, centerY + 60);
        font.draw(batch, "ESC — продолжить", 0, centerY, camera.viewportWidth, Align.center, false);
        font.draw(batch, "M — выйти в меню", 0, centerY - 32, camera.viewportWidth, Align.center, false);
    }
}
