package io.github.some_example_name.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Рюкзак игрока фиксированной вместимости (по числу ячеек в окне инвентаря). */
public final class Inventory {
    public static final int CAPACITY = 18;

    private final List<Item> items = new ArrayList<>();

    /** @return {@code false}, если рюкзак полон. */
    public boolean add(Item item) {
        if (isFull()) return false;
        items.add(item);
        return true;
    }

    public boolean remove(Item item) {
        return items.remove(item);
    }

    public boolean contains(Item item) {
        return items.contains(item);
    }

    public boolean isFull() {
        return items.size() >= CAPACITY;
    }

    public int size() {
        return items.size();
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }
}
