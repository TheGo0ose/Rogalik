package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

public class Level {
    public static final int TILE_SIZE = 64;
    private int[][] levelMap;
    private Texture wallSheet;
    private Texture floor;
    private TextureRegion[] wallRegion;
    private TextureRegion floorRegion;

    private final Array<Room> rooms = new Array<>();

    public Level() {
        levelMap = new int[60][15]; // увеличено для длинной генерации
        wallSheet = new Texture("wall.png");
        floor = new Texture("floor.png");

        wallRegion = new TextureRegion[10];
        for (int i = 0; i < 10; i++) {
            wallRegion[i] = new TextureRegion(wallSheet, i * 0, 0, 64, 97);
        }
        floorRegion = new TextureRegion(floor, 0, 0, 64, 64);
    }

    public void generateLevel() {
        // Initialize map with void tiles (-1)
        for (int x = 0; x < levelMap.length; x++) {
            for (int y = 0; y < levelMap[0].length; y++) {
                levelMap[x][y] = -1; // пустота
            }
        }

        rooms.clear();
        generateLinearRooms(5); // количество комнат
        drawRooms();
        connectRooms();
    }

    private void generateLinearRooms(int count) {
        int startX = 2;
        int startY = 5;

        for (int i = 0; i < count; i++) {
            RoomType type;
            if (i == 0) type = RoomType.START;
            else if (i == count - 1) type = RoomType.BOSS;
            else type = MathUtils.randomBoolean() ? RoomType.ENEMY : RoomType.TREASURE;

            int width = (type == RoomType.START) ? 3 : 6;
            int height = (type == RoomType.START) ? 3 : 6;

            Room room = new Room(startX, startY, width, height, type);
            rooms.add(room);

            startX += width + 4; // расстояние между комнатами
        }
    }

    private void drawRooms() {
        for (Room room : rooms) {
            for (int x = room.x; x < room.x + room.width; x++) {
                for (int y = room.y; y < room.y + room.height; y++) {
                    if (x == room.x || x == room.x + room.width - 1 ||
                        y == room.y || y == room.y + room.height - 1) {
                        levelMap[x][y] = 1; // стена
                    } else {
                        levelMap[x][y] = 0; // пол
                    }
                }
            }
        }
    }

    private void connectRooms() {
        for (int i = 1; i < rooms.size; i++) {
            Room prev = rooms.get(i - 1);
            Room curr = rooms.get(i);

            int y = (prev.y + prev.height / 2);
            int startX = prev.x + prev.width;
            int endX = curr.x - 1;

            for (int x = startX; x <= endX; x++) {
                if (x >= 0 && x < levelMap.length && y >= 0 && y < levelMap[0].length) {
                    levelMap[x][y] = 0; // коридор (пол)
                }
            }
            // Optional: Add walls around corridor
            if (endX >= 0 && endX < levelMap.length) {
                if (y + 1 < levelMap[0].length) levelMap[endX][y + 1] = 1;
                if (y - 1 >= 0) levelMap[endX][y - 1] = 1;
            }
        }
    }

    public TextureRegion getWallRegion(int x, int y) {
        if (levelMap[x][y] != 1) return null; // Return null for non-wall tiles (floor or void)

        boolean top = y < levelMap[0].length - 1 && levelMap[x][y + 1] == 1;
        boolean bottom = y > 0 && levelMap[x][y - 1] == 1;
        boolean left = x > 0 && levelMap[x - 1][y] == 1;
        boolean right = x < levelMap.length - 1 && levelMap[x + 1][y] == 1;

        if (top && bottom && left && right) return wallRegion[0];
        else if (top && bottom && left) return wallRegion[1];
        else if (top && bottom && right) return wallRegion[2];
        else if (top && bottom) return wallRegion[3];
        else if (left && right) return wallRegion[4];
        else if (top && left) return wallRegion[5];
        else if (top && right) return wallRegion[6];
        else if (bottom && left) return wallRegion[7];
        else if (bottom && right) return wallRegion[8];
        else return wallRegion[9];
    }

    public TextureRegion getFloorRegion(int x, int y) {
        if (levelMap[x][y] == 0) return floorRegion; // Return floor texture for floor tiles
        return null; // Return null for void or wall tiles
    }

    public int getTileType(int x, int y) {
        return levelMap[x][y]; // Return tile type (-1 for void, 0 for floor, 1 for wall)
    }

    public int[][] getLevelMap() {
        return levelMap;
    }

    public void dispose() {
        wallSheet.dispose();
        floor.dispose();
    }
}
