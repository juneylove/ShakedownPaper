package me.juneylove.shakedown.scoring;

import me.juneylove.shakedown.Main;
import me.juneylove.shakedown.data.BackupToFile;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;


public class TeamManager {

    private static HashMap<String, String> PLAYERS = new HashMap<>();
    private static List<String> TEAMS = new ArrayList<>();

    public static final String SPECTATOR = "Spectator";
    public static final String NONE = "None";

    //=========================================================================

    public static List<String> teams() {

        return TEAMS;

    }

    public static String getTeamByNumber(int i) {

        if (TEAMS.size() >= i) return TEAMS.get(i-1);
        return "";

    }

    public static int getTeamNumber(String team) {

        return TEAMS.indexOf(team) + 1;

    }

    public static int teamSize(String team) {
        return getMembers(team).size();
    }

    // Get a player's team; if they are not on a team, add them to team "NONE"
    public static String getTeam(String ign) {

        String team = PLAYERS.get(fixPlayerCase(ign));

        if (team == null) {
            PLAYERS.put(ign, NONE);
            return NONE;
        } else {
            return team;
        }

    }

    public static boolean sameTeam(String ign1, String ign2) {

        return (getTeam(ign1).equals(getTeam(ign2)));

    }

    // Get list of team members
    public static Set<String> getMembers(String team) {

        return ScoreManager.members(fixTeamCase(team));

    }

    public static List<String> gameTeams() {

        List<String> teams = new ArrayList<>(TEAMS);
        teams.remove(SPECTATOR);
        teams.remove(NONE);

        return teams;

    }

    public static List<String> allGamePlayers() {

        List<String> players = new ArrayList<>();
        for (String team : gameTeams()) {
            players.addAll(getMembers(team));
        }
        return players;

    }

    public static String fixTeamCase(String input) {

        for (String team : TEAMS) {
            if (input.equalsIgnoreCase(team)) {
                return team;
            }
        }
        return input;

    }

    public static String fixPlayerCase(String input) {

        for (String ign : PLAYERS.keySet()) {
            if (input.equalsIgnoreCase(ign)) {
                return ign;
            }
        }
        return input;

    }

    // Check if team is registered in TEAMS
    public static boolean teamExists(String team) {

        return (TEAMS.contains(fixTeamCase(team)));

    }

    // Check if player is registered in PLAYERS
    public static boolean playerHasTeam(String ign) {

        return (PLAYERS.containsKey(fixPlayerCase(ign)));

    }

    // Check if a player is on any game team (NOT "spectator" or "none" team)
    public static boolean isGamePlayer(String ign) {

        String team = PLAYERS.get(fixPlayerCase(ign));
        if (team != null) {
            return isGameTeam(team);
        }
        return false;

    }

    public static boolean isGameTeam(String team) {

        if (!teamExists(team)) return false;

        return !Objects.equals(fixTeamCase(team), SPECTATOR) && !Objects.equals(fixTeamCase(team), NONE);

    }

    // Check if a player is on a specific team
    public static boolean isPlayerOnThisTeam(String ign, String team) {

        String currentTeam = PLAYERS.get(fixPlayerCase(ign));
        return (Objects.equals(currentTeam, fixTeamCase(team)));

    }

    public static int numOfGameTeams(List<Player> players) {

        Set<String> teams = new HashSet<>();
        for (Player player : players) {
            String ign = player.getName();
            teams.add(getTeam(ign));
        }

        return teams.size();

    }

    //=========================================================================

    // Change a player's team assignment
    public static boolean changeTeam(String ign, String newTeam) {

        if (teamExists(newTeam) && playerHasTeam(ign)) {

            boolean Success = removePlayerFromTeam(ign, getTeam(ign));
            if (Success) {
                return addPlayerToTeam(ign, newTeam);
            }

        }
        return false;

    }

    // Remove player from team
    public static boolean removePlayerFromTeam(String ign, String team) {

        if (isPlayerOnThisTeam(ign, team)) {

            String ignFixedCase = fixPlayerCase(ign);
            String teamFixedCase = fixTeamCase(team);

            boolean Success = ScoreManager.removePlayerFromTeam(ignFixedCase, teamFixedCase);
            if (Success) {
                PLAYERS.remove(ignFixedCase);
                return true;
            }

        }
        return false;

    }

    // Add player to a team - returns false if player is already on a team or if team doesn't exist
    public static boolean addPlayerToTeam(String ign, String team) {

        if (teamExists(team)) {

            if (!playerHasTeam(ign)) {

                String teamFixedCase = fixTeamCase(team);

                // Add player to appropriate team
                ScoreManager.addPlayerToTeam(ign, teamFixedCase);
                PLAYERS.put(ign, teamFixedCase);

                // Update PLAYERS_PER_TEAM if necessary
                Set<String> members = getMembers(teamFixedCase);
                if (members != null) {
                    if (ScoreManager.PLAYERS_PER_TEAM < members.size()) {
                        ScoreManager.PLAYERS_PER_TEAM = members.size();
                        TeamManager.updatePlayerCount();
                    }
                }

                return true;

            }

        }
        return false;

    }

