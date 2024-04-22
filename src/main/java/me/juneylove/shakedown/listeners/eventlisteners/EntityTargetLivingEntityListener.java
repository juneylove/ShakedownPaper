package me.juneylove.shakedown.listeners.eventlisteners;

import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.mechanics.Respawn;
import me.juneylove.shakedown.scoring.TeamManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

public class EntityTargetLivingEntityListener implements Listener {

    @EventHandler
    public void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent event) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        if (event.getTarget() instanceof Player target) {

            String targetIgn = target.getName();

            if (Respawn.IsTempSpec(targetIgn) || !TeamManager.isGamePlayer(targetIgn)) {
                event.setCancelled(true);
                return;
            }

            if (event.getEntity().hasMetadata("source")) {

                String sourceIgn = event.getEntity().getMetadata("source").get(0).asString();
                String sourceTeam = TeamManager.getTeam(sourceIgn);

                if (sourceTeam.equals(TeamManager.getTeam(targetIgn))) {
                    event.setCancelled(true);
                }

            }

        }

    }

}
