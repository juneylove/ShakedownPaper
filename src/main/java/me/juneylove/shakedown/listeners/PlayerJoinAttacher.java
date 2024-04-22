package me.juneylove.shakedown.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinAttacher implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        ClientboundHandler.attach(event.getPlayer());
    }

}
