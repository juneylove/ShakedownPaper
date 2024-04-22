package me.juneylove.shakedown.listeners.eventlisteners;

import io.papermc.paper.event.entity.EntityLoadCrossbowEvent;
import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class EntityLoadCrossbowListener implements Listener {

    @EventHandler
    public void onCrossbowLoad(EntityLoadCrossbowEvent event) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        if (event.getEntity() instanceof Player) {
            game.onPlayerLoadCrossbow(event);
        }

    }

}
