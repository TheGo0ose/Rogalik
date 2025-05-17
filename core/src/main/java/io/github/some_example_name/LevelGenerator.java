package io.github.some_example_name;

import com.badlogic.gdx.math.MathUtils;
import java.util.ArrayList;
import java.util.List;

enum RoomType {
    NORMAL, CHEST, BOSS
}

class Room {
    public int x, y, width, height;
    public RoomType type;
    public List<Room> neighbors = new ArrayList<>();

    public Room(int x, int y, int width, int height, RoomType type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
    }

    public void addNeighbor(Room neighbor) {
        if (!neighbors.contains(neighbor)) {
            neighbors.add(neighbor);
        }
    }
}

public class LevelGenerator {
    private static final int MAP_WIDTH = 60;
    private static final int MAP_HEIGHT = 15;
    private static final int MAIN_PATH_LENGTH = 5;

    public List<Room> generateLevel() {
        List<Room> rooms = new ArrayList<>();
        int currentX = 2;
        int currentY = MAP_HEIGHT / 2;
        Room previousRoom = null;

        // Главная ветка
        for (int i = 0; i < MAIN_PATH_LENGTH; i++) {
            int size = MathUtils.random(4, 8);
            Room room = new Room(currentX, currentY, size, size, RoomType.NORMAL);
            if (previousRoom != null) {
                connectRooms(previousRoom, room);
            }
            rooms.add(room);
            previousRoom = room;
            currentX += size + 4; // Сдвиг с учётом размера и коридора
        }

        // Комната с боссом
        Room bossRoom = new Room(currentX, currentY, 8, 8, RoomType.BOSS);
        connectRooms(previousRoom, bossRoom);
        rooms.add(bossRoom);

        // Боковые ответвления
        for (Room room : new ArrayList<>(rooms)) {
            if (room.type != RoomType.BOSS) {
                int numBranches = MathUtils.random(0, 2);
                for (int j = 0; j < numBranches; j++) {
                    Room branchRoom = createBranchRoom(room);
                    if (branchRoom != null) {
                        connectRooms(room, branchRoom);
                        rooms.add(branchRoom);
                    }
                }
            }
        }

        return rooms;
    }

    private Room createBranchRoom(Room fromRoom) {
        int direction = MathUtils.random(0, 3);
        int size = MathUtils.random(4, 6);
        int branchX = fromRoom.x;
        int branchY = fromRoom.y;

        switch (direction) {
            case 0: // Север
                branchY += fromRoom.height + 2;
                break;
            case 1: // Юг
                branchY -= size + 2;
                break;
            case 2: // Запад
                branchX -= size + 2;
                break;
            case 3: // Восток
                branchX += fromRoom.width + 2;
                break;
        }

        if (branchX < 0 || branchX + size > MAP_WIDTH || branchY < 0 || branchY + size > MAP_HEIGHT) {
            return null;
        }

        RoomType type = MathUtils.randomBoolean() ? RoomType.NORMAL : RoomType.CHEST;
        return new Room(branchX, branchY, size, size, type);
    }

    private void connectRooms(Room room1, Room room2) {
        room1.addNeighbor(room2);
        room2.addNeighbor(room1);
        // Здесь можно добавить логику для точного позиционирования выхода (например, через центр или край)
    }
}
