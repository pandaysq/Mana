package ru.core.mana.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import ru.core.mana.service.ManaService;
import ru.core.mana.state.ManaState;

/**
 * Синхронизирует состояние маны с жизненным циклом игрока и его движением.
 * Используется ManaPlugin; новые события, влияющие на смерть или activity,
 * добавляются сюда.
 */
public final class ManaListener implements Listener {
    private final ManaService service;

    public ManaListener(ManaService service) { this.service = service; }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        service.load(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        service.unload(event.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        service.setDeathValue(event.getPlayer());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        ManaState state = service.state(player);
        if (event.getTo() == null) return;
        if (state.lastLocation() == null) {
            state.lastLocation(event.getTo().clone());
            return;
        }
        double dx = event.getTo().getX() - state.lastLocation().getX();
        double dz = event.getTo().getZ() - state.lastLocation().getZ();
        state.moving(dx * dx + dz * dz > 0.000025);
        state.lastLocation(event.getTo().clone());
    }
}