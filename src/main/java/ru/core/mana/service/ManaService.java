package ru.core.mana.service;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.java.JavaPlugin;
import ru.core.mana.api.ManaAPI;
import ru.core.mana.api.ManaChangeEvent;
import ru.core.mana.bonus.RegenBonusRegistry;
import ru.core.mana.config.ManaConfig;
import ru.core.mana.integration.GSitHook;
import ru.core.mana.permission.PermissionService;
import ru.core.mana.regen.RegenCalculator;
import ru.core.mana.state.ManaState;
import ru.core.mana.state.ManaStateStore;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Главный сервис маны и реализация ManaAPI, зарегистрированная в ServicesManager.
 * Используется слушателями, командами и внешними плагинами; новые операции
 * изменения маны проходят через change(), чтобы сохранять события и лимиты.
 */
public final class ManaService implements ManaAPI {
    private final JavaPlugin plugin;
    private final ManaConfig config;
    private final PermissionService permissions;
    private final ManaStateStore store;
    private final RegenBonusRegistry bonusRegistry;
    private final RegenCalculator calculator;
    private final Map<UUID, ManaState> states = new ConcurrentHashMap<>();

    public ManaService(JavaPlugin plugin, ManaConfig config, GSitHook gsit) {
        this.plugin = plugin;
        this.config = config;
        this.permissions = new PermissionService(config);
        this.store = new ManaStateStore(plugin, config);
        this.bonusRegistry = new RegenBonusRegistry(config, gsit);
        this.calculator = new RegenCalculator(config, bonusRegistry);
    }

    public void register() {
        ServicesManager services = Bukkit.getServicesManager();
        services.register(ManaAPI.class, this, plugin, org.bukkit.plugin.ServicePriority.Normal);
    }

    public void unregister() { Bukkit.getServicesManager().unregister(ManaAPI.class, this); }

    public ManaState state(Player player) {
        return states.computeIfAbsent(player.getUniqueId(),
                ignored -> new ManaState(player.getUniqueId(), config.initialMana()));
    }

    public Iterable<ManaState> states() { return states.values(); }

    public void load(Player player) {
        ManaState state = state(player);
        store.load(player, state);
        state.previousMana(state.mana());
        refreshPermissions(player);
        clamp(player, state);
    }

    public void save(Player player) {
        ManaState state = states.get(player.getUniqueId());
        if (state != null) store.save(player, state);
    }

    public void unload(Player player) {
        save(player);
        states.remove(player.getUniqueId());
    }

    public void refreshPermissions(Player player) {
        ManaState state = state(player);
        permissions.refresh(player, state);
        clamp(player, state);
    }

    public void refreshDue(Player player) {
        ManaState state = state(player);
        if (System.currentTimeMillis() - state.permissionsRefreshedAt()
                >= config.permissionRefreshSeconds() * 1000.0) refreshPermissions(player);
    }

    public void refreshAllPermissions() {
        for (Player player : Bukkit.getOnlinePlayers()) refreshPermissions(player);
    }

    public void updateInput(Player player, boolean moving) {
        state(player).moving(moving);
    }

    public void refreshByLuckPermsEvent(Event event) {
        try {
            Object user = event.getClass().getMethod("getUser").invoke(event);
            UUID uuid = (UUID) user.getClass().getMethod("getUniqueId").invoke(user);
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) refreshPermissions(player);
        } catch (ReflectiveOperationException | ClassCastException exception) {
            plugin.getLogger().warning("Не удалось обработать LuckPerms recalculation: " + exception.getMessage());
        }
    }

    public RegenCalculator.Calculation calculation(Player player) {
        return calculator.calculate(player, state(player));
    }

    public void regenerate(Player player, double amount) {
        if (amount <= 0.0 || getMaxMana(player) <= 0.0) return;
        change(player, Math.min(getMaxMana(player), getMana(player) + amount),
                ManaChangeEvent.Reason.ADD, false);
    }

    public void clamp(Player player, ManaState state) {
        double max = getMaxMana(player);
        if (state.mana() > max) change(player, max, ManaChangeEvent.Reason.CLAMP, false);
        if (max <= 0.0 && state.mana() != 0.0) state.mana(0.0);
    }

    public void setDeathValue(Player player) {
        String mode = config.deathMode();
        if ("reset".equals(mode)) change(player, 0.0, ManaChangeEvent.Reason.DEATH, true);
        else if ("percent".equals(mode)) change(player, getMaxMana(player)
                * Math.max(0.0, Math.min(100.0, config.respawnValue())) / 100.0,
                ManaChangeEvent.Reason.DEATH, true);
    }

    public RegenBonusRegistry bonusRegistry() { return bonusRegistry; }

    @Override public double getMana(Player player) { return state(player).mana(); }
    @Override public double getMaxMana(Player player) {
        ManaState state = state(player);
        return Math.max(0.0, state.maxFromPermissions()
                + state.maxModifiers().values().stream().mapToDouble(Double::doubleValue).sum());
    }
    @Override public double getRegenPerSecond(Player player) {
        return Math.max(0.0, calculation(player).total()
                + state(player).regenModifiers().values().stream().mapToDouble(Double::doubleValue).sum());
    }
    @Override public boolean setMana(Player player, double amount) {
        return change(player, amount, ManaChangeEvent.Reason.SET, true);
    }
    @Override public boolean addMana(Player player, double amount) {
        return change(player, getMana(player) + amount, amount >= 0 ? ManaChangeEvent.Reason.ADD
                : ManaChangeEvent.Reason.TAKE, true);
    }
    @Override public boolean tryConsume(Player player, double amount) {
        if (amount < 0.0 || getMana(player) < amount) return false;
        return change(player, getMana(player) - amount, ManaChangeEvent.Reason.TAKE, true);
    }
    @Override public void addMaxModifier(Player player, String key, double amount) {
        state(player).maxModifiers().put(key, amount);
        clamp(player, state(player));
    }
    @Override public void removeMaxModifier(Player player, String key) {
        state(player).maxModifiers().remove(key);
        clamp(player, state(player));
    }
    @Override public void addRegenModifier(Player player, String key, double amount) {
        state(player).regenModifiers().put(key, amount);
    }
    @Override public void removeRegenModifier(Player player, String key) {
        state(player).regenModifiers().remove(key);
    }

    private boolean change(Player player, double requested, ManaChangeEvent.Reason reason, boolean cancellable) {
        ManaState state = state(player);
        double old = state.mana();
        double max = getMaxMana(player);
        double safeRequested = Double.isFinite(requested) ? requested : 0.0;
        double next = Math.max(0.0, Math.min(max, safeRequested));
        if (Math.abs(old - next) < 0.000001) return false;
        if (cancellable) {
            ManaChangeEvent event = new ManaChangeEvent(player, old, next, reason);
            Bukkit.getPluginManager().callEvent(event);
            if (event.isCancelled()) return false;
            next = Math.max(0.0, Math.min(max, event.getNewMana()));
        }
        state.mana(next);
        if (cancellable && next > old) state.pendingManualIncrease(true);
        return true;
    }

}