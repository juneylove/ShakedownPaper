package me.juneylove.shakedown.games.mobsmash;

import io.papermc.paper.event.entity.EntityLoadCrossbowEvent;
import me.juneylove.shakedown.ui.GUIFormat;
import me.juneylove.shakedown.ui.Models;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;

import java.util.HashMap;

public abstract class BowKit extends MobKit {

    double reloadDuration = 3.0;
    public int maxArrowAmount = 10;

    public boolean reloadNeeded = false;

    int arrowSlot = 44;

    @Override
    public void onLoadCrossbow(EntityLoadCrossbowEvent event) {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        arrowSlot = player.getInventory().first(Material.ARROW);
        int totalArrows = getTotalArrows(player);

        // defensive check
        if (totalArrows > maxArrowAmount) {
            player.getInventory().remove(Material.ARROW);
            player.getInventory().setItem(arrowSlot, new ItemStack(Material.ARROW, maxArrowAmount));
        }

        if (totalArrows == 1) { // last arrow
            player.getInventory().setItem(arrowSlot, GUIFormat.getMenuIcon(Models.ARROW0));
            event.setConsumeItem(false); // have to because otherwise it will be canceled by replacing the arrow
        }

    }

    public void onFireArrow() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        if (!player.getInventory().contains(Material.CROSSBOW)) { // bow kit

            arrowSlot = player.getInventory().first(Material.ARROW);
            int totalArrows = getTotalArrows(player);

            // defensive check
            if (totalArrows > maxArrowAmount) {
                player.getInventory().remove(Material.ARROW);
                player.getInventory().setItem(arrowSlot, new ItemStack(Material.ARROW, maxArrowAmount));
            }

            if (totalArrows == 1) { // last arrow
                player.getInventory().setItem(arrowSlot, GUIFormat.getMenuIcon(Models.ARROW0));
                reloadNeeded = true;
            }

        } else { // crossbow kit

            int totalArrows = getTotalArrows(player);

            if (totalArrows == 0) { // last arrow
                reloadNeeded = true;
            }

        }

    }

    public int getTotalArrows(Player player) {
        HashMap<Integer, ? extends ItemStack> arrows = player.getInventory().all(Material.ARROW);
        int totalArrows = 0;
        for (ItemStack stack : arrows.values()) {
            totalArrows += stack.getAmount();
        }
        return totalArrows;
    }

    public void onUseItem(PlayerInteractEvent event) {

        if (!event.getAction().isRightClick()) return;

        ItemStack itemInHand = event.getItem();
        if (itemInHand != null &&

            ((itemInHand.getType() == GUIFormat.menuSelectItem
                && itemInHand.hasItemMeta()
                && itemInHand.getItemMeta().getCustomModelData() == Models.ARROW0.num)

            || (itemInHand.getType() == Material.CROSSBOW
                && itemInHand.hasItemMeta()
                && !(((CrossbowMeta)itemInHand.getItemMeta()).hasChargedProjectiles())
                && !event.getPlayer().getInventory().contains(Material.ARROW))

            || (itemInHand.getType() == Material.BOW
                && !event.getPlayer().getInventory().contains(Material.ARROW))

            || (itemInHand.getType() == Material.ARROW
                && getTotalArrows(event.getPlayer()) < maxArrowAmount))) {

            startReload();

        }

    }

    public void startReload() {

        reloadNeeded = false;

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        Material weapon = Material.BOW;
        if (player.getInventory().contains(Material.CROSSBOW)) weapon = Material.CROSSBOW;
        player.setCooldown(weapon, (int) (reloadDuration * 20));

        player.getInventory().remove(Material.ARROW);
        player.getInventory().setItem(arrowSlot, new ItemStack(Material.ARROW, maxArrowAmount));

    }

}
