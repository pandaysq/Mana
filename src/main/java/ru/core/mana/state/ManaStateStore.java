package ru.core.mana.state;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import ru.core.mana.config.ManaConfig;

/**
 * Загружает и сохраняет текущую ману в PersistentDataContainer игрока.
 * Используется при входе, выходе и периодическом сохранении; новые постоянные
 * поля состояния добавляются здесь с отдельным NamespacedKey.
 */
public final class ManaStateStore {
    private final NamespacedKey manaKey;
    private final ManaConfig config;

    public ManaStateStore(JavaPlugin plugin, ManaConfig config) {
        this.manaKey = new NamespacedKey(plugin, "mana");
        this.config = config;
    }

    public void load(Player player, ManaState state) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        Double stored = data.get(manaKey, PersistentDataType.DOUBLE);
        state.mana(stored == null ? config.initialMana() : stored);
        state.loaded(true);
    }

    public void save(Player player, ManaState state) {
        player.getPersistentDataContainer().set(manaKey, PersistentDataType.DOUBLE, state.mana());
    }
}