package io.github.some_example_name;

import com.badlogic.gdx.Gdx;

import java.util.ArrayList;
import java.util.List;

public class Inventory {
    private List<Item> items;
    private static final int MAX_ITEMS = 10; // Максимальное количество предметов

    public Inventory() {
        items = new ArrayList<>();
    }

    public boolean addItem(Item item) {
        if (items.size() >= MAX_ITEMS) {
            Gdx.app.log("Inventory", "Cannot add item: " + item.getName() + ". Inventory is full.");
            return false;
        }
        items.add(item);
        Gdx.app.log("Inventory", "Added item: " + item.getName());
        return true;
    }

    public void removeItem(Item item) {
        items.remove(item);
        Gdx.app.log("Inventory", "Removed item: " + item.getName());
    }

    public List<Item> getItems() {
        return items;
    }
}
