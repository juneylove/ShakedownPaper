package me.juneylove.shakedown.mechanics.abilities;

import me.juneylove.shakedown.Main;
import me.juneylove.shakedown.ui.GUIFormat;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class ElytraBoost extends BukkitRunnable {

    Player player;
    ItemStack priorChestplate;

    public ElytraBoost(Player player, ItemStack priorChestplate) {
        this.player = player;
        this.priorChestplate = priorChestplate;
    }

    @Override
    public void run() {
        player.getEquipment().setChestplate(priorChestplate);
    }

    public static void boost(Player player) {

        if (player.getCooldown(GUIFormat.menuSelectItem) != 0) return;

        ItemStack priorChestplate1 = player.getEquipment().getChestplate();
        player.getEquipment().setChestplate(new ItemStack(Material.ELYTRA), false);
        player.setGliding(true);
        player.fireworkBoost(new ItemStack(Material.FIREWORK_ROCKET));
        player.playSound(player, Sound.ITEM_ARMOR_EQUIP_ELYTRA, 1.0f, 1.0f);
        player.setCooldown(GUIFormat.menuSelectItem, 100);
        new ElytraBoost(player, priorChestplate1).runTaskLater(Main.getInstance(), 15);

    }

}
