package io.github.some_example_name.render;

import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.some_example_name.Assets;
import io.github.some_example_name.entity.Direction;
import io.github.some_example_name.entity.Enemy;
import io.github.some_example_name.entity.Player;

import java.util.Map;

/**
 * Нарезка спрайт-листов на анимации. Координаты подобраны по фактическому
 * расположению персонажей на листах (ноги каждого кадра — на одной линии).
 */
public final class Sprites {
    public final DirectionalAnimation playerWalk;
    public final DirectionalAnimation playerAttack;
    public final DirectionalAnimation slimeWalk;
    public final DirectionalAnimation slimeAttack;
    public final DirectionalAnimation slimeHurt;
    public final DirectionalAnimation slimeDeath;

    public Sprites(Assets assets) {
        // player2.png: 8 кадров по 48px; строки — вниз, влево, вверх, вправо
        playerWalk = DirectionalAnimation.fromSheet(assets.texture(Assets.PLAYER_WALK),
            Map.of(Direction.DOWN, 12, Direction.LEFT, 72, Direction.UP, 136, Direction.RIGHT, 196),
            0, 48, 8, 48, 52, 0.1f, PlayMode.LOOP, 24, 8);

        // player_attack.png: 8 кадров по 64px со сдвигом 3px; строки — вниз, влево, вправо, вверх
        playerAttack = DirectionalAnimation.fromSheet(assets.texture(Assets.PLAYER_ATTACK),
            Map.of(Direction.DOWN, 11, Direction.LEFT, 71, Direction.RIGHT, 134, Direction.UP, 197),
            3, 64, 8, 64, 52, Player.ATTACK_DURATION / 8, PlayMode.NORMAL, 32, 8);

        // Листы слизня: кадры 64×64; строки — вниз, вверх, влево, вправо
        Map<Direction, Integer> slimeRows =
            Map.of(Direction.DOWN, 0, Direction.UP, 64, Direction.LEFT, 128, Direction.RIGHT, 192);
        slimeWalk = slime(assets, Assets.SLIME_WALK, slimeRows, 8, 0.1f, PlayMode.LOOP);
        slimeAttack = slime(assets, Assets.SLIME_ATTACK, slimeRows, 10, Enemy.ATTACK_DURATION / 10, PlayMode.NORMAL);
        slimeHurt = slime(assets, Assets.SLIME_HURT, slimeRows, 5, Enemy.HURT_DURATION / 5, PlayMode.NORMAL);
        slimeDeath = slime(assets, Assets.SLIME_DEATH, slimeRows, 10, Enemy.DEATH_DURATION / 10, PlayMode.NORMAL);
    }

    private static DirectionalAnimation slime(Assets assets, String sheet, Map<Direction, Integer> rows,
                                              int frames, float frameDuration, PlayMode mode) {
        return DirectionalAnimation.fromSheet(assets.texture(sheet), rows,
            0, 64, frames, 64, 64, frameDuration, mode, 32, 22);
    }

    /** Кадр игрока для портрета в инвентаре. */
    public TextureRegion playerPortrait() {
        return playerWalk.frame(Direction.DOWN, 0);
    }
}
