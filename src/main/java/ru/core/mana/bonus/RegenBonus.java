package ru.core.mana.bonus;

import org.bukkit.entity.Player;
import ru.core.mana.config.ManaConfig;
import ru.core.mana.state.ManaState;

/**
 * Общий контракт одного независимого бонуса регенерации.
 * Используется RegenBonusRegistry и ManaService; новый бонус создаётся
 * отдельным классом и регистрируется в реестре.
 */
public interface RegenBonus {
    String key();
    boolean isActive(Player player, ManaState state);
    double amount(ManaConfig config);
}