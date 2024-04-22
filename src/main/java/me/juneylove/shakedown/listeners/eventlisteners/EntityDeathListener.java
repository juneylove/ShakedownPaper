package me.juneylove.shakedown.listeners.eventlisteners;

import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityDeathListener implements Listener {

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        if (!(event.getEntity() instanceof Player)) {

            if (!game.mobDropsEnabled) {
                event.getDrops().clear();
                event.setDroppedExp(0);
            }

        }

    }

}
