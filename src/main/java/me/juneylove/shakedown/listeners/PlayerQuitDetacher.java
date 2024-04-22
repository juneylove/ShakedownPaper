package me.juneylove.shakedown.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitDetacher implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        ClientboundHandler.detach(event.getPlayer());
    }

}
