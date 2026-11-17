package io.github.some_example_name.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import io.github.some_example_name.Assets;

/**
 * Шрифты и стили интерфейса. Шрифты создаются один раз (раньше BitmapFont
 * создавался и уничтожался каждый кадр) и поддерживают кириллицу.
 */
public final class UiStyles implements Disposable {
    private static final String CYRILLIC =
        "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя—«»№…";

    public final BitmapFont font;
    public final BitmapFont titleFont;
    public final Label.LabelStyle label;
    public final Label.LabelStyle title;

    public UiStyles() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(Assets.FONT));
        try {
            // Press Start 2P рисуется чётко только в размерах, кратных 8
            font = generate(generator, 16);
            titleFont = generate(generator, 32);
        } finally {
            generator.dispose();
        }
        label = new Label.LabelStyle(font, Color.WHITE);
        title = new Label.LabelStyle(titleFont, Color.WHITE);
    }

    private static BitmapFont generate(FreeTypeFontGenerator generator, int size) {
        FreeTypeFontGenerator.FreeTypeFontParameter p = new FreeTypeFontGenerator.FreeTypeFontParameter();
        p.size = size;
        p.mono = true;
        p.characters = FreeTypeFontGenerator.DEFAULT_CHARS + CYRILLIC;
        p.shadowOffsetX = size / 8;
        p.shadowOffsetY = size / 8;
        p.shadowColor = new Color(0, 0, 0, 0.75f);
        p.minFilter = Texture.TextureFilter.Nearest;
        p.magFilter = Texture.TextureFilter.Nearest;
        BitmapFont font = generator.generateFont(p);
        font.getData().setLineHeight(size * 1.5f); // по умолчанию строки слипаются
        return font;
    }

    /** Кнопка-картинка из двух регионов спрайт-листа: обычное и нажатое состояние. */
    public static ImageButton.ImageButtonStyle imageButton(TextureRegion up, TextureRegion down) {
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.up = new TextureRegionDrawable(up);
        style.down = new TextureRegionDrawable(down);
        return style;
    }

    public static ImageButton.ImageButtonStyle imageButton(Texture sheet, int upX, int upY, int downX, int downY,
                                                           int width, int height) {
        return imageButton(new TextureRegion(sheet, upX, upY, width, height),
            new TextureRegion(sheet, downX, downY, width, height));
    }

    @Override
    public void dispose() {
        font.dispose();
        titleFont.dispose();
    }
}
