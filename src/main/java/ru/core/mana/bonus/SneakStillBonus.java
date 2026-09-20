package ru.core.mana.bonus;

import org.bukkit.entity.Player;
import ru.core.mana.config.ManaConfig;
import ru.core.mana.state.ManaState;

/** Даёт бонус, когда игрок крадётся и не движется. Добавлять настройку в bonuses.sneak-still. */
public final class SneakStillBonus implements RegenBonus {
    public String key() { return "sneak-still"; }
    public boolean isActive(Player player, ManaState state) { return player.isSneaking() && !state.moving(); }
    public double amount(ManaConfig config) { return config.bonus(key()).amount(); }
}