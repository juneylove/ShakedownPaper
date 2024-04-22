package me.juneylove.shakedown.mechanics;

import me.juneylove.shakedown.scoring.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.*;

public class Spawn {

    Random random = null;

    //=========================================================================

    public interface SpawnSetting {
        HashMap<String, Location> GetSpawnLocations(String... igns);
        HashMap<String, Location> GetSpawnLocations(List<String> igns);
    }

    //=========================================================================

    // Used for something like the lobby where everyone spawns in one place
    // If multiple people, spawn everyone in a circle around global spawn
    public class WorldDefault implements SpawnSetting {

        World world;

        public WorldDefault(World world) {
            this.world = world;
        }

        public WorldDefault(String worldName) {
            this.world = Bukkit.getWorld(worldName);
        }

        @Override
        public HashMap<String, Location> GetSpawnLocations(String... igns) {

            List<String> players = new ArrayList<>();
            for (String ign : igns) {
                if (Bukkit.getPlayer(ign) != null) players.add(ign);
            }

            if (players.size() == 0) return new HashMap<>();

            if (players.size() == 1) {

                HashMap<String, Location> locations = new HashMap<>();
                locations.put(players.get(0), world.getSpawnLocation().add(0.5, 0.05, 0.5));
                return locations;

            }  else {
                return DistributeAroundPoint(world.getSpawnLocation().add(0.5, 0.05, 0.5), players);
            }

        }

        @Override
        public HashMap<String, Location> GetSpawnLocations(List<String> igns) {
            return GetSpawnLocations(igns.toArray(new String[0]));
        }

    }

    // [REGION] All players spawn in random locations
    public class RandomIndividual implements SpawnSetting {

        List<SpawnRegion.ISpawnRegion> regions;

        public RandomIndividual(List<SpawnRegion.ISpawnRegion> regions) {
            if (random == null) random = new Random();
            this.regions = regions;
        }

        public RandomIndividual(SpawnRegion.ISpawnRegion... regions) {
            if (random == null) random = new Random();
            this.regions = Arrays.asList(regions);
        }

        @Override
        public HashMap<String, Location> GetSpawnLocations(String... igns) {

            HashMap<String, Location> locations = new HashMap<>();

            for (String ign : igns) {

                int index = random.nextInt(regions.size());
                Location location = regions.get(index).GetRandom();
                locations.put(ign, location);

            }

            return locations;

        }

        @Override
        public HashMap<String, Location> GetSpawnLocations(List<String> igns) {
            return GetSpawnLocations(igns.toArray(new String[0]));
        }

    }

    // [REGION] Random location chosen for each team; team stays together
    public class RandomByTeam implements SpawnSetting {

        List<SpawnRegion.ISpawnRegion> regions;

        public RandomByTeam(List<SpawnRegion.ISpawnRegion> regions) {
            if (random == null) random = new Random();
            this.regions = regions;
        }

        public RandomByTeam(SpawnRegion.ISpawnRegion... regions) {
            if (random == null) random = new Random();
            this.regions = Arrays.asList(regions);
        }

        @Override
        public HashMap<String, Location> GetSpawnLocations(String... igns) {

            List<String> teams = new ArrayList<>();

            for (String ign : igns) {
                String team = TeamManager.getTeam(ign);
                if (!teams.contains(team)) {
                    teams.add(team);
                }
            }

            HashMap<String, Location> locations = new HashMap<>();

            for (String team : teams) {

                int index = random.nextInt(regions.size());
                Location location = regions.get(index).GetRandom();
                locations.putAll(DistributeAroundPoint(location, TeamManager.getMembers(team)));

            }

            // above code includes all members from all teams mentioned in the input list
            // remove anyone not specifically in the input list
            List<String> ignList = Arrays.asList(igns);
            for (String ign : locations.keySet()) {
                if (!ignList.contains(ign)) locations.remove(ign);
            }

            return locations;

        }

        @Override
        public HashMap<String, Location> GetSpawnLocations(List<String> igns) {
            return GetSpawnLocations(igns.toArray(new String[0]));
        }

    }

    // [LOCATION LIST] Self-explanatory
    @SuppressWarnings("InnerClassMayBeStatic")
    public class OnePlayerPerLocation implements SpawnSetting {

        List<Location> locationsList;

