package io.github.some_example_name.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.some_example_name.Assets;
import io.github.some_example_name.audio.AudioManager;
import io.github.some_example_name.entity.Player;
import io.github.some_example_name.item.Inventory;
import io.github.some_example_name.item.Item;

import java.util.List;
import java.util.function.Consumer;

/**
 * Окно инвентаря поверх Inventory.png: слоты экипировки слева, рюкзак 6×3 справа.
 * Клик по предмету в рюкзаке — использовать/экипировать, по слоту экипировки — снять.
 */
public final class InventoryWindow implements Disposable {
    private static final float SCALE = 3f;
    /** Разметка Inventory.png в пикселях исходника (от левого верхнего угла). */
    private static final int SLOT_PITCH = 19;
    private static final int SLOT_SIZE = 18;
    private static final int GRID_X = 82;
    private static final int GRID_Y = 8;
    private static final int GRID_COLUMNS = 6;
    private static final int GRID_ROWS = 3;
    private static final int ICON_SIZE = 48;

    private final Stage stage;
    private final Group window = new Group();
    private final Player player;
    private final Assets assets;
    private final AudioManager audio;
    private final Consumer<String> notifier;
    private final int imageHeight;

    private final Slot[] gridSlots = new Slot[GRID_COLUMNS * GRID_ROWS];
    private final Slot weaponSlot;
    private final Slot armorSlot;
    private final Label statsLabel;
    private final Label tooltip;
    private boolean visible;

