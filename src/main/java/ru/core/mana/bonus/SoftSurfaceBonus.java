package ru.core.mana.bonus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import ru.core.mana.config.ManaConfig;
import ru.core.mana.state.ManaState;

import java.util.List;

/** Даёт бонус на настроенной мягкой поверхности. Настройка находится в bonuses.soft-surface. */
public final class SoftSurfaceBonus implements RegenBonus {
    private List<String> configuredBlocks = List.of("#beds", "#wool");
    public String key() { return "soft-surface"; }
    public boolean isActive(Player player, ManaState state) {
        Material material = player.getLocation().subtract(0, 1, 0).getBlock().getType();
        String name = material.name().toLowerCase();
        return configuredBlocks.stream().anyMatch(value ->
                "#beds".equalsIgnoreCase(value) && name.endsWith("_bed")
                        || "#wool".equalsIgnoreCase(value) && name.endsWith("_wool")
                        || name.equalsIgnoreCase(value));
    }
    public double amount(ManaConfig config) {
        configuredBlocks = config.bonus(key()).blocks();
        return config.bonus(key()).amount();
    }
}