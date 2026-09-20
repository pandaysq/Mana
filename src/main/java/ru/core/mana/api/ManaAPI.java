package ru.core.mana.api;

import org.bukkit.entity.Player;

/**
 * Публичный API плагина Mana, зарегистрированный через Bukkit ServicesManager.
 * Используется другими плагинами для чтения и изменения маны; новые публичные
 * методы добавляются сюда только при необходимости расширить интеграцию.
 */
public interface ManaAPI {
    double getMana(Player player);
    double getMaxMana(Player player);
    double getRegenPerSecond(Player player);
    boolean setMana(Player player, double amount);
    boolean addMana(Player player, double amount);
    boolean tryConsume(Player player, double amount);
    void addMaxModifier(Player player, String key, double amount);
    void removeMaxModifier(Player player, String key);
    void addRegenModifier(Player player, String key, double amount);
    void removeRegenModifier(Player player, String key);
}