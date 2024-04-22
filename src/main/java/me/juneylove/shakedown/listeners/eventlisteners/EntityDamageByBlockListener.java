package me.juneylove.shakedown.listeners.eventlisteners;

import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.mechanics.InvulnerableRegion;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByBlockEvent;

public class EntityDamageByBlockListener implements Listener {

    @EventHandler
    public void onEntityDamageByBlockEvent(EntityDamageByBlockEvent event) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        if (event.getEntity() instanceof Player target) {
            if (InvulnerableRegion.isSpawnInvulnerable(target)) {
                event.setCancelled(true);
                return;
            }
        }

        if (!game.blockDamageEnabled) {
            event.setCancelled(true);
        }

    }

}
