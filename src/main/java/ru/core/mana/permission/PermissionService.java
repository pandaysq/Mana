package ru.core.mana.permission;

import org.bukkit.entity.Player;
import ru.core.mana.config.ManaConfig;
import ru.core.mana.state.ManaState;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Суммирует активные permission nodes и кэширует результат в ManaState.
 * Используется при входе, по таймеру и после LuckPerms recalculation; новые
 * виды разрешений добавляются в PermissionSnapshot/config, а не в регенератор.
 */
public final class PermissionService {
    private final ManaConfig config;

    public PermissionService(ManaConfig config) {
        this.config = config;
    }

    public void refresh(Player player, ManaState state) {
        String maxPrefix = config.permissionPrefix("permissions.max-prefix", "mana.give.");
        String regenPrefix = config.permissionPrefix("permissions.regen-prefix", "mana.regen.");
        Map<String, Double> maxValues = config.permissionValues("permissions.max");
        Map<String, Double> regenValues = config.permissionValues("permissions.regen");
        Map<String, Double> maxParts = new LinkedHashMap<>();
        Map<String, Double> regenParts = new LinkedHashMap<>();
        for (var permission : player.getEffectivePermissions()) {
            if (!permission.getValue()) continue;
            String node = permission.getPermission();
            if (node.startsWith(maxPrefix)) {
                String key = node.substring(maxPrefix.length());
                if (maxValues.containsKey(key)) maxParts.put(node, maxValues.get(key));
            }
            if (node.startsWith(regenPrefix)) {
                String key = node.substring(regenPrefix.length());
                if (regenValues.containsKey(key)) regenParts.put(node, regenValues.get(key));
            }
        }
        state.permissions(maxParts.values().stream().mapToDouble(Double::doubleValue).sum(),
                regenParts.values().stream().mapToDouble(Double::doubleValue).sum(), maxParts, regenParts);
        state.permissionsRefreshedAt(System.currentTimeMillis());
    }
}