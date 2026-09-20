package ru.core.mana.integration;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;

/**
 * Опционально вызывает GSit API через reflection, не добавляя compileOnly jar.
 * Используется SitLayBonus; при отсутствии GSit применяется fallback vehicle/sleeping,
 * а новые GSit методы добавляются в findState().
 */
public final class GSitHook {
    private final JavaPlugin plugin;
    private Method sitting;
    private Method laying;

    public GSitHook(JavaPlugin plugin) {
        this.plugin = plugin;
        if (plugin.getServer().getPluginManager().getPlugin("GSit") != null) {
            findState();
        } else {
            plugin.getLogger().info("GSit не найден: бонус sit-lay работает через Paper fallback.");
        }
    }

    private void findState() {
        try {
            Class<?> api = Class.forName("dev.geco.gsit.api.GSitAPI");
            sitting = find(api, "isSitting");
            laying = find(api, "isLaying");
            plugin.getLogger().info("GSit найден: включена интеграция sit/lay.");
        } catch (ReflectiveOperationException exception) {
            plugin.getLogger().warning("GSit найден, но его API недоступно: " + exception.getMessage());
        }
    }

    private Method find(Class<?> type, String name) {
        try { return type.getMethod(name, Player.class); }
        catch (NoSuchMethodException ignored) { return null; }
    }

    public boolean isSittingOrLaying(Player player) {
        try {
            if (sitting != null && Boolean.TRUE.equals(sitting.invoke(null, player))) return true;
            if (laying != null && Boolean.TRUE.equals(laying.invoke(null, player))) return true;
        } catch (ReflectiveOperationException ignored) {
            // Fallback below remains valid if GSit changes its API.
        }
        Entity vehicle = player.getVehicle();
        return vehicle != null || player.getPose() == Pose.SLEEPING;
    }
}