package ru.core.mana.hud;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import ru.core.mana.config.ManaConfig;
import ru.core.mana.service.ManaService;
import ru.core.mana.state.ManaState;

import java.text.DecimalFormat;

/**
 * Отправляет actionbar с glyph-компонентами шрифта core:mana.
 * Используется периодическим тикером; размеры и позиционирование меняются
 * в config.yml и resourcepack, а формат HUD остаётся в этом классе.
 */
public final class ManaHud {
    private static final Key MANA_FONT = Key.key("core", "mana");
    private final ManaService service;
    private final ManaConfig config;

    public ManaHud(ManaService service, ManaConfig config) {
        this.service = service;
        this.config = config;
    }

    public void send(Player player) {
        double max = service.getMaxMana(player);
        if (max <= 0.0) {
            player.sendActionBar(Component.empty());
            return;
        }
        double mana = service.getMana(player);
        if (config.hudHideWhenFull() && mana >= max) {
            player.sendActionBar(Component.empty());
            return;
        }
        double ratio = Math.max(0.0, Math.min(1.0, mana / max));
        TextColor color = TextColor.fromHexString(color(ratio, mana));
        int steps = Math.max(1, (int) config.hudWidthSteps());
        int cursor = config.hudDirection().equalsIgnoreCase("left-to-right")
                ? (int) Math.round(ratio * (steps - 1))
                : (int) Math.round((1.0 - ratio) * (steps - 1));
        StringBuilder bar = new StringBuilder("\uE000");
        for (int i = 0; i < steps; i++) bar.append(i == cursor ? "\uE002" : "\uE001");
        Component result = Component.text(shift(config.hudShiftX()) + bar, color).font(MANA_FONT);
        if (config.hudShowNumber()) {
            DecimalFormat format = new DecimalFormat("0." + "0".repeat(config.hudDecimals()));
            result = result.append(Component.text(" " + format.format(mana) + "/" + format.format(max), color));
        }
        player.sendActionBar(result);
    }

    private String color(double ratio, double mana) {
        if (mana <= 0.000001) {
            for (ManaConfig.ColorBand band : config.colorBands()) if (band.atZero()) return band.color();
        }
        double percent = ratio * 100.0;
        for (ManaConfig.ColorBand band : config.colorBands()) {
            if (!band.atZero() && percent <= band.from() && percent >= band.to()) return band.color();
        }
        return "#ffffff";
    }

    private String shift(double pixels) {
        int units = (int) Math.round(pixels / 8.0);
        if (units == 0) return "";
        String glyph = units < 0 ? "\uE101" : "\uE103";
        return glyph.repeat(Math.min(8, Math.abs(units)));
    }
}