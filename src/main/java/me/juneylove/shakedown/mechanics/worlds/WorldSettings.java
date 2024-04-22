package me.juneylove.shakedown.mechanics.worlds;

import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.ui.TextFormat;
import org.bukkit.*;
import org.bukkit.structure.Structure;

import java.io.File;
import java.util.*;

public class WorldSettings {

    protected static boolean worldFolderExists(String worldName) {

        File worldsFolder = Bukkit.getWorldContainer();
        if (worldsFolder.listFiles() != null) {

            for (File file : Objects.requireNonNull(worldsFolder.listFiles())) {
                if (file.getName().equals(worldName)) {
                    if (file.isDirectory()) return true;
                }
            }

        }
        return false;

    }

    public static void deregisterAllStructures() {

        Map<NamespacedKey,Structure> structures = Bukkit.getStructureManager().getStructures();
        for (NamespacedKey key : structures.keySet()) {
            Bukkit.getStructureManager().unregisterStructure(key);
        }

    }

    public static void addRoundBarriers(WorldSetting worldSetting, Collection<String> teams) {

        if (worldSetting.roundBarriers.size() == 0) return;

        int i = 0;
        for (String team : teams) {

            Material material;
            if (Games.CURRENT_GAME.isTeamGame) {
                material = TextFormat.glassColor(team);
            } else {
                material = Material.BARRIER;
            }
            List<Location> locations = worldSetting.roundBarriers.get(i);
            setBlocks(worldSetting.getWorld(), material, locations);

            i++;
            if (i >= worldSetting.roundBarriers.size()) break;

        }

    }

    public static void removeRoundBarriers(WorldSetting worldSetting) {

        if (worldSetting.roundBarriers.size() == 0) return;

        for (List<Location> locations : worldSetting.roundBarriers) {
            setBlocks(worldSetting.getWorld(), Material.AIR, locations);
        }

    }

    private static void setBlocks(World world, Material material, List<Location> locations) {

        for (Location location : locations) {

            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();

            world.getBlockAt(x, y, z).setType(material);

        }

    }

    public static void setTeamInvulnerability(WorldSetting worldSetting, Collection<String> teams) {

        if (worldSetting.invulnerableRegions.size() == 0) return;

        int i = 0;
        for (String team : teams) {

            worldSetting.invulnerableRegions.get(i).setTeam(team);

            i++;
            if (i >= worldSetting.invulnerableRegions.size()) break;

        }

    }

}
