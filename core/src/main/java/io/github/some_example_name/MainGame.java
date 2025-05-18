package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import io.github.some_example_name.GameManager.GameState; // Добавляем импорт

public class MainGame extends ApplicationAdapter {
    private GameManager gameManager;
    private OrthographicCamera camera;
    private boolean isPlayerDead = false;
    private float deathAlpha = 0f;
    private final float fadeSpeed = 0.5f;
    private Texture deathImage;
    private SpriteBatch batch;
    private float deathTimer = 0f;
    private final float DEATH_DELAY = 5f; // Задержка перед перезапуском (2 секунды)

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        camera.update();

        gameManager = new GameManager(camera);
        gameManager.initialize();

        batch = new SpriteBatch();
        deathImage = new Texture("death_image.png"); // Убедись, что файл в assets
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (isPlayerDead) {
            gameManager.setGameState(GameState.DEATH); // Теперь используем просто GameState.DEATH
            renderDeathScreen(Gdx.graphics.getDeltaTime());
            deathTimer += Gdx.graphics.getDeltaTime();
            if (deathTimer >= DEATH_DELAY) {
                resetGame();
            }
        } else {
            gameManager.update();

            if (gameManager.getPlayer().isDead()) {
                isPlayerDead = true;
                return;
            }

            gameManager.render();
        }
    }

    private void renderDeathScreen(float delta) {
        if (deathAlpha < 1f) {
            deathAlpha += fadeSpeed * delta;
            if (deathAlpha > 1f) deathAlpha = 1f;
        }

        batch.begin();
        batch.setColor(1, 1, 1, deathAlpha);
        batch.draw(deathImage, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setColor(1, 1, 1, 1); // Сброс цвета
        batch.end();
    }

    private void resetGame() {
        isPlayerDead = false;
        deathAlpha = 0f;
        deathTimer = 0f;
        gameManager.dispose(); // Очищаем старые ресурсы
        gameManager = new GameManager(camera);
        gameManager.initialize(); // Перезапуск игры
        gameManager.setGameState(GameState.MENU); // Возвращаемся в меню
    }

    @Override
    public void dispose() {
        gameManager.dispose();
        batch.dispose();
        deathImage.dispose();
    }
}