    // Register a new team
    public static boolean registerEmptyTeam(String team) {

        if (teamExists(team)) {
            return false;
        }

        TEAMS.add(team);
        ScoreManager.NUM_OF_TEAMS = TEAMS.size();
        TeamManager.updatePlayerCount();
        return ScoreManager.registerNewTeam(team); // Does not register to ScoreTracker if "Spectator" or "None"

    }

    // Remove an empty team
    public static boolean removeEmptyTeam(String team) {

        if (teamExists(team)) {

            if (getMembers(team) == null) {
                TEAMS.remove(fixTeamCase(team));
                ScoreManager.NUM_OF_TEAMS = TEAMS.size();
                TeamManager.updatePlayerCount();
                return true;
            }

        }
        return false;

    }

    //=========================================================================

    // Clear all scoring data = returns false if anything fails
    //  - Then clears placements because they can be recalculated from scores
    //  - Clears teams first since they can be reloaded from TeamList.yml
    //  - Then lastly clears scores only if the others succeeded
    public static boolean clearPlacementsThenTeamsThenScores() {

        if (PlacementManager.clearPlacementData()) {

            if (clearTeamData()) {
                return ScoreManager.clearScoreData();
            }

        }
        return false;

    }

    // Clear all team data to reset game - returns false and does nothing if game is running
    private static boolean clearTeamData() {

        if (!Main.GAME_IN_PROGRESS) {
            PLAYERS.clear();
            TEAMS.clear();
            return true;
        } else {
            return false;
        }

    }

    public static void updatePlayerCount() {

        int total = 0;
        for (String player : PLAYERS.keySet()) {

            if (isGamePlayer(player)) {
                total++;
            }

        }
        ScoreManager.NUM_OF_GAME_PLAYERS = total;

    }

    // Check all team data before starting game
    public static boolean verifyTeamsIntegrity(Plugin plugin) {

        // Check that all teams have players here in TeamTracker
        for (String team : TEAMS) {

            if (isGameTeam(team)) {

                if (!PLAYERS.containsValue(team)) {
                    plugin.getLogger().warning("VerifyTeams: Error - team " + team + " has no players in TeamTracker");
                    return false;
                }

            }

        }

        // Lastly, verify data is consistent between TeamTracker and ScoreTracker
        return compareTeamAssignments(plugin);

    }

    // Ensures data is consistent between TeamTracker and ScoreTracker
    private static boolean compareTeamAssignments(Plugin plugin) {

        // First, compare ScoreTracker -> TeamTracker
        for (String team : TEAMS) {

            // Check all teams except these (listed last in Teams enum so we can just break out of the loop)
            if (isGameTeam(team)) {

            // Return false if any team is empty in ScoreTracker
            Set<String> members = getMembers(team);
            if (members == null) {
                plugin.getLogger().warning("VerifyTeams: Error - empty team " + team + " in ScoreTracker");
                return false;
            }

            // Return false if any player registered in ScoreTracker is not registered to same team in TeamTracker
            for (String ign : members) {

                if (!Objects.equals(PLAYERS.get(ign), team)) {
                    plugin.getLogger().warning("VerifyTeams: Error - ScoreTracker does not match TeamTracker for ign " + ign);
                    return false;
                }

            }

            }

        }

        // Then, compare TeamTracker -> ScoreTracker
        for (String ign : PLAYERS.keySet()) {

            if (isGamePlayer(ign)) {

                // Return false if the team is empty in ScoreTracker, just to be sure lol
                Set<String> members = getMembers(PLAYERS.get(ign));
                if (members == null) {
                    plugin.getLogger().warning("VerifyTeams: Error - empty team " + PLAYERS.get(ign) + " in ScoreTracker");
                    return false;
                }

                // Return false if any player registered in TeamTracker is not registered to same team in ScoreTracker
                if (!members.contains(ign)) {
                    plugin.getLogger().warning("VerifyTeams: Error - TeamTracker does not match ScoreTracker for ign " + ign);
                    return false;
                }

            }

        }
        return true;

    }

    //=========================================================================

    // Empty teams will be lost!
    public static boolean backupTeams() {

        return BackupToFile.SaveObject(PLAYERS, "TeamTracker.dat");

    }

    public static boolean loadTeamsFromBackup() {

        Object obj = BackupToFile.LoadObject("TeamTracker.dat");
        if (obj == null) {
            return false;
        } else {
            //noinspection unchecked
            PLAYERS = (HashMap<String, String>) obj;
            TEAMS = new ArrayList<>(PLAYERS.values());
            return true;
        }

    }

    //=========================================================================

}
