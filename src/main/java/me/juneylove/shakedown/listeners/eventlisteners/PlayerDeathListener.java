package me.juneylove.shakedown.listeners.eventlisteners;

import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.mechanics.InvulnerableRegion;
import me.juneylove.shakedown.mechanics.PvpHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        Player target = event.getEntity();

        if (InvulnerableRegion.isSpawnInvulnerable(target)) {
            event.setCancelled(true);
            return;
        }

        Player source = event.getEntity().getKiller();

        PvpHandler.onKill(event, source, target);

    }

}
