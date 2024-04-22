package me.juneylove.shakedown.games.chorusvolley;

import me.juneylove.shakedown.ui.GUIFormat;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpeedPad extends PowerupPad{

    int speedDurationTicks = 60;

    SpeedPad(Material enabledMaterial, Location fanLocation, int reEnableDelay, String name) {

        super(enabledMaterial, fanLocation, reEnableDelay, name, GUIFormat.menuSelectItem);

        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(GUIFormat.menuSelectItem);
        meta.setCustomModelData(501);
        ItemStack stack = display.getItemStack();
        stack.setItemMeta(meta);
        display.setItemStack(stack);

    }

    @Override
    protected boolean shouldApplyEffect(Player player) {
        return true;
    }

    @Override
    protected void applyEffect(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, speedDurationTicks, 1));
    }
}
