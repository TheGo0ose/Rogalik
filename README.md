# Rogalik

Пиксельный рогалик на [libGDX](https://libgdx.com/): процедурное подземелье, слизни, инвентарь и экипировка.

## Запуск

Нужен JDK 23 (проект собирается с `release 23`).

```bash
./gradlew lwjgl3:run      # запустить игру
./gradlew core:test       # тесты логики
./gradlew lwjgl3:jar      # собрать запускаемый jar в lwjgl3/build/libs
./gradlew lwjgl3:exeToRoot  # собрать Rogalik.exe (со встроенной Java) прямо в корень проекта
```

`Rogalik.exe` запускается только вместе с лежащими рядом `runtime/`, `app/` и `Rogalik-*.jar`.
Чтобы передать игру, отправьте zip из `lwjgl3/build/construo/dist/`.

Если по умолчанию стоит другая версия Java, укажите нужную: `JAVA_HOME=путь/к/jdk-23 ./gradlew lwjgl3:run`.

## Управление

| Клавиша | Действие |
|---|---|
| WASD / стрелки | ходьба |
| Пробел | удар мечом |
| I | инвентарь (ЛКМ по предмету — использовать/надеть, по слоту экипировки — снять) |
| Esc | пауза (M — выйти в меню) |
| R / Esc на экране смерти | новая игра / меню |

## Архитектура

Логика игры отделена от графики, звука и ввода: классы из `world`, `entity` и `item`
не используют текстуры и `Gdx.input`, поэтому покрыты обычными JUnit-тестами.

```
core/src/main/java/io/github/some_example_name/
├── MainGame.java          точка входа: владеет общими ресурсами и переключает экраны
├── Assets.java            загрузка всех текстур/звуков через AssetManager (каждая — один раз)
├── audio/AudioManager     музыка, клики, громкость (сохраняется между запусками)
├── screen/                MenuScreen, SettingsScreen, GameScreen, DeathScreen
├── world/                 Level (клетки и комнаты), LevelGenerator, GameWorld (правила забега)
├── entity/                Player, Enemy (состояния и бой), Direction, PlayerInput
├── item/                  Item (неизменяемый), Items (каталог), Inventory
├── render/                WorldRenderer, Sprites (нарезка листов), DirectionalAnimation
└── ui/                    Hud, InventoryWindow, UiStyles (шрифты с кириллицей)
```

Поток кадра в `GameScreen`: клавиатура → `PlayerInput` → `GameWorld.update()` → `WorldRenderer` → `Hud` / `InventoryWindow`.

Как добавить:
- **предмет** — фабричный метод в `Items` и иконку в `Items.ALL_ICONS`;
- **врага** — класс в `entity/`, спавн в `GameWorld.spawnEnemies()`, анимации в `Sprites`, отрисовку в `WorldRenderer`;
- **тип комнаты** — значение в `RoomType` и его обработку в `LevelGenerator` / `GameWorld`.

## Ресурсы

Шрифт [Press Start 2P](https://fonts.google.com/specimen/Press+Start+2P) — лицензия SIL OFL 1.1 (`assets/fonts/OFL.txt`).
