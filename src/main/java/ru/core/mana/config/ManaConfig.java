package ru.core.mana.config;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Централизованно читает config.yml и messages.yml, включая все числовые настройки.
 * Используется сервисами Mana, HUD и командами; новое поле конфигурации сначала
 * добавляется сюда, чтобы остальные классы не читали YAML напрямую.
 */
public final class ManaConfig {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private FileConfiguration messages;

    public ManaConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) plugin.saveResource("messages.yml", false);
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public FileConfiguration raw() { return config; }
    public FileConfiguration messages() { return messages; }
    public double initialMana() { return config.getDouble("mana.initial", 0.0); }
    public double saveSeconds() { return Math.max(1.0, config.getDouble("mana.save-seconds", 10.0)); }
    public String deathMode() { return config.getString("mana.on-death", "keep").toLowerCase(); }
    public double respawnValue() { return config.getDouble("mana.respawn-value", 0.0); }
    public double permissionRefreshSeconds() { return Math.max(1.0, config.getDouble("permissions.refresh-seconds", 10.0)); }
    public double updateIntervalTicks() { return Math.max(1.0, config.getDouble("regen.update-interval-ticks", 5.0)); }
    public boolean requireActivity() { return config.getBoolean("regen.require-activity", false); }
    public double hudRefreshTicks() { return Math.max(1.0, config.getDouble("hud.refresh-ticks", 10.0)); }
    public boolean hudHideWhenFull() { return config.getBoolean("hud.hide-when-full", false); }
    public boolean hudShowNumber() { return config.getBoolean("hud.show-number", true); }
    public int hudDecimals() { return Math.max(0, (int) config.getDouble("hud.number-decimals", 1.0)); }
    public String hudDirection() { return config.getString("hud.direction", "right-to-left"); }
    public double hudShiftX() { return config.getDouble("hud.shift-x", 0.0); }
    public double hudAscent() { return config.getDouble("hud.ascent", 8.0); }
    public double hudWidthSteps() { return Math.max(1.0, config.getDouble("hud.width-steps", 10.0)); }

    public Map<String, Double> permissionValues(String path) {
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) return Collections.emptyMap();
        Map<String, Double> result = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) result.put(key, section.getDouble(key));
        return result;
    }

    public String permissionPrefix(String path, String fallback) {
        return config.getString(path, fallback);
    }

    public BonusSettings bonus(String name) {
        String path = "bonuses." + name;
        return new BonusSettings(config.getBoolean(path + ".enabled", false),
                config.getDouble(path + ".amount", 0.0),
                config.getString(path + ".food-level", ">=20"),
                config.getString(path + ".health", ">=0"),
                config.getStringList(path + ".blocks"));
    }

    public List<Threshold> thresholds() {
        List<Threshold> result = new ArrayList<>();
        for (Map<?, ?> value : config.getMapList("thresholds.list")) {
            Object at = value.get("at");
            Object amount = value.get("amount");
            if (at != null && amount != null) {
                result.add(new Threshold(Double.parseDouble(String.valueOf(at)),
                        Double.parseDouble(String.valueOf(amount))));
            }
        }
        return result;
    }

    public boolean thresholdsEnabled() { return config.getBoolean("thresholds.enabled", true); }
    public String thresholdMode() { return config.getString("thresholds.mode", "bonus").toLowerCase(); }

    public List<ColorBand> colorBands() {
        List<ColorBand> result = new ArrayList<>();
        for (Map<?, ?> value : config.getMapList("hud.colors")) {
            String color = String.valueOf(value(value, "color", "#ffffff"));
            boolean zero = Boolean.parseBoolean(String.valueOf(value(value, "at-zero", false)));
            double from = Double.parseDouble(String.valueOf(value(value, "from", 0.0)));
            double to = Double.parseDouble(String.valueOf(value(value, "to", 0.0)));
            result.add(new ColorBand(from, to, color, zero));
        }
        return result;
    }

    private Object value(Map<?, ?> values, Object key, Object fallback) {
        return values.containsKey(key) ? values.get(key) : fallback;
    }

    public SoundSettings soundIncrease() {
        return sound("sound-on-increase", "BLOCK_AMETHYST_BLOCK_CHIME");
    }

    public SoundSettings soundFull() {
        return sound("sound-on-full", "BLOCK_BEACON_POWER_SELECT");
    }

    private SoundSettings sound(String path, String fallback) {
        return new SoundSettings(config.getBoolean(path + ".enabled", false),
                config.getString(path + ".sound", fallback),
                (float) config.getDouble(path + ".volume", 1.0),
                (float) config.getDouble(path + ".pitch", 1.0),
                config.getDouble(path + ".min-increase", 0.0),
                config.getDouble(path + ".cooldown-ticks", 0.0),
                config.getBoolean(path + ".play-on-regen", false),
                config.getBoolean(path + ".play-on-add", true));
    }

    public record BonusSettings(boolean enabled, double amount, String foodLevel,
                                String health, List<String> blocks) {}
    public record Threshold(double at, double amount) {}
    public record ColorBand(double from, double to, String color, boolean atZero) {}
    public record SoundSettings(boolean enabled, String sound, float volume, float pitch,
                                double minIncrease, double cooldownTicks,
                                boolean playOnRegen, boolean playOnAdd) {}
}