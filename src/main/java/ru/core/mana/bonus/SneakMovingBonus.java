package ru.core.mana.bonus;

import org.bukkit.entity.Player;
import ru.core.mana.config.ManaConfig;
import ru.core.mana.state.ManaState;

/** Даёт бонус за движение в приседе. Настройка находится в bonuses.sneak-moving. */
public final class SneakMovingBonus implements RegenBonus {
    public String key() { return "sneak-moving"; }
    public boolean isActive(Player player, ManaState state) { return player.isSneaking() && state.moving(); }
    public double amount(ManaConfig config) { return config.bonus(key()).amount(); }
}