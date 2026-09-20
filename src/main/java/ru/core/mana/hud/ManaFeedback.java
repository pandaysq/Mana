package ru.core.mana.hud;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import ru.core.mana.config.ManaConfig;
import ru.core.mana.service.ManaService;
import ru.core.mana.state.ManaState;

/**
 * Проигрывает звуки прироста и достижения максимума с cooldown.
 * Используется в регенерационном тикере; настройки звука добавляются в
 * sound-on-increase и sound-on-full внутри config.yml.
 */
public final class ManaFeedback {
    private final ManaService service;
    private final ManaConfig config;

    public ManaFeedback(ManaService service, ManaConfig config) {
        this.service = service;
        this.config = config;
    }

    public void update(Player player, boolean regenerated) {
        ManaState state = service.state(player);
        double current = service.getMana(player);
        double previous = state.previousMana();
        double increase = current - previous;
        if (increase > 0.0) {
            ManaConfig.SoundSettings settings = config.soundIncrease();
            boolean manual = state.pendingManualIncrease();
            boolean allowed = settings.enabled() && increase >= settings.minIncrease()
                    && (manual ? settings.playOnAdd() : regenerated && settings.playOnRegen())
                    && (System.currentTimeMillis() - state.lastIncreaseSoundAt()
                    >= settings.cooldownTicks() * 50.0);
            if (allowed) {
                play(player, settings);
                state.lastIncreaseSoundAt(System.currentTimeMillis());
            }
            state.pendingManualIncrease(false);
        }
        boolean full = service.getMaxMana(player) > 0.0 && current >= service.getMaxMana(player);
        if (full && !state.lastFull() && config.soundFull().enabled()) play(player, config.soundFull());
        state.lastFull(full);
        state.previousMana(current);
    }

    private void play(Player player, ManaConfig.SoundSettings settings) {
        try {
            player.playSound(player.getLocation(), Sound.valueOf(settings.sound()),
                    settings.volume(), settings.pitch());
        } catch (IllegalArgumentException ignored) {
            // A bad configurable sound must not stop the mana ticker.
        }
    }
}