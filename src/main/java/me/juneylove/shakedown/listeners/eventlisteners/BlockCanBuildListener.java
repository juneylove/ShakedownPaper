package me.juneylove.shakedown.listeners.eventlisteners;

import me.juneylove.shakedown.mechanics.Respawn;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.util.BoundingBox;

import java.util.Collection;

public class BlockCanBuildListener implements Listener {

    @EventHandler
    public void onBlockCanBuild(BlockCanBuildEvent event) {

        if (event.getPlayer() == null) return;
        if (event.isBuildable()) return;

        // Block placement not allowed - entity/ies in the way
        // Check whether any non-spectators are present
        // If not, place the block anyway
        Block placedBlock = event.getBlock();
        BlockData priorData = placedBlock.getBlockData();

        placedBlock.setBlockData(event.getBlockData());
        BoundingBox boundingBox = placedBlock.getBoundingBox();
        placedBlock.setBlockData(priorData);

        Collection<Entity> entities = event.getBlock().getWorld().getNearbyEntities(boundingBox);

        boolean blocked = false;
        for (Entity entity : entities) {
            if (entity instanceof Player player) {
                if (!Respawn.IsTempSpec(player.getName())) {
                    blocked = true;
                    break;
                }
            } else {
                blocked = true;
                break;
            }
        }
        event.setBuildable(!blocked);

    }

}
