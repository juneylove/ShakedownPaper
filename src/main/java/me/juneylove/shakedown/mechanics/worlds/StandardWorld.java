package me.juneylove.shakedown.mechanics.worlds;

import me.juneylove.shakedown.mechanics.worlds.WorldSetting;
import me.juneylove.shakedown.mechanics.worlds.WorldSettings;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.util.ArrayList;

public class StandardWorld extends WorldSetting {

    String folderName;
    World world;

    public StandardWorld(String folderName) {

        if (WorldSettings.worldFolderExists(folderName)) {
            this.folderName = folderName;
            this.world = Bukkit.getWorld(folderName);
            WorldCreator.name(folderName).createWorld(); // loads existing world

            this.lootChestLocations = new ArrayList<>();
            this.roundBarriers = new ArrayList<>();
            this.invulnerableRegions = new ArrayList<>();
            this.capturePoints = new ArrayList<>();
            this.movementDetectRegions = new ArrayList<>();
        }

    }

    public StandardWorld(World world) {
        this(world.getName());
    }

    @Override
    public String getName() {
        return folderName;
    }

    @Override
    public String getFolderName() {
        return folderName;
    }

    @Override
    public World getWorld() {
        return world;
    }

}
