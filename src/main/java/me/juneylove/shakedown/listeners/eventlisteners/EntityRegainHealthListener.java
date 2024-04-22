package me.juneylove.shakedown.listeners.eventlisteners;

import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;

public class EntityRegainHealthListener implements Listener {

    @EventHandler
    public void onRegainHealth(EntityRegainHealthEvent event) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        if (event.getEntity() instanceof Player player) {

            if (!game.hungerEnabled) {

                player.setSaturation(0f);

            }

        }

    }

}
