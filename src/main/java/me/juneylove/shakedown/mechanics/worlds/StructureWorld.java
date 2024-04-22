package me.juneylove.shakedown.mechanics.worlds;

import me.juneylove.shakedown.Main;
import me.juneylove.shakedown.utils.VoidGenerator;
import org.bukkit.*;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.structure.Structure;
import org.bukkit.util.BlockVector;
import org.bukkit.util.BoundingBox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class StructureWorld extends WorldSetting {

    Random random = new Random();
    public static File structureFolder = new File(Main.getInstance().getDataFolder(), "../../world/generated/minecraft/structures/");

    String folderName;
    World world;
    String structureName; // filename minus extension
    File structureFile;

    public BlockVector min;
    public BlockVector max;
    public int voidPlane = -64;

    NamespacedKey structureKey;
    Structure structure;

    static List<Integer> doNotClearModels = new ArrayList<>();

    static {
        doNotClearModels.add(321);
        doNotClearModels.add(322);
        doNotClearModels.add(323);
    }

    public StructureWorld(String filename) {
        this(new File(structureFolder, filename));
    }

    public StructureWorld(File structureFile) {

        this.structureFile = structureFile;

        structureName = this.structureFile.getName().substring(0, this.structureFile.getName().length() - 4); // assumes 3 letter extension
        folderName = structureName;

        loadStructure();
        voidPlane = 120;
        min = new BlockVector(-(structure.getSize().getBlockX() / 2) - 10, voidPlane, -(structure.getSize().getBlockZ() / 2) - 10); // 10 block buffer
        max = new BlockVector((structure.getSize().getBlockX() / 2) + 10, structure.getSize().getBlockY() + 128, (structure.getSize().getBlockZ() / 2) + 10);

        if (WorldSettings.worldFolderExists(folderName)) {

            world = Bukkit.getWorld(folderName);
            if (world == null) {
                world = WorldCreator.name(folderName).createWorld(); // Loads world if it already exists
            }

        } else {
            generateWorld();
        }

        setDefaultGameRules();

        this.lootChestLocations = new ArrayList<>();
        this.roundBarriers = new ArrayList<>();
        this.invulnerableRegions = new ArrayList<>();
        this.capturePoints = new ArrayList<>();
        this.movementDetectRegions = new ArrayList<>();

    }

    public StructureWorld(StructureWorld structureWorld) {

        this.lootChestLocations = structureWorld.lootChestLocations;
        this.roundBarriers = structureWorld.roundBarriers;
        this.invulnerableRegions = structureWorld.invulnerableRegions;
        this.capturePoints = structureWorld.capturePoints;
        this.movementDetectRegions = structureWorld.movementDetectRegions;
        this.spawnSetting = structureWorld.spawnSetting;

        this.folderName = structureWorld.folderName;
        this.world = structureWorld.world;
        this.structureName = structureWorld.structureName;
        this.structureFile = structureWorld.structureFile;

        this.min = structureWorld.min;
        this.max = structureWorld.max;
        this.voidPlane = structureWorld.voidPlane;

        this.structureKey = structureWorld.structureKey;
        this.structure = structureWorld.structure;

    }

    @Override
    public String getName() {
        return structureName;
    }

    @Override
    public String getFolderName() {
        return folderName;
    }

    @Override
    public World getWorld() {
        return world;
    }

    public void clearEntities() {

        Collection<Entity> entities = world.getNearbyEntities(new BoundingBox(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ()));
        for (Entity entity : entities) {
            if (!(entity instanceof Player)) {
                if (entity instanceof ItemDisplay display
                        && display.getItemStack() != null
                        && display.getItemStack().hasItemMeta()
                        && display.getItemStack().getItemMeta().hasCustomModelData()
                        && doNotClearModels.contains(display.getItemStack().getItemMeta().getCustomModelData())) {
                    continue;
                }
                entity.remove();

            }
        }

    }

    private void loadStructure() {

        structureKey = NamespacedKey.fromString(structureName, Main.getInstance());
        try {
            structure = Bukkit.getStructureManager().loadStructure(structureFile);
        } catch (IOException e) {
            return;
        }
        Bukkit.getStructureManager().registerStructure(structureKey, structure);

    }

    private void generateWorld() {

        world = WorldCreator.name(folderName).generator(new VoidGenerator()).createWorld();

        int subtractX = structure.getSize().getBlockX() / 2;
        int subtractZ = structure.getSize().getBlockZ() / 2;
        Location placeLoc = new Location(world, -subtractX, 128, -subtractZ);

        structure.place(placeLoc, true, StructureRotation.NONE, Mirror.NONE, 0, 1.0f, random);

    }

    private String findNewWorldName(String structureName) {

        String newName = structureName + "-1";
        if (!WorldSettings.worldFolderExists(newName)) return newName;

        int appendedNum = 1;
        while (WorldSettings.worldFolderExists(newName)) {
            appendedNum++;
            newName = structureName + "-" + appendedNum;
        }
        return newName;

    }

    private void setDefaultGameRules() {

        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
        world.setGameRule(GameRule.DISABLE_ELYTRA_MOVEMENT_CHECK, true);
        world.setGameRule(GameRule.DISABLE_RAIDS, true);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_ENTITY_DROPS, false);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.DO_INSOMNIA, false);
        world.setGameRule(GameRule.DO_MOB_LOOT, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
        world.setGameRule(GameRule.SEND_COMMAND_FEEDBACK, false);
        world.setGameRule(GameRule.SHOW_DEATH_MESSAGES, false);
        world.setGameRule(GameRule.SPAWN_RADIUS, 0);
        world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);

        world.setTime(6000);

    }

}
