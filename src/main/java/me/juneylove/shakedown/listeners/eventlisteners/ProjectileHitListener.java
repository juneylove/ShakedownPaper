package me.juneylove.shakedown.listeners.eventlisteners;

import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.mechanics.PvpHandler;
import me.juneylove.shakedown.mechanics.Respawn;
import me.juneylove.shakedown.scoring.TeamManager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class ProjectileHitListener implements Listener {

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        Projectile projectile = event.getEntity();

        if (event.getHitEntity() != null && event.getHitEntity() instanceof Player target) {

            if (Respawn.IsTempSpec(target.getName()) || !TeamManager.isGamePlayer(target.getName())) {

                event.setCancelled(true);

            } else {

                String sourceIgn;
                if (projectile.getShooter() != null && projectile.getShooter() instanceof Player shooter) {
                    sourceIgn = shooter.getName();
                } else {
                    if (projectile.getMetadata("source").size() > 0) {
                        sourceIgn = projectile.getMetadata("source").get(0).asString();
                    } else {
                        sourceIgn = null;
                    }
                }

                if (TeamManager.isGamePlayer(target.getName()) && (sourceIgn == null || TeamManager.isGamePlayer(sourceIgn))) {
                    PvpHandler.onProjectileHit(event, sourceIgn, target);
                }

            }

        }

        game.onProjectileHit(event);

    }

}
