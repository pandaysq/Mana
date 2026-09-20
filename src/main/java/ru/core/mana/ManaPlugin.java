package ru.core.mana;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.core.mana.command.ManaCommand;
import ru.core.mana.config.MessageService;
import ru.core.mana.config.ManaConfig;
import ru.core.mana.hud.ManaFeedback;
import ru.core.mana.hud.ManaHud;
import ru.core.mana.integration.GSitHook;
import ru.core.mana.integration.PlayerInputHook;
import ru.core.mana.listener.ManaListener;
import ru.core.mana.permission.LuckPermsHook;
import ru.core.mana.service.ManaService;

/**
 * Точка входа плагина Mana: создаёт конфигурацию и сервисы, регистрирует API,
 * команды и тикер. Используется Bukkit при загрузке; новые общие компоненты
 * подключаются здесь, а не из отдельных listener-классов.
 */
public final class ManaPlugin extends JavaPlugin {
    private ManaConfig config;
    private ManaService service;
    private ManaHud hud;
    private ManaFeedback feedback;
    private long ticks;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = new ManaConfig(this);
        MessageService messages = new MessageService(config);
        GSitHook gsit = new GSitHook(this);
        service = new ManaService(this, config, gsit);
        service.register();
        hud = new ManaHud(service, config);
        feedback = new ManaFeedback(service, config);
        ManaCommand command = new ManaCommand(service, config, messages);
        getCommand("mana").setExecutor(command);
        getCommand("mana").setTabCompleter(command);
        Bukkit.getPluginManager().registerEvents(new ManaListener(service), this);
        LuckPermsHook.register(this, service);
        PlayerInputHook.register(this, service);
        for (Player player : Bukkit.getOnlinePlayers()) service.load(player);
        getServer().getScheduler().runTaskTimer(this, this::tick, 1L, 1L);
        getLogger().info("Mana включён.");
    }

    @Override
    public void onDisable() {
        if (service == null) return;
        for (Player player : Bukkit.getOnlinePlayers()) service.save(player);
        service.unregister();
    }

    private void tick() {
        ticks++;
        for (Player player : Bukkit.getOnlinePlayers()) {
            service.refreshDue(player);
            if (ticks % Math.max(1L, Math.round(config.updateIntervalTicks())) == 0L) {
                double intervalSeconds = config.updateIntervalTicks() / 20.0;
                service.regenerate(player, service.getRegenPerSecond(player) * intervalSeconds);
                feedback.update(player, true);
            }
            if (ticks % Math.max(1L, Math.round(config.hudRefreshTicks())) == 0L) hud.send(player);
            if (ticks % Math.max(1L, Math.round(config.saveSeconds() * 20.0)) == 0L) service.save(player);
        }
    }
}