package io.github.some_example_name;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Enemy {
    private float x, y;
    private int[][] levelMap;
    private int health = 50; // Пример здоровья врага
    private boolean isDead = false;

    public Enemy(int[][] levelMap) {
        this.levelMap = levelMap;
        // Инициализация позиции (пример)
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 15; j++) {
                if (levelMap[i][j] == 0 && Math.random() < 0.05) { // 5% шанс спавна
                    x = i * Level.TILE_SIZE;
                    y = j * Level.TILE_SIZE;
                    return;
                }
            }
        }
    }

    public List<Item> dropItems() {
        List<Item> droppedItems = new ArrayList<>();
        Random random = new Random();

        // Шанс дропа Стального меча — 10%
        if (random.nextFloat() < 0.1f) {
            droppedItems.add(new Item("Редкий меч", "Прочный и острый", Item.ItemType.WEAPON, 10, 0, 0, "rare_sword.png"));
        }

        // Шанс дропа Огненного меча — 5%
        if (random.nextFloat() < 0.05f) {
            droppedItems.add(new Item("Мифический меч", "Пылающий клинок", Item.ItemType.WEAPON, 15, 1.5f, 10, "myth_sword.png"));
        }

        return droppedItems;
    }

    public void update(float playerX, float playerY) {
        // Логика движения или атаки (пример)
        if (health <= 0) isDead = true;
    }

    public boolean isDead() {
        return isDead;
    }

    public void render(SpriteBatch batch) {
        // Отрисовка врага (пример)
    }

    public void dispose() {
        // Освобождение ресурсов, если есть
    }
}
