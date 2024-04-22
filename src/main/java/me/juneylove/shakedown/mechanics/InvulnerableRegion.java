package me.juneylove.shakedown.mechanics;

import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.mechanics.worlds.WorldSetting;
import me.juneylove.shakedown.scoring.TeamManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class InvulnerableRegion {

    Set<String> igns = new HashSet<>();

    int minX;
    int maxX;
    int minY;
    int maxY;
    int minZ;
    int maxZ;

    public InvulnerableRegion(int x1, int x2, int y1, int y2, int z1, int z2) {

        this.minX = Math.min(x1, x2);
        this.maxX = Math.max(x1, x2) + 1;
        this.minY = Math.min(y1, y2);
        this.maxY = Math.max(y1, y2) + 1;
        this.minZ = Math.min(z1, z2);
        this.maxZ = Math.max(z1, z2) + 1;

    }

    public InvulnerableRegion(Location corner1, Location corner2) {
        this(corner1.toVector(), corner2.toVector());
    }

    public InvulnerableRegion(Vector corner1, Vector corner2) {

        if (corner1.getBlockX() < corner2.getBlockX()) {
            this.minX = corner1.getBlockX();
            this.maxX = corner2.getBlockX() + 1;
        } else {
            this.minX = corner2.getBlockX();
            this.maxX = corner1.getBlockX() + 1;
        }

        if (corner1.getBlockY() < corner2.getBlockY()) {
            this.minY = corner1.getBlockY();
            this.maxY = corner2.getBlockY() + 1;
        } else {
            this.minY = corner2.getBlockY();
            this.maxY = corner1.getBlockY() + 1;
        }

        if (corner1.getBlockZ() < corner2.getBlockZ()) {
            this.minZ = corner1.getBlockZ();
            this.maxZ = corner2.getBlockZ() + 1;
        } else {
            this.minZ = corner2.getBlockZ();
            this.maxZ = corner1.getBlockZ() + 1;
        }

    }

    public void setTeam(String team) {
        this.igns = TeamManager.getMembers(team);
    }

    public boolean isInvulnerable(Player player) {

        Location location = player.getLocation();
        double x = location.x();
        double y = location.y();
        double z = location.z();

        return igns.contains(player.getName())
                && minX < x && x < maxX
                && minY < y && y < maxY
                && minZ < z && z < maxZ;

    }

    public static boolean isSpawnInvulnerable(Player player) {

        for (WorldSetting worldSetting : Games.CURRENT_GAME.currentRound.worldSettings) {

            if (worldSetting.getWorld().equals(player.getWorld())) {

                for (InvulnerableRegion region : worldSetting.invulnerableRegions) {

                    if (region.isInvulnerable(player)) return true;

                }

            }

        }

        return false;

    }

}
