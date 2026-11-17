package io.github.some_example_name.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Генератор подземелья: главная ветка комнат слева направо (старт → ... → босс)
 * и боковые комнаты сверху/снизу. Размер карты подбирается под комнаты,
 * поэтому ничего не обрезается краем карты.
 */
public final class LevelGenerator {
    private static final int MAIN_PATH_ROOMS = 5; // без учёта комнаты босса
    private static final int ROOM_MIN = 5;
    private static final int ROOM_MAX = 8;
    private static final int BRANCH_MIN = 4;
    private static final int BRANCH_MAX = 6;
    private static final int BOSS_ROOM_SIZE = 9;
    private static final int MAX_BRANCHES_PER_ROOM = 2;
    private static final int BRANCH_PLACEMENT_ATTEMPTS = 6;
    /** Запас клеток по краям карты, чтобы вокруг пола всегда было место для стен. */
    private static final int BORDER = 2;

    private final Random random;

    public LevelGenerator(Random random) {
        this.random = random;
    }

    public Level generate() {
        List<Room> rooms = new ArrayList<>();
        List<Room> mainPath = new ArrayList<>();
        List<Room[]> links = new ArrayList<>();

        int x = 0;
        Room previous = null;
        for (int i = 0; i <= MAIN_PATH_ROOMS; i++) {
            boolean boss = i == MAIN_PATH_ROOMS;
            RoomType type = i == 0 ? RoomType.START : boss ? RoomType.BOSS : RoomType.NORMAL;
            int w = boss ? BOSS_ROOM_SIZE : range(ROOM_MIN, ROOM_MAX);
            int h = boss ? BOSS_ROOM_SIZE : range(ROOM_MIN, ROOM_MAX);
            int y = -h / 2 + range(-2, 2);
            Room room = new Room(x, y, w, h, type);
            rooms.add(room);
            mainPath.add(room);
            if (previous != null) link(previous, room, links);
            previous = room;
            x += w + range(3, 5);
        }

        for (Room parent : mainPath) {
            if (parent.getType() == RoomType.BOSS) continue;
            int branches = range(0, MAX_BRANCHES_PER_ROOM);
            for (int i = 0; i < branches; i++) {
                Room branch = tryPlaceBranch(parent, rooms);
                if (branch != null) {
                    rooms.add(branch);
                    link(parent, branch, links);
                }
            }
        }

        return buildLevel(rooms, links);
    }

    private Room tryPlaceBranch(Room parent, List<Room> rooms) {
        for (int attempt = 0; attempt < BRANCH_PLACEMENT_ATTEMPTS; attempt++) {
            int w = range(BRANCH_MIN, BRANCH_MAX);
            int h = range(BRANCH_MIN, BRANCH_MAX);
            int gap = range(3, 4);
            boolean north = random.nextBoolean();
            // центр боковой комнаты по X лежит внутри родителя — коридор получается прямым
            int centerX = range(parent.getX() + 1, parent.getX() + parent.getWidth() - 2);
            int bx = centerX - w / 2;
            int by = north ? parent.getY() + parent.getHeight() + gap : parent.getY() - gap - h;
            RoomType type = random.nextBoolean() ? RoomType.NORMAL : RoomType.CHEST;
            Room candidate = new Room(bx, by, w, h, type);

            boolean free = true;
            for (Room other : rooms) {
                if (candidate.overlaps(other, 2)) {
                    free = false;
                    break;
                }
            }
            if (free) return candidate;
        }
        return null;
    }

    private Level buildLevel(List<Room> rooms, List<Room[]> links) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        for (Room r : rooms) {
            minX = Math.min(minX, r.getX());
            minY = Math.min(minY, r.getY());
            maxX = Math.max(maxX, r.getX() + r.getWidth());
            maxY = Math.max(maxY, r.getY() + r.getHeight());
        }
        for (Room r : rooms) {
            r.translate(BORDER - minX, BORDER - minY);
        }
        Level level = new Level(maxX - minX + 2 * BORDER, maxY - minY + 2 * BORDER, rooms);

        for (Room r : rooms) {
            for (int tx = r.getX(); tx < r.getX() + r.getWidth(); tx++) {
                for (int ty = r.getY(); ty < r.getY() + r.getHeight(); ty++) {
                    level.setTile(tx, ty, Tile.FLOOR);
                }
            }
        }
        for (Room[] link : links) {
            carveCorridor(level, link[0], link[1]);
        }
        buildWalls(level);
        return level;
    }

    /** Г-образный коридор: по горизонтали от центра {@code a}, затем по вертикали к центру {@code b}. */
    private static void carveCorridor(Level level, Room a, Room b) {
        int ax = a.getCenterX(), ay = a.getCenterY();
        int bx = b.getCenterX(), by = b.getCenterY();
        for (int tx = Math.min(ax, bx); tx <= Math.max(ax, bx); tx++) {
            level.setTile(tx, ay, Tile.FLOOR);
        }
        for (int ty = Math.min(ay, by); ty <= Math.max(ay, by); ty++) {
            level.setTile(bx, ty, Tile.FLOOR);
        }
    }

    /** Любая пустая клетка, соседняя с полом (включая диагональ), становится стеной. */
    private static void buildWalls(Level level) {
        for (int tx = 0; tx < level.getWidth(); tx++) {
            for (int ty = 0; ty < level.getHeight(); ty++) {
                if (level.getTile(tx, ty) != Tile.VOID) continue;
                if (hasFloorNeighbor(level, tx, ty)) {
                    level.setTile(tx, ty, Tile.WALL);
                }
            }
        }
    }

    private static boolean hasFloorNeighbor(Level level, int tx, int ty) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if ((dx != 0 || dy != 0) && level.getTile(tx + dx, ty + dy) == Tile.FLOOR) return true;
            }
        }
        return false;
    }

    private static void link(Room a, Room b, List<Room[]> links) {
        a.connect(b);
        links.add(new Room[]{a, b});
    }

    /** Случайное целое в диапазоне [min, max] включительно. */
    private int range(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }
}
