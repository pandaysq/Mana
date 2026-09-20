package ru.core.mana.state;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Живое состояние одного игрока: мана, кэш разрешений, модификаторы и данные
 * движения. Используется ManaService, регенерацией и HUD; новое состояние,
 * необходимое бонусу, добавляется сюда, а не в Player через metadata.
 */
public final class ManaState {
    private final UUID playerId;
    private double mana;
    private double maxFromPermissions;
    private double regenFromPermissions;
    private Map<String, Double> maxPermissionBreakdown = new LinkedHashMap<>();
    private Map<String, Double> regenPermissionBreakdown = new LinkedHashMap<>();
    private final Map<String, Double> maxModifiers = new LinkedHashMap<>();
    private final Map<String, Double> regenModifiers = new LinkedHashMap<>();
    private long permissionsRefreshedAt;
    private long lastHudAt;
    private long lastIncreaseSoundAt;
    private boolean lastFull;
    private double previousMana;
    private boolean moving;
    private boolean activity;
    private boolean pendingManualIncrease;
    private org.bukkit.Location lastLocation;
    private boolean loaded;

    public ManaState(UUID playerId, double initialMana) {
        this.playerId = playerId;
        this.mana = Math.max(0.0, initialMana);
        this.previousMana = this.mana;
    }

    public UUID playerId() { return playerId; }
    public double mana() { return mana; }
    public void mana(double mana) { this.mana = Math.max(0.0, mana); }
    public double maxFromPermissions() { return maxFromPermissions; }
    public double regenFromPermissions() { return regenFromPermissions; }
    public Map<String, Double> maxPermissionBreakdown() { return maxPermissionBreakdown; }
    public Map<String, Double> regenPermissionBreakdown() { return regenPermissionBreakdown; }
    public void permissions(double max, double regen, Map<String, Double> maxParts, Map<String, Double> regenParts) {
        this.maxFromPermissions = max;
        this.regenFromPermissions = regen;
        this.maxPermissionBreakdown = new LinkedHashMap<>(maxParts);
        this.regenPermissionBreakdown = new LinkedHashMap<>(regenParts);
    }
    public Map<String, Double> maxModifiers() { return maxModifiers; }
    public Map<String, Double> regenModifiers() { return regenModifiers; }
    public long permissionsRefreshedAt() { return permissionsRefreshedAt; }
    public void permissionsRefreshedAt(long value) { permissionsRefreshedAt = value; }
    public long lastHudAt() { return lastHudAt; }
    public void lastHudAt(long value) { lastHudAt = value; }
    public long lastIncreaseSoundAt() { return lastIncreaseSoundAt; }
    public void lastIncreaseSoundAt(long value) { lastIncreaseSoundAt = value; }
    public boolean lastFull() { return lastFull; }
    public void lastFull(boolean value) { lastFull = value; }
    public double previousMana() { return previousMana; }
    public void previousMana(double value) { previousMana = value; }
    public boolean moving() { return moving; }
    public void moving(boolean value) { moving = value; }
    public boolean activity() { return activity; }
    public void activity(boolean value) { activity = value; }
    public boolean pendingManualIncrease() { return pendingManualIncrease; }
    public void pendingManualIncrease(boolean value) { pendingManualIncrease = value; }
    public org.bukkit.Location lastLocation() { return lastLocation; }
    public void lastLocation(org.bukkit.Location value) { lastLocation = value; }
    public boolean loaded() { return loaded; }
    public void loaded(boolean value) { loaded = value; }
}