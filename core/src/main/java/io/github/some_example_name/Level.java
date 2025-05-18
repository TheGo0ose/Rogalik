package io.github.some_example_name;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Level {
    public static final int TILE_SIZE = 64;
    private int[][] levelMap;
    private Texture wallSheet;
    private Texture floor;
    private TextureRegion[] wallRegion;
    private TextureRegion floorRegion;
    public final Array<Room> rooms = new Array<>();
    private List<Enemy> enemies = new ArrayList<>();

    public Level() {
        levelMap = new int[60][15];
        wallSheet = new Texture("wall.png");
        floor = new Texture("floor.png");

        wallRegion = new TextureRegion[10];
        for (int i = 0; i < 10; i++) {
            wallRegion[i] = new TextureRegion(wallSheet, i * 0, 0, 64, 97);
        }
        floorRegion = new TextureRegion(floor, 0, 0, 64, 64);
    }

    public void generateLevel() {
        for (int x = 0; x < levelMap.length; x++) {
            for (int y = 0; y < levelMap[0].length; y++) {
                levelMap[x][y] = -1;
            }
        }

        rooms.clear();
        LevelGenerator generator = new LevelGenerator();
        List<Room> generatedRooms = generator.generateLevel();

        rooms.addAll(new Array<>(generatedRooms.toArray(new Room[0])));

        drawRooms();
        connectRooms();
        spawnEnemies();
    }

    private void drawRooms() {
        for (Room room : rooms) {
            for (int x = room.x; x < room.x + room.width; x++) {
                for (int y = room.y; y < room.y + room.height; y++) {
                    if (x >= 0 && x < levelMap.length && y >= 0 && y < levelMap[0].length) {
                        if (x == room.x || x == room.x + room.width - 1 ||
                            y == room.y || y == room.y + room.height - 1) {
                            levelMap[x][y] = 1;
                        } else {
                            levelMap[x][y] = 0;
                        }
                    }
                }
            }
        }
    }

    private void connectRooms() {
        for (Room room : rooms) {
            for (Room neighbor : room.neighbors) {
                int startX = room.x + room.width / 2;
                int startY = room.y + room.height / 2;
                int endX = neighbor.x + neighbor.width / 2;
                int endY = neighbor.y + neighbor.height / 2;

                int dx = endX - startX;
                int dy = endY - startY;
                int steps = Math.max(Math.abs(dx), Math.abs(dy));
                for (int i = 0; i <= steps; i++) {
                    int corridorX = startX + (dx * i) / steps;
                    int corridorY = startY + (dy * i) / steps;
                    if (corridorX >= 0 && corridorX < levelMap.length &&
                        corridorY >= 0 && corridorY < levelMap[0].length) {
                        levelMap[corridorX][corridorY] = 0;
                    }
                }
            }
        }
    }

    private void spawnEnemies() {
        Random random = new Random();
        Room startRoom = rooms.isEmpty() ? null : rooms.get(0);

        for (Room room : rooms) {
            if (room.type == RoomType.NORMAL && room != startRoom) {
                int enemyCount = random.nextInt(2, 5); // От 2 до 4 врагов
                List<int[]> freeTiles = new ArrayList<>();
                for (int i = room.x + 1; i < room.x + room.width - 1; i++) {
                    for (int j = room.y + 1; j < room.y + room.height - 1; j++) {
                        if (levelMap[i][j] == 0) {
                            freeTiles.add(new int[]{i, j});
                        }
                    }
                }
                if (freeTiles.size() >= enemyCount) {
                    for (int i = 0; i < enemyCount; i++) {
                        Enemy enemy = new Enemy(levelMap, this, room); // Спавн уже внутри конструктора
                        enemies.add(enemy);
                        Gdx.app.log("Level", "Spawned enemy " + i + " at (" + enemy.getX() + ", " + enemy.getY() + ") in room at (" + room.x + "," + room.y + ")");
                    }
                } else {
                    Gdx.app.log("Level", "Not enough free tiles in room at (" + room.x + "," + room.y + ") for " + enemyCount + " enemies, skipping");
                }
            }
        }
    }

    public int[][] getLevelMap() { return levelMap; }
    public TextureRegion getFloorRegion(int x, int y) { return floorRegion; }
    public TextureRegion getWallRegion(int x, int y) { return wallRegion[MathUtils.random(0, 9)]; }
    public List<Enemy> getEnemies() { return enemies; }

    public void dispose() {
        wallSheet.dispose();
        floor.dispose();
        for (Enemy enemy : enemies) {
            enemy.dispose();
        }
    }
}
