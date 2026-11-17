package io.github.some_example_name.world;

/** Тип клетки карты. */
public enum Tile {
    /** Пустота за пределами подземелья. Непроходима. */
    VOID(false),
    FLOOR(true),
    WALL(false);

    private final boolean walkable;

    Tile(boolean walkable) {
        this.walkable = walkable;
    }

    public boolean isWalkable() {
        return walkable;
    }
}
