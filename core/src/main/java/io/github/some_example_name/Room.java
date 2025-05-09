package io.github.some_example_name;

public class Room {
    public int x, y, width, height;
    public RoomType type;

    public Room(int x, int y, int width, int height, RoomType type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
    }
}
