package me.juneylove.shakedown.games.chorusvolley;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class ArrowPad extends PowerupPad {

    int maxArrows = 4;

    ArrowPad(Material enabledMaterial, Location fanLocation, int reEnableDelay, String name) {
        super(enabledMaterial, fanLocation, reEnableDelay, name, Material.ARROW);
    }

    @Override
    protected boolean shouldApplyEffect(Player player) {

        HashMap<Integer, ? extends ItemStack> arrows = player.getInventory().all(Material.ARROW);

        int total = 0;
        for (ItemStack stack : arrows.values()) {
            total += stack.getAmount();
        }

        return total < maxArrows;

    }

    @Override
    protected void applyEffect(Player player) {
        player.getInventory().addItem(new ItemStack(Material.ARROW, 1));
    }

}
