package io.github.some_example_name.entity;

public enum Direction {
    DOWN, UP, LEFT, RIGHT;

    /** Направление по вектору; при равенстве осей предпочитается горизонталь. */
    public static Direction fromVector(float dx, float dy) {
        if (Math.abs(dx) >= Math.abs(dy)) {
            return dx < 0 ? LEFT : RIGHT;
        }
        return dy < 0 ? DOWN : UP;
    }
}
