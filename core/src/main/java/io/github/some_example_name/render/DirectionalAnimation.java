package io.github.some_example_name.render;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import io.github.some_example_name.entity.Direction;

import java.util.EnumMap;
import java.util.Map;

/**
 * Анимация с отдельной строкой спрайт-листа для каждого направления.
 * Кадр рисуется так, что «якорь» (точка между ног в пикселях кадра) совпадает
 * с позицией сущности, — поэтому разные анимации не дёргаются при смене.
 */
public final class DirectionalAnimation {
    private final Map<Direction, Animation<TextureRegion>> animations = new EnumMap<>(Direction.class);
    private final float anchorX;
    private final float anchorY;

    private DirectionalAnimation(float anchorX, float anchorY) {
        this.anchorX = anchorX;
        this.anchorY = anchorY;
    }

    /**
     * @param rowTops    Y верхнего края кадров для каждого направления
     * @param firstX     X первого кадра в строке
     * @param pitch      шаг между кадрами по X
     * @param anchorX    X якоря внутри кадра (от левого края)
     * @param anchorY    Y якоря внутри кадра (от нижнего края)
     */
    public static DirectionalAnimation fromSheet(Texture sheet, Map<Direction, Integer> rowTops,
                                                 int firstX, int pitch, int frameCount,
                                                 int frameWidth, int frameHeight,
                                                 float frameDuration, Animation.PlayMode mode,
                                                 float anchorX, float anchorY) {
        DirectionalAnimation result = new DirectionalAnimation(anchorX, anchorY);
        for (Map.Entry<Direction, Integer> row : rowTops.entrySet()) {
            Array<TextureRegion> frames = new Array<>(frameCount);
            for (int i = 0; i < frameCount; i++) {
                frames.add(new TextureRegion(sheet, firstX + i * pitch, row.getValue(), frameWidth, frameHeight));
            }
            result.animations.put(row.getKey(), new Animation<>(frameDuration, frames, mode));
        }
        return result;
    }

    public TextureRegion frame(Direction direction, float time) {
        return animations.get(direction).getKeyFrame(time);
    }

    public void draw(Batch batch, Direction direction, float time, float footX, float footY, float scale) {
        TextureRegion region = frame(direction, time);
        batch.draw(region,
            footX - anchorX * scale,
            footY - anchorY * scale,
            region.getRegionWidth() * scale,
            region.getRegionHeight() * scale);
    }
}
