package io.github.some_example_name;

import com.badlogic.gdx.graphics.Texture;

public class Item {
    public enum ItemType { WEAPON, ARMOR, POTION }

    private String name;
    private String description;
    private ItemType type;
    private int damageBonus;       // Бонус к урону
    private int defenseBonus;      // Бонус к защите
    private int healthRestore;     // Восстановление здоровья (для зелий)
    private float speedMultiplier; // Множитель скорости (для эффектов)
    private float effectDuration;  // Длительность эффекта
    private Texture texture;       // Текстура предмета

    // Конструктор для оружия
    public Item(String name, String description, ItemType type, Integer healthRestore, float speedMultiplier, float effectDuration, String texturePath) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.healthRestore = healthRestore != null ? healthRestore : 0; // Обработка null
        this.speedMultiplier = speedMultiplier;
        this.effectDuration = effectDuration;
        this.texture = new Texture(texturePath);
    }

    // Конструктор для брони
    public Item(String name, String description, ItemType type, int defenseBonus, String texturePath) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.defenseBonus = defenseBonus;
        this.speedMultiplier = 0;
        this.effectDuration = 0;
        this.texture = new Texture(texturePath);
    }

    // Конструктор для зелий (исправлен)
    public Item(String name, String description, ItemType type, int healthRestore, float speedMultiplier, float effectDuration, String texturePath) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.healthRestore = healthRestore; // Исправлено: добавлено присваивание
        this.speedMultiplier = speedMultiplier;
        this.effectDuration = effectDuration;
        this.texture = new Texture(texturePath);
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public ItemType getType() { return type; }
    public int getDamageBonus() { return damageBonus; }
    public int getDefenseBonus() { return defenseBonus; }
    public int getHealthRestore() { return healthRestore; }
    public float getSpeedMultiplier() { return speedMultiplier; }
    public float getEffectDuration() { return effectDuration; }
    public Texture getTexture() { return texture; }

    public void dispose() {
        texture.dispose();
    }
}
