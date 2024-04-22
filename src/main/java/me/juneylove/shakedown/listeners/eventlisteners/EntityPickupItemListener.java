package me.juneylove.shakedown.listeners.eventlisteners;

import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.mechanics.Respawn;
import me.juneylove.shakedown.scoring.TeamManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class EntityPickupItemListener implements Listener {

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent event) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        if (event.getEntity() instanceof Player player) {
            if (Respawn.IsTempSpec(player.getName()) || !TeamManager.isGamePlayer(player.getName())) {
                event.setCancelled(true);
            }
        } else {
            // For now, cancel any non-player entity picking up any items (could add a gamerule later)
            event.setCancelled(true);
        }

    }

}
