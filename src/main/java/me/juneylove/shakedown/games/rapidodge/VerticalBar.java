package me.juneylove.shakedown.games.rapidodge;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.Piston;

public class VerticalBar extends AbstractBar {

    int tickCount = 0;
    int delay = 0;
    Location baseLocation;
    BlockFace direction;

    public VerticalBar(Location location, BlockFace blockFace, int delay) {

        if (blockFace.isCartesian()) {
            if (blockFace == BlockFace.DOWN || blockFace == BlockFace.UP) return;
        } else {
            return;
        }

        this.delay = delay;
        baseLocation = location;
        direction = blockFace;

        Material stickMaterial;
        if (blockFace == BlockFace.EAST || blockFace == BlockFace.WEST) {

            if (Math.abs(location.getBlockZ()%2) == 1) {
                stickMaterial = Material.HONEY_BLOCK;
            } else {
                stickMaterial = Material.SLIME_BLOCK;
            }

        } else { // north/south

            if (Math.abs(location.getBlockX()%2) == 1) {
                stickMaterial = Material.HONEY_BLOCK;
            } else {
                stickMaterial = Material.SLIME_BLOCK;
            }

        }

        // if there's something in the way of this bar spawning, just delete it
        // should only happen late game when there's plenty of other bars
         if (baseLocation.getBlock().getRelative(BlockFace.UP, height-1).getRelative(direction, 2).getType() != Material.AIR) {
             BarManager.activeBars.remove(this);
             return;
         }

        baseLocation.getBlock().setType(Material.AIR);
        Block piston = baseLocation.getBlock().getRelative(BlockFace.UP, height-1);
        piston.setType(Material.PISTON);
        Piston blockData = (Piston) piston.getBlockData();
        blockData.setFacing(direction);
        piston.setBlockData(blockData);

        for (int i=0; i<height; i++) {
            piston.getRelative(direction).getRelative(BlockFace.DOWN, i).setType(stickMaterial);
            piston.getRelative(direction, 2).getRelative(BlockFace.DOWN, i).setType(Material.BLACK_GLAZED_TERRACOTTA);
        }

    }

    public void run() {

        int ticksSinceLaunch = tickCount-delay;
        int distance = ticksSinceLaunch/moveInterval;

        if (distance >= maxDistance) {

            remove();
            return;

        }

        if (ticksSinceLaunch == -9) {

            Block piston = baseLocation.getBlock().getRelative(BlockFace.UP, height-1);
            for (int i=0; i<height; i++) {
                piston.getRelative(direction, 2).getRelative(BlockFace.DOWN, i).setType(Material.GRAY_GLAZED_TERRACOTTA);
            }

        } else if (ticksSinceLaunch == -6) {

            Block piston = baseLocation.getBlock().getRelative(BlockFace.UP, height-1);
            for (int i=0; i<height; i++) {
                piston.getRelative(direction, 2).getRelative(BlockFace.DOWN, i).setType(Material.LIGHT_GRAY_GLAZED_TERRACOTTA);
            }

        } else if (ticksSinceLaunch == -3) {

            Block piston = baseLocation.getBlock().getRelative(BlockFace.UP, height-1);
            for (int i=0; i<height; i++) {
                piston.getRelative(direction, 2).getRelative(BlockFace.DOWN, i).setType(Material.WHITE_GLAZED_TERRACOTTA);
            }

        } else if (ticksSinceLaunch == 0) {

            Block piston = baseLocation.getBlock().getRelative(BlockFace.UP, height-1);
            for (int i=0; i<height; i++) {
                piston.getRelative(direction, 2).getRelative(BlockFace.DOWN, i).setType(Material.ORANGE_GLAZED_TERRACOTTA);
            }

        }

        if (ticksSinceLaunch >= 0) { // move across platform

            if (ticksSinceLaunch % moveInterval == 0) {

                Block blockInFront = baseLocation.getBlock().getRelative(BlockFace.UP, height - 1).getRelative(direction, distance+3);
                if (blockInFront.getType() != Material.AIR) {
                    // bar passing in front of this one - wait to move until it's passed
                    return; // *important* returns before incrementing tickCount
                }

                Block topStickBlock = baseLocation.getBlock().getRelative(BlockFace.UP, height - 1).getRelative(direction, distance+1);
                BlockFace side1;
                BlockFace side2;
                if (direction == BlockFace.EAST || direction == BlockFace.WEST) {
                    side1 = BlockFace.NORTH;
                    side2 = BlockFace.SOUTH;
                } else {
                    side1 = BlockFace.EAST;
                    side2 = BlockFace.WEST;
                }
                if (topStickBlock.getRelative(side1).getType() == Material.PISTON
                    || topStickBlock.getRelative(side2).getType() == Material.PISTON) {
                    // also wait if we're stuck to a piston on the side
                    return; // *important* returns before incrementing tickCount
                }

                BlockFace opposite = direction.getOppositeFace();
                Block button = baseLocation.getBlock().getRelative(BlockFace.UP, height - 1).getRelative(direction, distance).getRelative(opposite);
                Directional buttonData = (Directional) Bukkit.createBlockData(Material.STONE_BUTTON);
                buttonData.setFacing(opposite);
                ((Powerable) buttonData).setPowered(true);
                button.setBlockData(buttonData);

            } else if (ticksSinceLaunch % moveInterval == 1) {

                BlockFace opposite = direction.getOppositeFace();

                Block oldPiston = baseLocation.getBlock().getRelative(BlockFace.UP, height - 1).getRelative(direction, distance);
                Block oldButton = oldPiston.getRelative(opposite);
                oldButton.setType(Material.AIR);
                oldPiston.setType(Material.AIR);

                oldPiston.getRelative(direction).setType(Material.PISTON);
                Piston pistonData = (Piston) Bukkit.createBlockData(Material.PISTON);
                pistonData.setFacing(direction);
                oldPiston.getRelative(direction).setBlockData(pistonData);

                oldPiston.setType(Material.STONE_BUTTON);
                Directional buttonData = (Directional) Bukkit.createBlockData(Material.STONE_BUTTON);
                buttonData.setFacing(opposite);
                oldPiston.setBlockData(buttonData);

            }

        }

        tickCount++;

    }

    protected void remove() {

        int ticksSinceLaunch = tickCount-delay;
        int distance = ticksSinceLaunch/moveInterval;

        Block button = baseLocation.getBlock().getRelative(BlockFace.UP, height - 1).getRelative(direction, distance -1);
        button.setType(Material.AIR);
        Block piston = baseLocation.getBlock().getRelative(BlockFace.UP, height - 1).getRelative(direction, distance);
        piston.setType(Material.AIR);

        for (int i=0; i<height; i++) {
            piston.getRelative(direction).getRelative(BlockFace.DOWN, i).setType(Material.AIR);
            piston.getRelative(direction, 2).getRelative(BlockFace.DOWN, i).setType(Material.AIR);
        }

        BarManager.activeBars.remove(this);

    }


}
