package ru.core.mana.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import ru.core.mana.config.MessageService;
import ru.core.mana.config.ManaConfig;
import ru.core.mana.regen.RegenCalculator;
import ru.core.mana.service.ManaService;
import ru.core.mana.state.ManaState;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Реализует /mana, администрирование, debug и reload.
 * Используется Bukkit command map; новые подкоманды добавляются в onCommand и
 * сопровождаются ключами сообщений в messages.yml.
 */
public final class ManaCommand implements CommandExecutor, TabCompleter {
    private final ManaService service;
    private final ManaConfig config;
    private final MessageService messages;

    public ManaCommand(ManaService service, ManaConfig config, MessageService messages) {
        this.service = service;
        this.config = config;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return show(sender, sender instanceof Player player ? player : null);
        String first = args[0].toLowerCase(Locale.ROOT);
        if (first.equals("reload")) {
            if (!sender.hasPermission("mana.admin")) return noPermission(sender);
            config.reload();
            service.refreshAllPermissions();
            messages.send(sender, "mana.reloaded");
            return true;
        }
        if (first.equals("set") || first.equals("add") || first.equals("take")) {
            if (!sender.hasPermission("mana.admin")) return noPermission(sender);
            return modify(sender, first, args);
        }
        if (first.equals("debug")) {
            if (!sender.hasPermission("mana.view")) return noPermission(sender);
            Player target = target(sender, args.length > 1 ? args[1] : null,
                    args.length > 1 ? "mana.view.others" : "mana.view");
            return target == null ? true : debug(sender, target);
        }
        Player target = target(sender, first, "mana.view.others");
        return target == null ? true : show(sender, target);
    }

    private boolean show(CommandSender sender, Player target) {
        if (target == null) {
            messages.send(sender, "mana.usage");
            return true;
        }
        if (target != sender && !sender.hasPermission("mana.view.others")) return noPermission(sender);
        messages.send(sender, "mana." + (target == sender ? "self" : "other"),
                "player", target.getName(), "current", format(service.getMana(target)),
                "max", format(service.getMaxMana(target)),
                "regen", format(service.getRegenPerSecond(target)));
        return true;
    }

    private boolean modify(CommandSender sender, String operation, String[] args) {
        if (args.length < 3) {
            messages.send(sender, "mana.usage");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            messages.send(sender, "mana.no-player");
            return true;
        }
        try {
            double amount = Double.parseDouble(args[2]);
            boolean changed = switch (operation) {
                case "set" -> service.setMana(target, amount);
                case "add" -> service.addMana(target, amount);
                default -> service.addMana(target, -Math.abs(amount));
            };
            if (changed) messages.send(sender, "mana.changed", "player", target.getName());
            return true;
        } catch (NumberFormatException exception) {
            messages.send(sender, "mana.invalid-number");
            return true;
        }
    }

    private boolean debug(CommandSender sender, Player target) {
        ManaState state = service.state(target);
        RegenCalculator.Calculation calculation = service.calculation(target);
        messages.send(sender, "mana.debug-header", "player", target.getName());
        messages.send(sender, "mana.debug-line", "label", "Мана",
                "value", format(service.getMana(target)) + "/" + format(service.getMaxMana(target)));
        messages.send(sender, "mana.debug-line", "label", "Реген",
                "value", format(service.getRegenPerSecond(target)) + "/с");
        state.maxPermissionBreakdown().forEach((permission, value) ->
                messages.send(sender, "mana.debug-permission", "permission", permission, "value", format(value)));
        state.regenPermissionBreakdown().forEach((permission, value) ->
                messages.send(sender, "mana.debug-permission", "permission", permission, "value", format(value)));
        messages.send(sender, "mana.debug-line", "label", "База разрешений",
                "value", format(calculation.permissionBase()));
        calculation.bonuses().forEach(bonus -> messages.send(sender, "mana.debug-line",
                "label", bonus.key(), "value", format(bonus.amount())));
        if (calculation.threshold() != null) messages.send(sender, "mana.debug-line",
                "label", "Порог " + format(calculation.threshold().at()),
                "value", format(calculation.threshold().amount()));
        return true;
    }

    private Player target(CommandSender sender, String name, String permission) {
        if (name == null) return sender instanceof Player player ? player : null;
        if (!sender.hasPermission(permission)) {
            noPermission(sender);
            return null;
        }
        Player target = Bukkit.getPlayerExact(name);
        if (target == null) messages.send(sender, "mana.no-player");
        return target;
    }

    private boolean noPermission(CommandSender sender) {
        messages.send(sender, "mana.no-permission");
        return true;
    }

    private String format(double value) { return String.format(Locale.US, "%.2f", value); }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) return List.of("set", "add", "take", "debug", "reload");
        if (args.length == 2 && List.of("set", "add", "take", "debug").contains(args[0].toLowerCase())) {
            return new ArrayList<>(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
        }
        return List.of();
    }
}