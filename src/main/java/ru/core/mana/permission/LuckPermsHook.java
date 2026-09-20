package ru.core.mana.permission;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import ru.core.mana.service.ManaService;

/**
 * Подключает UserDataRecalculateEvent LuckPerms без compileOnly-зависимости.
 * Используется только при наличии LuckPerms; если API отсутствует, плагин
 * продолжает работать с периодическим обновлением разрешений.
 */
public final class LuckPermsHook {
    private LuckPermsHook() {}

    @SuppressWarnings("unchecked")
    public static void register(JavaPlugin plugin, ManaService service) {
        if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) {
            plugin.getLogger().info("LuckPerms не найден: обновление разрешений выполняется по таймеру.");
            return;
        }
        try {
            Class<?> eventClass = Class.forName(
                    "net.luckperms.api.event.user.UserDataRecalculateEvent");
            Listener listener = new Listener() {};
            EventExecutor executor = (ignored, event) -> service.refreshByLuckPermsEvent(event);
            Bukkit.getPluginManager().registerEvent((Class<? extends Event>) eventClass, listener,
                    EventPriority.NORMAL, executor, plugin);
            plugin.getLogger().info("LuckPerms найден: включено обновление маны по UserDataRecalculateEvent.");
        } catch (ReflectiveOperationException | IllegalArgumentException exception) {
            plugin.getLogger().warning("Не удалось подключить LuckPerms API: " + exception.getMessage());
        }
    }
}