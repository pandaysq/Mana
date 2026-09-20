package ru.core.mana.bonus;

import org.bukkit.entity.Player;
import ru.core.mana.config.ManaConfig;
import ru.core.mana.config.NumberMatcher;
import ru.core.mana.state.ManaState;

/** Даёт бонус при совпадении текущего здоровья. Настройка находится в bonuses.health-range. */
public final class HealthRangeBonus implements RegenBonus {
    public String key() { return "health-range"; }
    public boolean isActive(Player player, ManaState state) {
        return NumberMatcher.parse(statefulExpression).matches(player.getHealth());
    }
    private String statefulExpression = ">=0";
    public double amount(ManaConfig config) {
        statefulExpression = config.bonus(key()).health();
        return config.bonus(key()).amount();
    }
}