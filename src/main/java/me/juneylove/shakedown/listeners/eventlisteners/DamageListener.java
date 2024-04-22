package me.juneylove.shakedown.listeners.eventlisteners;

import me.juneylove.shakedown.control.MatchProgress;
import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.mechanics.InvulnerableRegion;
import me.juneylove.shakedown.mechanics.Respawn;
import me.juneylove.shakedown.scoring.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class DamageListener implements Listener {

    @EventHandler
    public void onDamageEvent(EntityDamageEvent event) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        if (!MatchProgress.playIsActive()) {
            event.setCancelled(true);
            return;
        }

        if (event.getEntity() instanceof Player player) {

            String ign = player.getName();

            if (Respawn.IsTempSpec(ign) || !TeamManager.isGamePlayer(ign)) {
                event.setCancelled(true);
                return;
            }

            if (InvulnerableRegion.isSpawnInvulnerable(player)) {
                event.setCancelled(true);
                return;
            }

            switch (event.getCause()) {

                case FALL:
                    if (!game.fallDamageEnabled) {
                        event.setCancelled(true);
                    }
                    break;

                case BLOCK_EXPLOSION:
                    // tbd

            }

        }

        game.onEntityDamage(event);

    }

}
