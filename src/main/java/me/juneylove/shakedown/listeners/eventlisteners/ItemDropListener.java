package me.juneylove.shakedown.listeners.eventlisteners;

import me.juneylove.shakedown.control.MatchProgress;
import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.mechanics.KitSettings;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class ItemDropListener implements Listener {

    @EventHandler
    public void onItemDropEvent(PlayerDropItemEvent event) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        if (MatchProgress.kitSelectionIsActive()) {
            if (game.currentRound.kitSetting instanceof KitSettings.Selection selection) {
                selection.openKitSelect(event.getPlayer());
                event.setCancelled(true);
            }
        }

        if (!game.itemDropsEnabled) {
            event.setCancelled(true);
        }

        game.onPlayerDropItem(event);

    }

}
