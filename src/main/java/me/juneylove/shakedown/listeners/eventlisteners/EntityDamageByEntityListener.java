package me.juneylove.shakedown.listeners.eventlisteners;

import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.mechanics.PvpHandler;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageByEntityListener implements Listener {

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        if (event.getEntity() instanceof Player target) {

            if (event.getDamager() instanceof Explosive || event.getDamager() instanceof Projectile || event.getDamager() instanceof Player) {

                if (!game.pvpEnabled) {
                    event.setCancelled(true);
                    return;
                }

                if (event.getDamager() instanceof Player source) {

                    PvpHandler.onDamage(event, source, target);

                } else if (event.getDamager() instanceof Explosive explosive) {

                    PvpHandler.onExplosionDamage(event, explosive, target);

                } else { // Damager must be projectile in this case

                    PvpHandler.onProjectileDamage(event, (Projectile) event.getDamager(), target);

                }

            }

        }

        game.onEntityDamageByEntity(event);

    }

}
