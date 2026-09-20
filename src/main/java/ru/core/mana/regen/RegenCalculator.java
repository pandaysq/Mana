package ru.core.mana.regen;

import org.bukkit.entity.Player;
import ru.core.mana.bonus.RegenBonusRegistry;
import ru.core.mana.config.ManaConfig;
import ru.core.mana.state.ManaState;

import java.util.List;

/**
 * Считает итоговую скорость регенерации как сумму базы, всех активных бонусов
 * и порога. Используется ManaService и /mana debug; новый источник регена
 * добавляется в Calculation или RegenBonusRegistry.
 */
public final class RegenCalculator {
    private final ManaConfig config;
    private final RegenBonusRegistry bonuses;

    public RegenCalculator(ManaConfig config, RegenBonusRegistry bonuses) {
        this.config = config;
        this.bonuses = bonuses;
    }

    public Calculation calculate(Player player, ManaState state) {
        List<RegenBonusRegistry.ActiveBonus> active = bonuses.active(player, state);
        double bonusTotal = active.stream().mapToDouble(RegenBonusRegistry.ActiveBonus::amount).sum();
        boolean poseActivity = active.stream().anyMatch(value -> value.key().equals("sneak-still")
                || value.key().equals("sit-lay") || value.key().equals("sneak-moving")
                || value.key().equals("standing-still"));
        double permissionBase = config.requireActivity() && !poseActivity ? 0.0 : state.regenFromPermissions();
        ThresholdMatch threshold = threshold(state.mana());
        double base = "replace-base".equals(config.thresholdMode()) && threshold != null
                ? threshold.amount() : permissionBase;
        double thresholdAmount = "replace-base".equals(config.thresholdMode()) ? 0.0
                : threshold == null ? 0.0 : threshold.amount();
        return new Calculation(permissionBase, active, threshold, bonusTotal,
                base + bonusTotal + thresholdAmount, poseActivity);
    }

    private ThresholdMatch threshold(double mana) {
        if (!config.thresholdsEnabled()) return null;
        ManaConfig.Threshold selected = null;
        for (ManaConfig.Threshold candidate : config.thresholds()) {
            if (candidate.at() <= mana && (selected == null || candidate.at() > selected.at())) selected = candidate;
        }
        return selected == null ? null : new ThresholdMatch(selected.at(), selected.amount());
    }

    public record ThresholdMatch(double at, double amount) {}
    public record Calculation(double permissionBase,
                              List<RegenBonusRegistry.ActiveBonus> bonuses,
                              ThresholdMatch threshold,
                              double bonusTotal,
                              double total,
                              boolean poseActivity) {}
}