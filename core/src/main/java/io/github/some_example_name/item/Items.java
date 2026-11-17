package io.github.some_example_name.item;

/** Каталог всех предметов игры. Каждый вызов создаёт новый экземпляр. */
public final class Items {
    public static final String ICON_IRON_SWORD = "base_sword.png";
    public static final String ICON_RARE_SWORD = "rare_sword.png";
    public static final String ICON_MYTH_SWORD = "myth_sword.png";
    public static final String ICON_HEALING_POTION = "healing_potion.png";

    /** Все иконки — чтобы {@link io.github.some_example_name.Assets} загрузил их заранее. */
    public static final String[] ALL_ICONS = {
        ICON_IRON_SWORD, ICON_RARE_SWORD, ICON_MYTH_SWORD, ICON_HEALING_POTION
    };

    private Items() {
    }

    public static Item ironSword() {
        return Item.builder("Железный меч", ItemType.WEAPON, ICON_IRON_SWORD)
            .description("Меч, который даётся всем воинам перед войной")
            .damage(5)
            .build();
    }

    public static Item rareSword() {
        return Item.builder("Редкий меч", ItemType.WEAPON, ICON_RARE_SWORD)
            .description("Прочный и острый")
            .damage(10)
            .build();
    }

    public static Item mythSword() {
        return Item.builder("Мифический меч", ItemType.WEAPON, ICON_MYTH_SWORD)
            .description("Ускоряет владельца в 1.5 раза")
            .damage(15)
            .speed(1.5f)
            .build();
    }

    public static Item healingPotion() {
        return Item.builder("Зелье лечения", ItemType.POTION, ICON_HEALING_POTION)
            .description("Восстанавливает 20 здоровья")
            .heal(20)
            .build();
    }
}