        public OnePlayerPerLocation(List<Location> locations) {
            this.locationsList = locations;
        }

        public OnePlayerPerLocation(Location... locations) {
            this.locationsList = Arrays.asList(locations);
        }

        @Override
        public HashMap<String, Location> GetSpawnLocations(String... igns) {

            HashMap<String, Location> locations = new HashMap<>();

            int i = 0;
            for (String ign : igns) {
                locations.put(ign, locationsList.get(i % locationsList.size()));
                i++;
            }

            return locations;

        }

        @Override
        public HashMap<String, Location> GetSpawnLocations(List<String> igns) {
            return GetSpawnLocations(igns.toArray(new String[0]));
        }

    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class OneListPerTeam implements SpawnSetting {

        List<List<Location>> locations;

        public OneListPerTeam(List<List<Location>> locations) {
            this.locations = locations;
        }

        @Override
        public HashMap<String, Location> GetSpawnLocations(String... igns) {

            HashMap<String, Location> spawnLocations = new HashMap<>();

            Set<String> teams = new HashSet<>();
            for (String ign : igns) {
                teams.add(TeamManager.getTeam(ign));
            }

            int i = 0;
            for (String team : teams) {

                List<Location> teamLocations = locations.get(i % locations.size());

                int j = 0;
                for (String ign : TeamManager.getMembers(team)) {
                    spawnLocations.put(ign, teamLocations.get(j % teamLocations.size()).add(0.5, 0, 0.5));
                    j++;
                }

                i++;

            }

            return spawnLocations;

        }

        @Override
        public HashMap<String, Location> GetSpawnLocations(List<String> igns) {
            return GetSpawnLocations(igns.toArray(new String[0]));
        }

    }

    // [LOCATION LIST] Spawn players in a circle around the given point
    public class OneTeamPerLocation implements SpawnSetting {

        List<Location> locationsList;

        public OneTeamPerLocation(List<Location> locations) {
            this.locationsList = locations;
        }

        public OneTeamPerLocation(Location... locations) {
            this.locationsList = Arrays.asList(locations);
        }

        @Override
        public HashMap<String, Location> GetSpawnLocations(String... igns) {

            List<String> teams = new ArrayList<>();

            for (String ign : igns) {
                String team = TeamManager.getTeam(ign);
                if (!teams.contains(team)) {
                    teams.add(team);
                }
            }

            HashMap<String, Location> locations = new HashMap<>();

            int i = 0;
            for (String team : teams) {
                locations.putAll(DistributeAroundPoint(locationsList.get(i), TeamManager.getMembers(team)));
                i++;
            }

            // above code includes all members from all teams mentioned in the input list
            // remove anyone not specifically in the input list
            List<String> ignList = Arrays.asList(igns);
            for (String ign : locations.keySet()) {
                if (!ignList.contains(ign)) locations.remove(ign);
            }

            return locations;

        }

        @Override
        public HashMap<String, Location> GetSpawnLocations(List<String> igns) {
            return GetSpawnLocations(igns.toArray(new String[0]));
        }

    }

    //=========================================================================

    private HashMap<String, Location> DistributeAroundPoint(Location location, String... igns) {
        return DistributeAroundPoint(location, Arrays.asList(igns));
    }

    private HashMap<String, Location> DistributeAroundPoint(Location location, Collection<String> igns) {

        int count = igns.size();
        HashMap<String, Location> locations = new HashMap<>(count);

        if (count == 0) return locations;
        if (count == 1) {
            locations.put(igns.toArray(new String[0])[0], location);
            return locations;
        }

        World world = location.getWorld();
        double centerX = location.getX();
        double centerZ = location.getZ();
        @SuppressWarnings("UnnecessaryLocalVariable")
        double radius  = count; // pi blocks between each person, TODO adjust spacing here
        double spacingRadians = 2*Math.PI / count;

        int completed = 0;
        for (String ign : igns) {

            double x = centerX + Math.cos(spacingRadians * completed)*radius;
            double z = centerZ + Math.sin(spacingRadians * completed)*radius;
            double y = world.getHighestBlockYAt((int) x, (int) z) + 1.1;

            locations.put(ign, new Location(world, x, y, z));
            completed++;

        }

        return locations;

    }

    //=========================================================================

}
