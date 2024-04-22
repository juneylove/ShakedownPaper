package me.juneylove.shakedown.listeners.eventlisteners;

import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EntityChangeBlockListener implements Listener {

    @EventHandler
    public void onEntityChangeBlock(org.bukkit.event.entity.EntityChangeBlockEvent event) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        // prevent silverfish from entering/leaving stone blocks
        if (event.getEntityType() == EntityType.SILVERFISH) {
            event.setCancelled(true);
        }

    }

}
