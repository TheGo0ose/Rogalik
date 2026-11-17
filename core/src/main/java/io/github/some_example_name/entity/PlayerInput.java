package io.github.some_example_name.entity;

/**
 * Команды игрока за один кадр. Отделяет логику игрока от клавиатуры:
 * экран читает клавиши и передаёт в мир уже готовые намерения.
 *
 * @param moveX  -1..1 по горизонтали
 * @param moveY  -1..1 по вертикали
 * @param attack нажата ли атака в этом кадре
 */
public record PlayerInput(float moveX, float moveY, boolean attack) {
    public static final PlayerInput NONE = new PlayerInput(0, 0, false);
}
