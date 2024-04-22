package me.juneylove.shakedown.listeners.eventlisteners;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class JumpListener implements Listener {

    @EventHandler
    public void onJump(PlayerJumpEvent event) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        game.onPlayerJump(event);

    }

}
