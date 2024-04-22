package me.juneylove.shakedown.data;

import me.juneylove.shakedown.Main;
import me.juneylove.shakedown.scoring.PlacementManager;
import me.juneylove.shakedown.scoring.ScoreManager;
import me.juneylove.shakedown.scoring.TeamManager;
import me.juneylove.shakedown.ui.TextFormat;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;

public class LoadTeams {

    public static boolean LoadFromFile() {

        Plugin plugin = Main.getInstance();

        // Data was cleared successfully - proceed to load new data
        File teamFile = new File(plugin.getDataFolder(), "TeamList.yml");

        if (!teamFile.exists()) {
            plugin.getLogger().warning("LoadTeams: Failed to find TeamList.yml");
            return false;
        }

        // File loaded, load each team listed in Teams enum
        YamlConfiguration data = YamlConfiguration.loadConfiguration(teamFile);

        Set<String> teams = data.getKeys(false);
        teams.add(TeamManager.SPECTATOR);
        teams.add(TeamManager.NONE);
        for (String team : teams) {

            List<String> members = data.getStringList(team + ".members");
            if (members.size() == 0) {
                if (!team.equals(TeamManager.SPECTATOR) && !team.equals(TeamManager.NONE)) {
                    plugin.getLogger().warning("LoadTeams: Failed to find members of team " + team);
                    return false;
                }
            }

            if (ScoreManager.PLAYERS_PER_TEAM < members.size()) {
                ScoreManager.PLAYERS_PER_TEAM = members.size();
            }

            // Team members loaded successfully, register to appropriate data structures

            // Register all teams and members to score, team, and placement trackers
            boolean teamSuccess = TeamManager.registerEmptyTeam(team);
            if (!teamSuccess) {
                plugin.getLogger().warning("LoadTeams: Team " + team + " already exists");
                return false;
            }

            for (String ign : members) {

                boolean playerSuccess = TeamManager.addPlayerToTeam(ign, team);
                if (!playerSuccess) {
                    plugin.getLogger().warning("LoadTeams: Player " + ign + " already on a different team");
                    return false;
                }

            }

        }

        ScoreManager.NUM_OF_TEAMS = teams.size() - 2;
        TeamManager.updatePlayerCount();

        boolean verified = TeamManager.verifyTeamsIntegrity(plugin);
        if (!verified) {
            plugin.getLogger().warning("LoadTeams: No errors during loading, but team verification failed");
            return false;
        }

        PlacementManager.initializePlacements();
        PlacementManager.recalculateAll();

        TextFormat.LoadTeamData(data);

        return true;
    }

}
