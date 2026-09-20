package ru.core.mana.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;

/**
 * Рендерит сообщения из messages.yml через MiniMessage.
 * Используется командами и системными уведомлениями; новый текст добавляется
 * в messages.yml и вызывается по пути через message().
 */
public final class MessageService {
    private final ManaConfig config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MessageService(ManaConfig config) {
        this.config = config;
    }

    public Component message(String path, Object... replacements) {
        String text = config.messages().getString(path, "<red>Missing message: " + path);
        text = text.replace("<prefix>", config.messages().getString("prefix", ""));
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            text = text.replace("<" + replacements[i] + ">", String.valueOf(replacements[i + 1]));
        }
        return miniMessage.deserialize(text);
    }

    public void send(CommandSender sender, String path, Object... replacements) {
        sender.sendMessage(message(path, replacements));
    }
}