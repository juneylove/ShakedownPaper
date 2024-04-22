package me.juneylove.shakedown.mechanics;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.Random;

public class SpawnRegion {

    private Random random = null;

    public interface ISpawnRegion {
        Location GetRandom();
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class XYZRegion implements ISpawnRegion {

        World world;

        int minX;
        int maxX;
        int minY;
        int maxY;
        int minZ;
        int maxZ;

        public XYZRegion(World world, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {

            if (random == null) random = new Random();

            this.world = world;

            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
            this.minZ = minZ;
            this.maxZ = maxZ;

        }

        public XYZRegion(String worldName, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
            this(Bukkit.getWorld(worldName), minX, maxX, minY, maxY, minZ, maxZ);
        }

        @Override
        public Location GetRandom() {

            int x = random.nextInt(minX, maxX);
            int z = random.nextInt(minZ, maxZ);
            int y = GetSpawnableY(x, z, minY, maxY);

            return new Location(world, x+0.5, y, z+0.5);

        }

        // Looks for 3 consecutive air blocks, starting from the top and working down
        private int GetSpawnableY(int x, int z, int min, int max) {

            boolean block2AboveIsAir = false;
            boolean block1AboveIsAir = false;

            for (int y = max; y >= min; y--) {

                boolean currentBlockIsAir = (world.getBlockAt(x, y, z).getType() == Material.AIR);

                if (currentBlockIsAir && block1AboveIsAir && block2AboveIsAir) return y;

                // shift all up by one
                block2AboveIsAir = block1AboveIsAir;
                block1AboveIsAir = currentBlockIsAir;

            }

            // min will probably usually be the floor so return that if none found? idk lol
            return min;

        }

    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class XZRegion implements ISpawnRegion {

        World world;

        int minX;
        int maxX;
        int minZ;
        int maxZ;

        public XZRegion(World world, int minX, int maxX, int minZ, int maxZ) {

            if (random == null) random = new Random();

            this.world = world;

            this.minX = minX;
            this.maxX = maxX;
            this.minZ = minZ;
            this.maxZ = maxZ;

        }

        public XZRegion(String worldName, int minX, int maxX, int minY, int maxY) {
            this(Bukkit.getWorld(worldName), minX, maxX, minY, maxY);
        }

        @Override
        public Location GetRandom() {

            int x = random.nextInt(minX, maxX);
            int z = random.nextInt(minZ, maxZ);
            int y = world.getHighestBlockYAt(x, z) + 1;

            return new Location(world, x+0.5, y, z+0.5);

        }

    }

}
