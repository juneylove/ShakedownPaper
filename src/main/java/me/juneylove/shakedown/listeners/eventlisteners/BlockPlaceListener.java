package me.juneylove.shakedown.listeners.eventlisteners;

import me.juneylove.shakedown.Main;
import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.mechanics.LootChests;
import me.juneylove.shakedown.mechanics.Respawn;
import me.juneylove.shakedown.ui.GUIFormat;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Piston;
import org.bukkit.block.data.type.Sapling;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4d;
import org.joml.AxisAngle4f;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.List;

public class BlockPlaceListener implements Listener {

    @EventHandler
    public void onBlockPlacement(BlockPlaceEvent event) {

        // TEMP: use block displays as quasi-slabs, displayed over a cake
        // use block in offhand to determine what block display to use
        if (event.getBlockPlaced().getBlockData().getMaterial() == Material.CAKE
            && event.getPlayer().getInventory().getItemInOffHand().getType() != Material.AIR) {

            Location displayLoc = event.getBlockPlaced().getLocation().add(0.0005, -0.485, 0.0005);
            BlockDisplay blockDisplay = (BlockDisplay) displayLoc.getWorld().spawnEntity(displayLoc, EntityType.BLOCK_DISPLAY);
            blockDisplay.setBlock(Bukkit.createBlockData(event.getPlayer().getInventory().getItemInOffHand().getType()));
            blockDisplay.setBrightness(new Display.Brightness(15, 15));

            Transformation transformation = new Transformation(new Vector3f(), new AxisAngle4f(), new Vector3f(0.999f, 0.999f, 0.999f), new AxisAngle4f());
            blockDisplay.setTransformation(transformation);
            return;

        }

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        if (Respawn.IsTempSpec(event.getPlayer().getName())) {
            event.setCancelled(true);
            return;
        }

        Block block = event.getBlockPlaced();

        // FOR TESTING: add ability to place loot chests
        if (LootChests.IsLootChest(block.getType())) {

            block.getRelative(BlockFace.UP).setType(Material.LILY_PAD);

            // First, get chest level
            int chestLevel = LootChests.ChestLevel(block.getType());

            // Get front side (away from any blocks if possible)
            Material north = block.getRelative(BlockFace.NORTH).getType();
            Material south = block.getRelative(BlockFace.SOUTH).getType();
            Material east  = block.getRelative(BlockFace.EAST) .getType();
            Material west  = block.getRelative(BlockFace.WEST) .getType();
            int blockedSides = 0;
            for (Material side : new Material[]{north, south, east, west}) {
                if (side != Material.AIR) {
                    blockedSides++;
                }
            }
/*
            BlockFace front = null;
            switch (blockedSides) {
                case 3: // find the one open side
                case 2: // either will work, so first one we find
                    if (north == Material.AIR) front = BlockFace.NORTH;
                    if (south == Material.AIR) front = BlockFace.SOUTH;
                    if (east  == Material.AIR) front = BlockFace.EAST;
                    if (west  == Material.AIR) front = BlockFace.WEST;
                    break;
                case 1: // Find opposite of wall and make that the front
                    if (north != Material.AIR) front = BlockFace.SOUTH;
                    if (south != Material.AIR) front = BlockFace.NORTH;
                    if (east  != Material.AIR) front = BlockFace.WEST;
                    if (west  != Material.AIR) front = BlockFace.EAST;
                    break;
                default: // all open or all blocked (why?)
                    front = BlockFace.NORTH;
            }
 */
            BlockFace facing = event.getPlayer().getFacing();
            BlockFace front = switch (facing) {
                case NORTH -> BlockFace.SOUTH;
                default    -> BlockFace.NORTH;
                case EAST  -> BlockFace.WEST;
                case WEST  -> BlockFace.EAST;
            };

            LootChests.LootChest chest = new LootChests.LootChest(chestLevel, front);
            block.setType(chest.sapling);
            Sapling data = (Sapling) block.getBlockData();
            data.setStage(chest.stage);
            block.setBlockData(data);

        }

        /*
        if (game.restrictBlockPlacementByLocation && game.placementRegion != null) {

            if (game.placementRegion.size() == 6) {

                int x = block.getX();
                int y = block.getY();
                int z = block.getZ();

                int minx = game.placementRegion.get(0);
                int maxx = game.placementRegion.get(1);
                int miny = game.placementRegion.get(2);
                int maxy = game.placementRegion.get(3);
                int minz = game.placementRegion.get(4);
                int maxz = game.placementRegion.get(5);

                if (!(minx<=x && x<=maxx &&
                      miny<=y && y<=maxy &&
                      minz<=z && z<=maxz)) {

                    event.setCancelled(true);
                    return;

                }

            }

        }
        */
        String placerIgn = event.getPlayer().getName();

        switch (block.getType()) {

            case TNT:

                if (game.allowTntPlace) {

                    // Immediate tnt prime with specified ticks; save player name as metadata for damage considerations
                    if (game.immediateTntPrime) {

                        event.setCancelled(true);
                        removeOneTnt(event.getPlayer().getInventory());
                        event.getPlayer().updateInventory();

                        Location spawnLocation = block.getLocation().add(0.5, 0.5, 0.5);
                        TNTPrimed tntPrimed = (TNTPrimed) block.getWorld().spawnEntity(spawnLocation, EntityType.PRIMED_TNT);
                        tntPrimed.setFuseTicks(game.tntFuseTicks);
                        tntPrimed.setMetadata("source", new FixedMetadataValue(Main.getInstance(), placerIgn));

                    }

                } else {
                    event.setCancelled(true);
                }
                break;

            case BLACK_CONCRETE:
            case BLUE_CONCRETE:
            case BROWN_CONCRETE:
            case CYAN_CONCRETE:
            case GRAY_CONCRETE:
            case GREEN_CONCRETE:
            case LIGHT_BLUE_CONCRETE:
            case LIGHT_GRAY_CONCRETE:
            case LIME_CONCRETE:
            case MAGENTA_CONCRETE:
            case ORANGE_CONCRETE:
            case PINK_CONCRETE:
            case PURPLE_CONCRETE:
            case RED_CONCRETE:
            case WHITE_CONCRETE:
            case YELLOW_CONCRETE:

                if (game.allowConcretePlace) {

                    if (game.infiniteConcrete) {
                        maxConcrete(event.getPlayer().getInventory());
                    }

                } else {
                    event.setCancelled(true);
                }
                break;

            case COBWEB:

                if (!game.allowCobwebPlace) {
                    event.setCancelled(true);
                }
                break;

            default:

                if (!game.allowOtherBlockPlace) {
                    event.setCancelled(true);
                }

        }

        if (!event.isCancelled()) {
            Games.CURRENT_GAME.onBlockPlace(event.getPlayer(), block);
        }

    }

    private void maxConcrete(PlayerInventory inventory) {

        if (inventory.getItemInMainHand().getType().name().contains("CONCRETE") &&
           !inventory.getItemInMainHand().getType().name().contains("POWDER")) {

            ItemStack stack = inventory.getItemInMainHand();
            stack.setAmount(64);
            inventory.setItemInMainHand(stack);

        } else if (inventory.getItemInOffHand().getType().name().contains("CONCRETE") &&
                  !inventory.getItemInOffHand().getType().name().contains("POWDER")) {

            ItemStack stack = inventory.getItemInOffHand();
            stack.setAmount(64);
            inventory.setItemInOffHand(stack);

        }
    }

    private void removeOneTnt(PlayerInventory inventory) {

        if (inventory.getItemInMainHand().getType() == Material.TNT) {

            ItemStack stack = inventory.getItemInMainHand();
            stack.setAmount(stack.getAmount() - 1);
            inventory.setItemInMainHand(stack);

        } else if (inventory.getItemInOffHand().getType() == Material.TNT) {

            ItemStack stack = inventory.getItemInOffHand();
            stack.setAmount(stack.getAmount() - 1);
            inventory.setItemInOffHand(stack);

        }

    }

}
