package io.github.some_example_name.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Прямоугольная комната. Координаты и размеры задают только пол (в клетках);
 * стены вокруг генерируются автоматически.
 */
public final class Room {
    private int x, y;
    private final int width, height;
    private final RoomType type;
    private final List<Room> neighbors = new ArrayList<>();

    public Room(int x, int y, int width, int height, RoomType type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public RoomType getType() { return type; }
    public int getCenterX() { return x + width / 2; }
    public int getCenterY() { return y + height / 2; }
    public List<Room> getNeighbors() { return Collections.unmodifiableList(neighbors); }

    public boolean containsTile(int tileX, int tileY) {
        return tileX >= x && tileX < x + width && tileY >= y && tileY < y + height;
    }

    /** Пересекается ли комната с другой с учётом отступа {@code margin} клеток. */
    public boolean overlaps(Room other, int margin) {
        return x - margin < other.x + other.width
            && x + width + margin > other.x
            && y - margin < other.y + other.height
            && y + height + margin > other.y;
    }

    void connect(Room other) {
        if (other != this && !neighbors.contains(other)) {
            neighbors.add(other);
            other.neighbors.add(this);
        }
    }

    void translate(int dx, int dy) {
        x += dx;
        y += dy;
    }
}
