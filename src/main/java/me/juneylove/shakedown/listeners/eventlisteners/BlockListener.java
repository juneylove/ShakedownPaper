package me.juneylove.shakedown.listeners.eventlisteners;

import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.mechanics.LootChests;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

public class BlockListener implements Listener {

    @EventHandler
    public void onBlockEvent(BlockPhysicsEvent event) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        Material sourceBlockType = event.getBlock().getType();

        if (LootChests.IsLootChest(sourceBlockType)) {
            event.setCancelled(true);
            return;
        }

        for (BlockFace face : new BlockFace[]{BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {

            Block adjacentBlock = event.getBlock().getRelative(face);

            if (LootChests.IsLootChest(adjacentBlock.getType())) {
                event.setCancelled(true);
                break;
            }

        }

    }

}
