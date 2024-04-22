package me.juneylove.shakedown.listeners.eventlisteners;

import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.mechanics.Respawn;
import me.juneylove.shakedown.scoring.TeamManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupArrowEvent;

public class PlayerPickupArrowListener implements Listener {

    @EventHandler
    public void onPickupArrow(PlayerPickupArrowEvent event) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        if (Respawn.IsTempSpec(event.getPlayer().getName()) || !TeamManager.isGamePlayer(event.getPlayer().getName())) {
            event.setCancelled(true);
        }

    }

}
