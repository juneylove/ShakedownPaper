package me.juneylove.shakedown.games.rapidodge.powerups;

import me.juneylove.shakedown.games.rapidodge.AbstractPowerup;
import me.juneylove.shakedown.games.rapidodge.PowerupManager;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class SwiftSneakPowerup extends AbstractPowerup {

    ItemStack leggings = new ItemStack(Material.LEATHER_LEGGINGS);

    {
        leggings.addEnchantment(Enchantment.SWIFT_SNEAK, 3);
        durationTicks = PowerupManager.swiftSneakDuration;
    }

    @Override
    protected void start() {
        player.getInventory().setItem(EquipmentSlot.LEGS, leggings);
    }

    @Override
    protected void end() {
        player.getInventory().remove(Material.LEATHER_LEGGINGS);
    }

}
