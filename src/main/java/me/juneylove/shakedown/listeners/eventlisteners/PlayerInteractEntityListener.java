package me.juneylove.shakedown.listeners.eventlisteners;

import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class PlayerInteractEntityListener implements Listener {

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event) {

        Games.CURRENT_GAME.onPlayerInteractEntity(event); // TODO: move back to end once powerups implemented

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        if (!game.entityInteractsEnabled) {
            event.setCancelled(true);
        }

    }

}
