package io.github.some_example_name.world;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LevelGeneratorTest {
    private static final int SEEDS = 300;

    @Test
    void hasExactlyOneStartAndOneBossRoom() {
        for (long seed = 0; seed < SEEDS; seed++) {
            Level level = generate(seed);
            assertEquals(1, count(level, RoomType.START), "seed " + seed);
            assertEquals(1, count(level, RoomType.BOSS), "seed " + seed);
        }
    }

    @Test
    void roomsFitInsideMapAndDoNotOverlap() {
        for (long seed = 0; seed < SEEDS; seed++) {
            Level level = generate(seed);
            List<Room> rooms = level.getRooms();
            for (Room r : rooms) {
                assertTrue(r.getX() >= 1 && r.getY() >= 1, "seed " + seed);
                assertTrue(r.getX() + r.getWidth() < level.getWidth(), "seed " + seed);
                assertTrue(r.getY() + r.getHeight() < level.getHeight(), "seed " + seed);
            }
            for (int i = 0; i < rooms.size(); i++) {
                for (int j = i + 1; j < rooms.size(); j++) {
                    assertFalse(rooms.get(i).overlaps(rooms.get(j), 0), "seed " + seed);
                }
            }
        }
    }

    @Test
    void floorIsAlwaysEnclosedByWalls() {
        for (long seed = 0; seed < SEEDS; seed++) {
            Level level = generate(seed);
            for (int x = 0; x < level.getWidth(); x++) {
                for (int y = 0; y < level.getHeight(); y++) {
                    if (level.getTile(x, y) != Tile.FLOOR) continue;
                    for (int dx = -1; dx <= 1; dx++) {
                        for (int dy = -1; dy <= 1; dy++) {
                            assertNotEquals(Tile.VOID, level.getTile(x + dx, y + dy),
                                "seed " + seed + ": пол без стены у (" + x + "," + y + ")");
                        }
                    }
                }
            }
        }
    }

    @Test
    void everyRoomIsReachableFromStart() {
        for (long seed = 0; seed < SEEDS; seed++) {
            Level level = generate(seed);
            boolean[][] reached = floodFill(level, level.getStartRoom());
            for (Room room : level.getRooms()) {
                assertTrue(reached[room.getCenterX()][room.getCenterY()],
                    "seed " + seed + ": недостижима комната " + room.getType());
            }
        }
    }

    private static Level generate(long seed) {
        return new LevelGenerator(new Random(seed)).generate();
    }

    private static long count(Level level, RoomType type) {
        return level.getRooms().stream().filter(r -> r.getType() == type).count();
    }

    /** Обход по 4 направлениям — так же ходит игрок (диагональные щели непроходимы). */
    private static boolean[][] floodFill(Level level, Room from) {
        boolean[][] seen = new boolean[level.getWidth()][level.getHeight()];
        Deque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{from.getCenterX(), from.getCenterY()});
        seen[from.getCenterX()][from.getCenterY()] = true;
        int[][] steps = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        while (!queue.isEmpty()) {
            int[] p = queue.poll();
            for (int[] s : steps) {
                int nx = p[0] + s[0];
                int ny = p[1] + s[1];
                if (level.isWalkable(nx, ny) && !seen[nx][ny]) {
                    seen[nx][ny] = true;
                    queue.add(new int[]{nx, ny});
                }
            }
        }
        return seen;
    }
}
