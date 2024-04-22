package me.juneylove.shakedown.mechanics;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class MovementDetectRegion {

    public String name;

    List<String> alreadyWithinIgns = new ArrayList<>();

    int minX;
    int maxX;
    int minY;
    int maxY;
    int minZ;
    int maxZ;

    public MovementDetectRegion(String name, int x1, int x2, int y1, int y2, int z1, int z2) {

        this.name = name;

        this.minX = Math.min(x1, x2);
        this.maxX = Math.max(x1, x2) + 1;
        this.minY = Math.min(y1, y2);
        this.maxY = Math.max(y1, y2) + 1;
        this.minZ = Math.min(z1, z2);
        this.maxZ = Math.max(z1, z2) + 1;

    }

    public MovementDetectRegion(String name, Location corner1, Location corner2) {
        this(name, corner1.toVector(), corner2.toVector());
    }

    public MovementDetectRegion(String name, Vector corner1, Vector corner2) {

        this.name = name;

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

    public boolean justEntered(Player player) {

        Location location = player.getLocation();
        double x = location.x();
        double y = location.y();
        double z = location.z();

        boolean isWithin = minX < x && x < maxX
                        && minY < y && y < maxY
                        && minZ < z && z < maxZ;

        if (!isWithin) {
            alreadyWithinIgns.remove(player.getName());
            return false;
        }

        // Player is within region, check if they already were
        if (alreadyWithinIgns.contains(player.getName())) {
            return false;
        } else {
            alreadyWithinIgns.add(player.getName());
            return true;
        }

    }

}
