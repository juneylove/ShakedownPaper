package me.juneylove.shakedown.listeners.eventlisteners;

import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;

public class BlockDamageListener implements Listener {

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        Material type = event.getBlock().getType();
        if (type.name().contains("CONCRETE") && !type.name().contains("POWDER")) {
            if (game.concreteInstabreak) {
                event.setInstaBreak(true);
            }
        }

    }

}
