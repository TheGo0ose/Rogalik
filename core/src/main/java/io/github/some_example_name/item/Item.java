package io.github.some_example_name.item;

/**
 * Предмет. Неизменяемый, не владеет текстурой — хранит только путь к иконке,
 * сама текстура берётся из {@link io.github.some_example_name.Assets}.
 * Создавать через {@link Items}.
 */
public final class Item {
    private final String name;
    private final String description;
    private final ItemType type;
    private final String iconPath;
    private final int damageBonus;
    private final int defenseBonus;
    private final int healthRestore;
    /** Множитель скорости: у оружия — пока оно экипировано, у зелья — на {@link #effectDuration} секунд. */
    private final float speedMultiplier;
    private final float effectDuration;

    private Item(Builder b) {
        this.name = b.name;
        this.description = b.description;
        this.type = b.type;
        this.iconPath = b.iconPath;
        this.damageBonus = b.damageBonus;
        this.defenseBonus = b.defenseBonus;
        this.healthRestore = b.healthRestore;
        this.speedMultiplier = b.speedMultiplier;
        this.effectDuration = b.effectDuration;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public ItemType getType() { return type; }
    public String getIconPath() { return iconPath; }
    public int getDamageBonus() { return damageBonus; }
    public int getDefenseBonus() { return defenseBonus; }
    public int getHealthRestore() { return healthRestore; }
    public float getSpeedMultiplier() { return speedMultiplier; }
    public float getEffectDuration() { return effectDuration; }

    @Override
    public String toString() {
        return name;
    }

    static Builder builder(String name, ItemType type, String iconPath) {
        return new Builder(name, type, iconPath);
    }

    static final class Builder {
        private final String name;
        private final ItemType type;
        private final String iconPath;
        private String description = "";
        private int damageBonus;
        private int defenseBonus;
        private int healthRestore;
        private float speedMultiplier = 1f;
        private float effectDuration;

        private Builder(String name, ItemType type, String iconPath) {
            this.name = name;
            this.type = type;
            this.iconPath = iconPath;
        }

        Builder description(String value) { description = value; return this; }
        Builder damage(int value) { damageBonus = value; return this; }
        Builder defense(int value) { defenseBonus = value; return this; }
        Builder heal(int value) { healthRestore = value; return this; }
        Builder speed(float multiplier) { speedMultiplier = multiplier; return this; }
        Builder duration(float seconds) { effectDuration = seconds; return this; }

        Item build() { return new Item(this); }
    }
}
