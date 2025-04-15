package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Level {
    public static final int TILE_SIZE = 64;
    private final int[][] levelMap;
    private final Texture wallSheet;
    private final TextureRegion[] wallRegion;
    private final TextureRegion floorRegion;

    public Level() {
        levelMap = new int[20][15];
        wallSheet = new Texture("wall.png");

        wallRegion = new TextureRegion[5];
        for (int i = 0; i < 5; i++) {
            wallRegion[i] = new TextureRegion(wallSheet, i * 64, 0, 64, 64);
        }
        floorRegion = new TextureRegion(wallSheet, 0, 384, 64, 64);
    }

    public void generateLevel() {
        for (int x = 0; x < 20; x++) {
            for (int y = 0; y < 15; y++) {
                levelMap[x][y] = 0;
            }
        }

        for (int x = 0; x < 20; x++) {
            levelMap[x][0] = 1;
            levelMap[x][14] = 1;
        }
        for (int y = 0; y < 15; y++) {
            levelMap[0][y] = 1;
            levelMap[19][y] = 1;
        }

        generateRoom(2, 2, 5, 5);
        generateRoom(8, 8, 5, 5);
        generateRoom(14, 2, 4, 4);

        connectRooms(4, 4, 10, 10);
        connectRooms(10, 10, 16, 4);

        levelMap[3][3] = 0;
    }

    private void generateRoom(int startX, int startY, int width, int height) {
        for (int x = startX; x < startX + width; x++) {
            for (int y = startY; y < startY + height; y++) {
                if (x == startX || x == startX + width - 1 || y == startY || y == startY + height - 1) {
                    levelMap[x][y] = 1;
                } else {
                    levelMap[x][y] = 0;
                }
            }
        }
    }

    private void connectRooms(int x1, int y1, int x2, int y2) {
        int startX = Math.min(x1, x2);
        int endX = Math.max(x1, x2);
        for (int x = startX; x <= endX; x++) {
            levelMap[x][y1] = 0;
        }

        int startY = Math.min(y1, y2);
        int endY = Math.max(y1, y2);
        for (int y = startY; y <= endY; y++) {
            levelMap[x2][y] = 0;
        }
    }

    public int[][] getLevelMap() { return levelMap; }
    public TextureRegion getFloorRegion() { return floorRegion; }
    public TextureRegion getWallRegion(int x, int y) {
        boolean top = y < 14 && levelMap[x][y + 1] == 1;
        boolean bottom = y > 0 && levelMap[x][y - 1] == 1;
        boolean left = x > 0 && levelMap[x - 1][y] == 1;
        boolean right = x < 19 && levelMap[x + 1][y] == 1;

        if (top && bottom && left && right) return wallRegion[0];
        else if (top && bottom && left) return wallRegion[1];
        else if (top && bottom && right) return wallRegion[2];
        else if (top && bottom) return wallRegion[3];
        else return wallRegion[4];
    }

    public void dispose() {
        wallSheet.dispose();
    }
}
