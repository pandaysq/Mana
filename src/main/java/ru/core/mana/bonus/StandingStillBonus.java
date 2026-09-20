package ru.core.mana.bonus;

import org.bukkit.entity.Player;
import ru.core.mana.config.ManaConfig;
import ru.core.mana.state.ManaState;

/** Даёт бонус стоящему без движения игроку. Настройка находится в bonuses.standing-still. */
public final class StandingStillBonus implements RegenBonus {
    public String key() { return "standing-still"; }
    public boolean isActive(Player player, ManaState state) {
        return !player.isSneaking() && !state.moving() && !player.isFlying();
    }
    public double amount(ManaConfig config) { return config.bonus(key()).amount(); }
}