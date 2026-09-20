# Mana

Плагин для Paper 1.21.11 и Java 21. Максимальная мана и базовая регенерация
получаются только из активных permission nodes. Все значения находятся в
`config.yml`, тексты — в `messages.yml`.

## Сборка

```bash
./gradlew shadowJar
```

Готовый файл: `build/libs/Mana-1.0.0.jar`.

Плагин работает без GSit и LuckPerms. Если GSit не найден, `sit-lay` использует
Paper fallback (vehicle или поза sleeping). Если LuckPerms не найден, разрешения
обновляются по таймеру. При наличии LuckPerms подключается
`UserDataRecalculateEvent` без compileOnly-зависимости.

## Основная логика

- Начальная мана — `mana.initial`, по умолчанию `0.0`.
- Текущая мана хранится в PDC игрока, загружается при входе и сохраняется при
  выходе и через `mana.save-seconds`.
- Полоска показывается только при максимуме больше нуля.
- Максимум — сумма всех активных `mana.give.<key>` из `permissions.max`.
- Реген — сумма всех активных `mana.regen.<key>`, включённых бонусов и порога.
- `regen.require-activity: true` отключает permission-реген без активного
  бонуса позы: sneak-still, sit-lay, sneak-moving или standing-still.
- При уменьшении максимума мана ограничивается новым максимумом.

Событие `ManaChangeEvent` публикуется для изменений через API и команды, но не
для обычной регенерации. Оно отменяемое.

## Permission nodes

Из настроек по умолчанию:

```text
mana.give.novice   +100 max mana
mana.give.mage     +300 max mana
mana.give.archmage +1000 max mana
mana.regen.novice  +1.0 mana/sec
mana.regen.mage    +2.5 mana/sec
```

Значения складываются. Проверяются `Player#getEffectivePermissions()` с
`value == true`, затем результат кэшируется на игрока.

## Бонусы регенерации

Каждый бонус — отдельный класс, реализующий `RegenBonus`, и регистрируется в
`RegenBonusRegistry`:

- `sneak-still` — shift без движения;
- `sit-lay` — GSit или fallback;
- `sneak-moving` — shift во время движения;
- `standing-still` — стоит без движения;
- `soft-surface` — кровать или шерсть;
- `full-food` — совпадение с `food-level`;
- `health-range` — совпадение с `health`.

Числовые выражения поддерживают `8`, `>=8`, `<=8`, `>8`, `<8`, `8..12`.
На Paper с `PlayerInputEvent` движение читается по клавишам; на API без этого
события используется fallback по смещению позиции.
Порог выбирается по наибольшему `at`, не превышающему текущую ману. В режиме
`bonus` он прибавляется к регену; в режиме `replace-base` заменяет только базу
из разрешений.

## Команды

```text
/mana
/mana <игрок>
/mana set <игрок> <число>
/mana add <игрок> <число>
/mana take <игрок> <число>
/mana debug [игрок]
/mana reload
```

Права: `mana.view`, `mana.view.others`, `mana.admin`.

## API

`ManaAPI` регистрируется через Bukkit `ServicesManager`:

```java
RegisteredServiceProvider<ManaAPI> registration =
    Bukkit.getServicesManager().getRegistration(ManaAPI.class);
ManaAPI mana = registration.getProvider();
double current = mana.getMana(player);
boolean enough = mana.tryConsume(player, 25.0);
mana.addMaxModifier(player, "my-plugin", 100.0);
```

Доступны `getMana`, `getMaxMana`, `getRegenPerSecond`, `setMana`, `addMana`,
`tryConsume`, а также add/remove модификаторов максимума и регена. Модификаторы
живут в памяти и снимаются при выходе игрока.

## Добавление нового бонуса регенерации

1. Создайте класс в `ru.core.mana.bonus`, реализующий `RegenBonus`.
2. Верните уникальный ключ из `key()`.
3. В `isActive()` проверьте состояние игрока и `ManaState`.
4. В `amount()` прочитайте `config.bonus(key()).amount()`.
5. Добавьте экземпляр в `RegenBonusRegistry`.
6. Добавьте блок `enabled` и `amount` в `config.yml`.
7. Если условие числовое, используйте `NumberMatcher`.
8. Соберите плагин и проверьте `/mana debug`.

## Resourcepack

В `resourcepack/` находится `pack.mcmeta` с `pack_format: 75`,
`assets/core/font/mana.json` и README по трём отсутствующим PNG-текстурам.
Текстуры не включены намеренно: они создаются автором ресурс-пака. Цвет HUD
задаётся цветом Adventure-компонента по пяти диапазонам запаса маны.