package ru.core.mana.bonus;

import org.bukkit.entity.Player;
import ru.core.mana.config.ManaConfig;
import ru.core.mana.integration.GSitHook;
import ru.core.mana.state.ManaState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Хранит все бонусы и считает их независимо, без выбора максимального значения.
 * Используется RegenCalculator; новый класс бонуса регистрируется в constructor.
 */
public final class RegenBonusRegistry {
    private final ManaConfig config;
    private final List<RegenBonus> bonuses;

    public RegenBonusRegistry(ManaConfig config, GSitHook gsit) {
        this.config = config;
        this.bonuses = List.of(new SneakStillBonus(), new SitLayBonus(gsit),
                new SneakMovingBonus(), new StandingStillBonus(),
                new SoftSurfaceBonus(), new FullFoodBonus(), new HealthRangeBonus());
    }

    public List<ActiveBonus> active(Player player, ManaState state) {
        List<ActiveBonus> result = new ArrayList<>();
        for (RegenBonus bonus : bonuses) {
            ManaConfig.BonusSettings settings = config.bonus(bonus.key());
            double amount = bonus.amount(config);
            if (settings.enabled() && bonus.isActive(player, state)) {
                result.add(new ActiveBonus(bonus.key(), amount));
            }
        }
        return result;
    }

    public List<RegenBonus> all() { return Collections.unmodifiableList(bonuses); }
    public record ActiveBonus(String key, double amount) {}
}