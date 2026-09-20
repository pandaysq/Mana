package ru.core.mana.api;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Отменяемое событие изменения маны, кроме обычной регенерации.
 * Публикуется ManaService перед set/add/take; новый код интеграций может
 * отменить изменение здесь, не меняя реализацию сервиса.
 */
public final class ManaChangeEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private final double oldMana;
    private double newMana;
    private final Reason reason;
    private boolean cancelled;

    public ManaChangeEvent(Player player, double oldMana, double newMana, Reason reason) {
        this.player = player;
        this.oldMana = oldMana;
        this.newMana = newMana;
        this.reason = reason;
    }

    public Player getPlayer() { return player; }
    public double getOldMana() { return oldMana; }
    public double getNewMana() { return newMana; }
    public void setNewMana(double newMana) { this.newMana = newMana; }
    public Reason getReason() { return reason; }
    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }

    public enum Reason {
        SET, ADD, TAKE, DEATH, CLAMP
    }
}