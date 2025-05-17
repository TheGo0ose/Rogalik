package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import java.util.List;

public class Level {
    public static final int TILE_SIZE = 64;
    private int[][] levelMap;
    private Texture wallSheet;
    private Texture floor;
    private TextureRegion[] wallRegion;
    private TextureRegion floorRegion;
    public final Array<Room> rooms = new Array<>();

    public Level() {
        levelMap = new int[60][15]; // Размер карты
        wallSheet = new Texture("wallref.png");
        floor = new Texture("floor.png");

        wallRegion = new TextureRegion[10];
        for (int i = 0; i < 10; i++) {
            wallRegion[i] = new TextureRegion(wallSheet, i * 0, 0, 64, 97);
        }
        floorRegion = new TextureRegion(floor, 0, 0, 64, 64);
    }

    public void generateLevel() {
        // Инициализация карты пустотой (-1)
        for (int x = 0; x < levelMap.length; x++) {
            for (int y = 0; y < levelMap[0].length; y++) {
                levelMap[x][y] = -1;
            }
        }

        rooms.clear();
        LevelGenerator generator = new LevelGenerator();
        List<Room> generatedRooms = generator.generateLevel();

        // Преобразование List в Array
        rooms.addAll(new Array<>(generatedRooms.toArray(new Room[0])));

        drawRooms();
        connectRooms();
    }

    private void drawRooms() {
        for (Room room : rooms) {
            for (int x = room.x; x < room.x + room.width; x++) {
                for (int y = room.y; y < room.y + room.height; y++) {
                    if (x >= 0 && x < levelMap.length && y >= 0 && y < levelMap[0].length) {
                        if (x == room.x || x == room.x + room.width - 1 ||
                            y == room.y || y == room.y + room.height - 1) {
                            levelMap[x][y] = 1; // Стены
                        } else {
                            levelMap[x][y] = 0; // Пол
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

                // Простая линия между центрами комнат
                int dx = endX - startX;
                int dy = endY - startY;
                int steps = Math.max(Math.abs(dx), Math.abs(dy));
                for (int i = 0; i <= steps; i++) {
                    int corridorX = startX + (dx * i) / steps;
                    int corridorY = startY + (dy * i) / steps;
                    if (corridorX >= 0 && corridorX < levelMap.length &&
                        corridorY >= 0 && corridorY < levelMap[0].length) {
                        levelMap[corridorX][corridorY] = 0; // Пол для коридора
                    }
                }
            }
        }
    }

    public int[][] getLevelMap() { return levelMap; }
    public TextureRegion getFloorRegion(int x, int y) { return floorRegion; }
    public TextureRegion getWallRegion(int x, int y) { return wallRegion[MathUtils.random(0, 9)]; }

    public void dispose() {
        wallSheet.dispose();
        floor.dispose();
    }
}
