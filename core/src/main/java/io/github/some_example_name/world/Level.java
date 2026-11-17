package io.github.some_example_name.world;

import java.util.Collections;
import java.util.List;

/** Карта уровня: клетки и комнаты. Не содержит графики. */
public final class Level {
    public static final int TILE_SIZE = 64;

    private final int width, height;
    private final Tile[][] tiles;
    private final List<Room> rooms;

    Level(int width, int height, List<Room> rooms) {
        this.width = width;
        this.height = height;
        this.rooms = List.copyOf(rooms);
        this.tiles = new Tile[width][height];
        for (Tile[] column : tiles) {
            java.util.Arrays.fill(column, Tile.VOID);
        }
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public List<Room> getRooms() { return Collections.unmodifiableList(rooms); }

    public Tile getTile(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) return Tile.VOID;
        return tiles[x][y];
    }

    void setTile(int x, int y, Tile tile) {
        if (x >= 0 && y >= 0 && x < width && y < height) {
            tiles[x][y] = tile;
        }
    }

    public boolean isWalkable(int tileX, int tileY) {
        return getTile(tileX, tileY).isWalkable();
    }

    /** Проверяет, что прямоугольник (в мировых координатах) целиком стоит на проходимых клетках. */
    public boolean isAreaWalkable(float x, float y, float w, float h) {
        int x0 = toTile(x);
        int y0 = toTile(y);
        int x1 = toTile(x + w - 0.001f);
        int y1 = toTile(y + h - 0.001f);
        for (int tx = x0; tx <= x1; tx++) {
            for (int ty = y0; ty <= y1; ty++) {
                if (!isWalkable(tx, ty)) return false;
            }
        }
        return true;
    }

    public Room getStartRoom() {
        for (Room room : rooms) {
            if (room.getType() == RoomType.START) return room;
        }
        return rooms.get(0);
    }

    /** Комната, в которой находится точка (в мировых координатах), или {@code null} (коридор). */
    public Room roomAt(float worldX, float worldY) {
        int tx = toTile(worldX);
        int ty = toTile(worldY);
        for (Room room : rooms) {
            if (room.containsTile(tx, ty)) return room;
        }
        return null;
    }

    public static int toTile(float world) {
        return (int) Math.floor(world / TILE_SIZE);
    }
}
