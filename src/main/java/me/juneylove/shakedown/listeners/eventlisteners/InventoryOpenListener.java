package me.juneylove.shakedown.listeners.eventlisteners;

import me.juneylove.shakedown.control.MatchProgress;
import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;

public class InventoryOpenListener implements Listener {

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        InventoryType type = event.getInventory().getType();

        if (MatchProgress.kitSelectionIsActive()) {
            event.getView().setCursor(null);
            return;
        }

        if (type == InventoryType.CHEST) {

            if (!game.chestInteractsEnabled) {
                event.setCancelled(true);
            }

        } else {

            // May add a game option for this later, but for now disallow anything that's not a chest
            //event.setCancelled(true); disabled for testing

        }

    }

}