    public InventoryWindow(Batch batch, Assets assets, UiStyles ui, AudioManager audio,
                           Player player, TextureRegion portrait, Consumer<String> notifier) {
        if (gridSlots.length != Inventory.CAPACITY) {
            throw new IllegalStateException("Число ячеек окна не совпадает с вместимостью инвентаря");
        }
        this.stage = new Stage(new ScreenViewport(), batch);
        this.player = player;
        this.assets = assets;
        this.audio = audio;
        this.notifier = notifier;

        Texture background = assets.texture(Assets.INVENTORY);
        imageHeight = background.getHeight();
        window.setSize(background.getWidth() * SCALE, background.getHeight() * SCALE);
        Image backgroundImage = new Image(background);
        backgroundImage.setSize(window.getWidth(), window.getHeight());
        window.addActor(backgroundImage);

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLUMNS; col++) {
                Slot slot = new Slot(this::onBackpackClick);
                place(slot, GRID_X + col * SLOT_PITCH, GRID_Y + row * SLOT_PITCH, SLOT_SIZE, SLOT_SIZE);
                gridSlots[row * GRID_COLUMNS + col] = slot;
            }
        }
        weaponSlot = new Slot(this::onEquipmentClick);
        place(weaponSlot, 6, 8, SLOT_SIZE, SLOT_SIZE);
        armorSlot = new Slot(this::onEquipmentClick);
        place(armorSlot, 6, 27, SLOT_SIZE, SLOT_SIZE);

        Image portraitImage = new Image(portrait);
        float portraitScale = 1.75f;
        portraitImage.setSize(portrait.getRegionWidth() * portraitScale, portrait.getRegionHeight() * portraitScale);
        // центр области портрета на картинке: x 25..52, y 8..44
        portraitImage.setPosition(39 * SCALE - portraitImage.getWidth() / 2f,
            (imageHeight - 26) * SCALE - portraitImage.getHeight() / 2f);
        portraitImage.setTouchable(Touchable.disabled);
        window.addActor(portraitImage);

        statsLabel = new Label("", ui.label);
        place(statsLabel, 9, 47, 62, 16);
        statsLabel.setAlignment(Align.left);

        tooltip = new Label("", ui.label);
        tooltip.setWrap(true);
        tooltip.setAlignment(Align.topLeft);
        tooltip.setWidth(window.getWidth());
        tooltip.setPosition(0, -16 - 100);
        tooltip.setHeight(100);
        window.addActor(tooltip);

        Label hint = new Label("I / ESC — закрыть", ui.label);
        hint.setColor(0.8f, 0.8f, 0.8f, 1f);
        hint.setPosition(0, window.getHeight() + 12);
        window.addActor(hint);

        stage.addActor(window);
        refresh();
    }

    /** Размещает актёра по координатам картинки (левый верхний угол, пиксели исходника). */
    private void place(Actor actor, int imageX, int imageTopY, int width, int height) {
        actor.setBounds(imageX * SCALE, (imageHeight - imageTopY - height) * SCALE, width * SCALE, height * SCALE);
        window.addActor(actor);
    }

    private void onBackpackClick(Item item) {
        if (player.useItem(item)) {
            audio.playClick();
        } else {
            notifier.accept("Здоровье уже полное");
        }
        refresh();
    }

    private void onEquipmentClick(Item item) {
        if (player.unequip(item)) {
            audio.playClick();
        } else {
            notifier.accept("Рюкзак полон");
        }
        refresh();
    }

    public void refresh() {
        List<Item> items = player.getInventory().getItems();
        for (int i = 0; i < gridSlots.length; i++) {
            gridSlots[i].item = i < items.size() ? items.get(i) : null;
        }
        weaponSlot.item = player.getWeapon();
        armorSlot.item = player.getArmor();
        statsLabel.setText("Урон " + player.getDamage() + "\nЗащита " + player.getDefense());
    }

    private void updateTooltip() {
        Slot hovered = null;
        for (Slot slot : gridSlots) if (slot.isHovered()) hovered = slot;
        if (weaponSlot.isHovered()) hovered = weaponSlot;
        if (armorSlot.isHovered()) hovered = armorSlot;

        if (hovered == null || hovered.item == null) {
            tooltip.setText("");
            return;
        }
        Item item = hovered.item;
        boolean equipped = hovered == weaponSlot || hovered == armorSlot;
        StringBuilder text = new StringBuilder(item.getName()).append('\n').append(item.getDescription());
        if (item.getDamageBonus() > 0) text.append("\nУрон +").append(item.getDamageBonus());
        if (item.getDefenseBonus() > 0) text.append("\nЗащита +").append(item.getDefenseBonus());
        if (item.getHealthRestore() > 0) text.append("\nЛечение +").append(item.getHealthRestore());
        String action = equipped ? "снять" : switch (item.getType()) {
            case POTION -> "выпить";
            case WEAPON, ARMOR -> "надеть";
        };
        text.append("\n[ЛКМ — ").append(action).append(']');
        tooltip.setText(text);
    }

    public void toggle() {
        setVisible(!visible);
    }

    public void setVisible(boolean value) {
        visible = value;
        if (visible) refresh();
    }

    public boolean isVisible() {
        return visible;
    }

    public Stage getStage() {
        return stage;
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        window.setPosition(Math.round((width - window.getWidth()) / 2f), Math.round((height - window.getHeight()) / 2f + 40));
    }

    public void render(float delta) {
        if (!visible) return;
        updateTooltip();
        stage.getViewport().apply();
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }

    /** Ячейка с иконкой предмета и подсветкой при наведении. */
    private final class Slot extends Actor {
        private final ClickListener listener;
        private Item item;

        Slot(Consumer<Item> onClick) {
            listener = new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (visible && item != null) onClick.accept(item);
                }
            };
            addListener(listener);
        }

        boolean isHovered() {
            return listener.isOver();
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (isHovered()) {
                batch.setColor(1f, 1f, 1f, 0.35f * parentAlpha);
                batch.draw(assets.whitePixel(), getX(), getY(), getWidth(), getHeight());
                batch.setColor(Color.WHITE);
            }
            if (item != null) {
                Texture icon = assets.texture(item.getIconPath());
                batch.draw(icon,
                    getX() + (getWidth() - ICON_SIZE) / 2f,
                    getY() + (getHeight() - ICON_SIZE) / 2f,
                    ICON_SIZE, ICON_SIZE);
            }
        }
    }
}
