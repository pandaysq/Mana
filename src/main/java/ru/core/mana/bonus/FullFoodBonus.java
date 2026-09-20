package ru.core.mana.bonus;

import org.bukkit.entity.Player;
import ru.core.mana.config.ManaConfig;
import ru.core.mana.config.NumberMatcher;
import ru.core.mana.state.ManaState;

/** Даёт бонус при совпадении food-level. Настройка и выражение находятся в bonuses.full-food. */
public final class FullFoodBonus implements RegenBonus {
    public String key() { return "full-food"; }
    public boolean isActive(Player player, ManaState state) {
        return NumberMatcher.parse(statefulExpression).matches(player.getFoodLevel());
    }
    private String statefulExpression = ">=20";
    public double amount(ManaConfig config) {
        statefulExpression = config.bonus(key()).foodLevel();
        return config.bonus(key()).amount();
    }
}