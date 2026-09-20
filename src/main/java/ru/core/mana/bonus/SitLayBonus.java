package ru.core.mana.bonus;

import org.bukkit.entity.Player;
import ru.core.mana.config.ManaConfig;
import ru.core.mana.integration.GSitHook;
import ru.core.mana.state.ManaState;

/** Даёт бонус за сидение или лежание через GSit либо Paper fallback. Настройка находится в bonuses.sit-lay. */
public final class SitLayBonus implements RegenBonus {
    private final GSitHook gsit;
    public SitLayBonus(GSitHook gsit) { this.gsit = gsit; }
    public String key() { return "sit-lay"; }
    public boolean isActive(Player player, ManaState state) { return gsit.isSittingOrLaying(player); }
    public double amount(ManaConfig config) { return config.bonus(key()).amount(); }
}