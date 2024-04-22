package me.juneylove.shakedown.games.chorusvolley;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public class FishingRodPad extends PowerupPad {

    ItemStack fishingRod = new ItemStack(Material.FISHING_ROD);

    {
        Damageable meta = (Damageable) Bukkit.getItemFactory().getItemMeta(Material.FISHING_ROD);
        meta.setDamage(61);
        fishingRod.setItemMeta(meta);
    }

    FishingRodPad(Material enabledMaterial, Location fanLocation, int reEnableDelay, String name) {
        super(enabledMaterial, fanLocation, reEnableDelay, name, Material.FISHING_ROD);
    }

    @Override
    protected boolean shouldApplyEffect(Player player) {
        return !player.getInventory().contains(Material.FISHING_ROD);
    }

    @Override
    protected void applyEffect(Player player) {
        player.getInventory().addItem(fishingRod);
    }

}
