package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;


public class MainGame extends ApplicationAdapter {
    private GameManager gameManager;
    private OrthographicCamera camera;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        camera.update();

        gameManager = new GameManager(camera);
        gameManager.initialize();
    }


    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        gameManager.update();
        gameManager.render();
    }

    @Override
    public void dispose() {
        gameManager.dispose();
    }
}
