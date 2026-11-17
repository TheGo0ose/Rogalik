package io.github.some_example_name.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.some_example_name.Assets;
import io.github.some_example_name.MainGame;
import io.github.some_example_name.audio.AudioManager;
import io.github.some_example_name.ui.UiStyles;

public final class MenuScreen extends ScreenAdapter {
    private final MainGame game;
    private final Stage stage;

    public MenuScreen(MainGame game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport(), game.getBatch());

        Texture panel = game.getAssets().texture(Assets.MENU_PANEL);
        Texture buttons = game.getAssets().texture(Assets.MENU_BUTTONS);

        Table root = new Table();
        root.setFillParent(true);
        Table menu = new Table();
        menu.setBackground(new TextureRegionDrawable(panel));
        root.add(menu).size(panel.getWidth(), panel.getHeight());
        stage.addActor(root);

        // butts.png: слева обычные кнопки, справа нажатые; строки — старт, настройки, выход
        addButton(menu, UiStyles.imageButton(buttons, 0, 0, 172, 0, 150, 69), game::startNewGame);
        addButton(menu, UiStyles.imageButton(buttons, 0, 79, 172, 79, 150, 70), game::showSettings);
        addButton(menu, UiStyles.imageButton(buttons, 0, 159, 172, 159, 150, 69), () -> Gdx.app.exit());
    }

    private void addButton(Table table, ImageButton.ImageButtonStyle style, Runnable action) {
        ImageButton button = new ImageButton(style);
        button.addListener(clickWithSound(game.getAudio(), action));
        table.add(button).pad(6).row();
    }

    static ClickListener clickWithSound(AudioManager audio, Runnable action) {
        return new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                audio.playClick();
                action.run();
            }
        };
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        game.getAudio().play(AudioManager.Track.MENU);
    }

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void render(float delta) {
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
}
