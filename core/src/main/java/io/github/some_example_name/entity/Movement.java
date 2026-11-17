package io.github.some_example_name.entity;

/**
 * Перемещение с отдельной проверкой осей: персонаж скользит вдоль стены, а не застревает.
 * При упоре в препятствие шаг уменьшается, чтобы подойти к стене вплотную.
 */
final class Movement {
    /** Может ли хитбокс стоять в точке (x, y). */
    interface Occupancy {
        boolean canOccupy(float x, float y);
    }

    private static final int REFINE_STEPS = 5;

    private Movement() {
    }

    static float moveX(float x, float y, float dx, Occupancy occupancy) {
        float step = dx;
        for (int i = 0; i < REFINE_STEPS && step != 0; i++) {
            if (occupancy.canOccupy(x + step, y)) return x + step;
            step *= 0.5f;
        }
        return x;
    }

    static float moveY(float x, float y, float dy, Occupancy occupancy) {
        float step = dy;
        for (int i = 0; i < REFINE_STEPS && step != 0; i++) {
            if (occupancy.canOccupy(x, y + step)) return y + step;
            step *= 0.5f;
        }
        return y;
    }
}
