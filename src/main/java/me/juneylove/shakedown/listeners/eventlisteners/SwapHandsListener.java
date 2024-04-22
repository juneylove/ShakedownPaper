package me.juneylove.shakedown.listeners.eventlisteners;

import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class SwapHandsListener implements Listener {

    @EventHandler
    public void onHandSwap(PlayerSwapHandItemsEvent event) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        game.onPlayerSwapHandItems(event);

    }

}
