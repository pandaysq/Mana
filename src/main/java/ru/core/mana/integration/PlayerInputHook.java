package ru.core.mana.integration;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import ru.core.mana.service.ManaService;

import java.lang.reflect.Method;

/**
 * Подключает Paper PlayerInputEvent через reflection, чтобы проект оставался
 * совместимым с API, где событие ещё отсутствует. Используется для точного
 * определения нажатых клавиш; при отсутствии события ManaListener применяет
 * fallback по смещению позиции.
 */
public final class PlayerInputHook {
    private PlayerInputHook() {}

    @SuppressWarnings("unchecked")
    public static void register(JavaPlugin plugin, ManaService service) {
        try {
            Class<?> eventClass = Class.forName("org.bukkit.event.player.PlayerInputEvent");
            Listener listener = new Listener() {};
            EventExecutor executor = (ignored, event) -> {
                Object player = eventClass.getMethod("getPlayer").invoke(event);
                Object input = eventClass.getMethod("getInput").invoke(event);
                boolean moving = Boolean.TRUE.equals(input.getClass().getMethod("isForward").invoke(input))
                        || Boolean.TRUE.equals(input.getClass().getMethod("isBackward").invoke(input))
                        || Boolean.TRUE.equals(input.getClass().getMethod("isLeft").invoke(input))
                        || Boolean.TRUE.equals(input.getClass().getMethod("isRight").invoke(input));
                service.updateInput((org.bukkit.entity.Player) player, moving);
            };
            Bukkit.getPluginManager().registerEvent((Class<? extends Event>) eventClass, listener,
                    EventPriority.MONITOR, executor, plugin);
            plugin.getLogger().info("Paper PlayerInputEvent найден: движение читается по клавишам.");
        } catch (ReflectiveOperationException | IllegalArgumentException exception) {
            plugin.getLogger().info("PlayerInputEvent недоступен: движение определяется по смещению позиции.");
        }
    }
}