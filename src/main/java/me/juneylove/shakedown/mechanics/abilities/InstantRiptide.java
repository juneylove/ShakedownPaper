package me.juneylove.shakedown.mechanics.abilities;

import me.juneylove.shakedown.ui.GUIFormat;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class InstantRiptide {

    private static final double SPEED_METERS_PER_TICK = 3.0;

    public static void boost(Player player) {

        if (player.getCooldown(GUIFormat.menuSelectItem) != 0) return;

        player.setCooldown(GUIFormat.menuSelectItem, 100);
        Vector newVelocity = player.getVelocity().add(player.getLocation().getDirection().multiply(SPEED_METERS_PER_TICK));
        player.setVelocity(newVelocity);
        player.playSound(player, Sound.ITEM_TRIDENT_RIPTIDE_3, 1.0f, 1.0f);

    }

}
