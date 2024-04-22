package me.juneylove.shakedown.worlddefinitions;

import me.juneylove.shakedown.mechanics.*;
import me.juneylove.shakedown.mechanics.worlds.StructureWorld;
import me.juneylove.shakedown.ui.GUIFormat;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.BlockVector;
import org.bukkit.util.Transformation;

import java.util.ArrayList;
import java.util.List;

public class FenceDuelWorlds {

    public static class FenceDuelWorld extends StructureWorld {

        public FenceDuelWorld(String filename) {
            super(filename);
        }

        List<Location> redSpawns = new ArrayList<>();
        List<Location> blueSpawns = new ArrayList<>();

        public Location getRedSpawn(int i) {
            return redSpawns.get(i+5);
        }

        public Location getBlueSpawn(int i) {
            return blueSpawns.get(i+5);
        }

    }

    public FenceDuelWorld fenceDuel1() {

        FenceDuelWorld worldSetting = new FenceDuelWorld("fenceduel1.nbt");
        World world = worldSetting.getWorld();

        Location spawn1 = new Location(world, 11.5, 137, 0.5);
        spawn1.setYaw(-90f);
        Location spawn2 = new Location(world, 33.5, 137, 0.5);
        spawn2.setYaw(90f);

        worldSetting.spawnSetting = new Spawn().new OneTeamPerLocation(spawn1, spawn2);

        // replace structure blocks
        for (int x=-1; x<=1; x++) {
            for (int y=128; y<=129; y++) {
                world.getBlockAt(x, y, 0).setType(Material.AIR);
            }
        }

        List<Location> spawnBarriers1 = new ArrayList<>();
        List<Location> spawnBarriers2 = new ArrayList<>();
        for (int y=137; y<=139; y++) {

            for (int z=-2; z<=2; z++) {
                spawnBarriers2.add(new Location(world, 31, y, z));
                spawnBarriers2.add(new Location(world, 35, y, z));

                spawnBarriers1.add(new Location(world, 13, y, z));
                spawnBarriers1.add(new Location(world,  9, y, z));
            }

            for (int x=32; x<=34; x++) {
                spawnBarriers2.add(new Location(world, x, y, -2));
                spawnBarriers2.add(new Location(world, x, y,  2));
            }

            for (int x=10; x<=12; x++) {
                spawnBarriers1.add(new Location(world, x, y, -2));
                spawnBarriers1.add(new Location(world, x, y,  2));
            }

        }
        worldSetting.roundBarriers = List.of(spawnBarriers1, spawnBarriers2);


        final int voidDepth = 5;
        final int minZ = -13;
        final int maxZ = 13;
        worldSetting.movementDetectRegions.add(new MovementDetectRegion("void0",  16,  28, 132-voidDepth, 132, minZ, maxZ));

        worldSetting.movementDetectRegions.add(new MovementDetectRegion("void-1",   6,  15, 133-voidDepth, 133, minZ, maxZ));
        worldSetting.movementDetectRegions.add(new MovementDetectRegion("void-2",  -5,   4, 135-voidDepth, 135, minZ, maxZ));
        worldSetting.movementDetectRegions.add(new MovementDetectRegion("void-3", -16,  -7, 138-voidDepth, 138, minZ, maxZ));
        worldSetting.movementDetectRegions.add(new MovementDetectRegion("void-4", -27, -18, 142-voidDepth, 142, minZ, maxZ));
        worldSetting.movementDetectRegions.add(new MovementDetectRegion("void-5", -43, -29, 147-voidDepth, 147, minZ, maxZ));

        worldSetting.movementDetectRegions.add(new MovementDetectRegion("void1", 29,  39, 133-voidDepth, 133, minZ, maxZ));
        worldSetting.movementDetectRegions.add(new MovementDetectRegion("void2", 40,  50, 135-voidDepth, 135, minZ, maxZ));
        worldSetting.movementDetectRegions.add(new MovementDetectRegion("void3", 51,  61, 138-voidDepth, 138, minZ, maxZ));
        worldSetting.movementDetectRegions.add(new MovementDetectRegion("void4", 62,  72, 142-voidDepth, 142, minZ, maxZ));
        worldSetting.movementDetectRegions.add(new MovementDetectRegion("void5", 73,  87, 147-voidDepth, 147, minZ, maxZ));

        int islandHeight = 8;
        worldSetting.movementDetectRegions.add(new MovementDetectRegion("island0", 18,  26, 135, 135+islandHeight, minZ, maxZ));

        worldSetting.movementDetectRegions.add(new MovementDetectRegion("island-1",   7,  15, 136, 136+islandHeight, minZ, maxZ));
        worldSetting.movementDetectRegions.add(new MovementDetectRegion("island-2",  -4,   4, 138, 138+islandHeight, minZ, maxZ));
        worldSetting.movementDetectRegions.add(new MovementDetectRegion("island-3", -15,  -7, 141, 141+islandHeight, minZ, maxZ));
        worldSetting.movementDetectRegions.add(new MovementDetectRegion("island-4", -26, -18, 145, 145+islandHeight, minZ, maxZ));
        worldSetting.movementDetectRegions.add(new MovementDetectRegion("island-5", -43, -29, 150, 150+islandHeight, minZ, maxZ));

        worldSetting.movementDetectRegions.add(new MovementDetectRegion("island1", 29, 37, 136, 136+islandHeight, minZ, maxZ));
        worldSetting.movementDetectRegions.add(new MovementDetectRegion("island2", 40, 48, 138, 138+islandHeight, minZ, maxZ));
        worldSetting.movementDetectRegions.add(new MovementDetectRegion("island3", 51, 59, 141, 141+islandHeight, minZ, maxZ));
        worldSetting.movementDetectRegions.add(new MovementDetectRegion("island4", 62, 70, 145, 145+islandHeight, minZ, maxZ));
        worldSetting.movementDetectRegions.add(new MovementDetectRegion("island5", 73, 87, 150, 150+islandHeight, minZ, maxZ));

        int goalHeight = 7;
        worldSetting.movementDetectRegions.add(new MovementDetectRegion("goal1", -42, -39, 150, 150+goalHeight, -2, 2));
        worldSetting.movementDetectRegions.add(new MovementDetectRegion("goal2",  83,  86, 150, 150+goalHeight, -2, 2));


        worldSetting.redSpawns.add(new Location(world, -36, 151, 0));
        worldSetting.redSpawns.add(new Location(world, -25, 146, 0));
        worldSetting.redSpawns.add(new Location(world, -14, 142, 0));
        worldSetting.redSpawns.add(new Location(world,  -3, 139, 0));
        worldSetting.redSpawns.add(new Location(world,   8, 137, 0));
        worldSetting.redSpawns.add(new Location(world,  19, 136, 0));
        worldSetting.redSpawns.add(new Location(world,  30, 137, 0));
        worldSetting.redSpawns.add(new Location(world,  41, 139, 0));
        worldSetting.redSpawns.add(new Location(world,  52, 142, 0));
        worldSetting.redSpawns.add(new Location(world,  63, 146, 0));
        worldSetting.redSpawns.add(new Location(world,  74, 151, 0));

        worldSetting.blueSpawns.add(new Location(world, -30, 151, 0));
        worldSetting.blueSpawns.add(new Location(world, -19, 146, 0));
        worldSetting.blueSpawns.add(new Location(world,  -8, 142, 0));
        worldSetting.blueSpawns.add(new Location(world,   3, 139, 0));
        worldSetting.blueSpawns.add(new Location(world,  14, 137, 0));
        worldSetting.blueSpawns.add(new Location(world,  25, 136, 0));
        worldSetting.blueSpawns.add(new Location(world,  36, 137, 0));
        worldSetting.blueSpawns.add(new Location(world,  47, 139, 0));
        worldSetting.blueSpawns.add(new Location(world,  58, 142, 0));
        worldSetting.blueSpawns.add(new Location(world,  69, 146, 0));
        worldSetting.blueSpawns.add(new Location(world,  80, 151, 0));


        worldSetting.min = new BlockVector(-43, 128, minZ);
        worldSetting.max = new BlockVector( 73, 159, maxZ);

        worldSetting.clearEntities();

        Location loc1 = new Location(world, 83.5, 153.5, 0.5);
        loc1.setYaw(-90f);
        ItemDisplay goal1 = (ItemDisplay) world.spawnEntity(loc1, EntityType.ITEM_DISPLAY);
        ItemStack stack1 = new ItemStack(GUIFormat.menuSelectItem);
        ItemMeta meta1 = Bukkit.getItemFactory().getItemMeta(GUIFormat.menuSelectItem);
        meta1.setCustomModelData(401);
        stack1.setItemMeta(meta1);
        goal1.setItemStack(stack1);
        Transformation transformation1 = goal1.getTransformation();
        transformation1.getScale().set(5.0);
        goal1.setTransformation(transformation1);

        Location loc2 = new Location(world, -38.5, 153.5, 0.5);
        loc2.setYaw(90f);
        ItemDisplay goal2 = (ItemDisplay) world.spawnEntity(loc2, EntityType.ITEM_DISPLAY);
        ItemStack stack2 = new ItemStack(GUIFormat.menuSelectItem);
        ItemMeta meta2 = Bukkit.getItemFactory().getItemMeta(GUIFormat.menuSelectItem);
        meta2.setCustomModelData(401);
        stack2.setItemMeta(meta2);
        goal2.setItemStack(stack2);
        Transformation transformation2 = goal2.getTransformation();
        transformation2.getScale().set(5.0);
        goal2.setTransformation(transformation2);

        return worldSetting;

    }

}
