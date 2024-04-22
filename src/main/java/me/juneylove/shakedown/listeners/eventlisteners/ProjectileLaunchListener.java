package me.juneylove.shakedown.listeners.eventlisteners;

import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;

public class ProjectileLaunchListener implements Listener {

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        if (event.getEntity() instanceof Arrow arrow) {
            if (arrow.getShooter() instanceof Player shooter) {

                if (game.infiniteArrows) {
                    arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                    refillArrows(shooter);
                }

            }
        }

        game.onProjectileLaunch(event);

    }

    private void refillArrows(Player player) {

        int slot = player.getInventory().first(Material.ARROW);
        player.getInventory().remove(Material.ARROW); // prevent splitting stack to get more arrows by removing all arrows first
        player.getInventory().setItem(slot, new ItemStack(Material.ARROW, 64));

    }

}
