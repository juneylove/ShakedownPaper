package me.juneylove.shakedown.listeners.eventlisteners;

import org.bukkit.block.data.type.BubbleColumn;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;

public class BlockFromToListener implements Listener {

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {

        if (event.getBlock().getBlockData() instanceof BubbleColumn) {
            event.setCancelled(true);
        }

    }

}
