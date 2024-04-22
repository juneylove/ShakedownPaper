package me.juneylove.shakedown.scoring;

import java.util.*;

import me.juneylove.shakedown.Main;
import me.juneylove.shakedown.data.BackupToFile;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.ui.Sidebar;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nullable;

public class ScoreManager {

    public static int NUM_OF_TEAMS = 2;
    public static int PLAYERS_PER_TEAM = 2;
    public static int NUM_OF_GAME_PLAYERS = NUM_OF_TEAMS * PLAYERS_PER_TEAM;

    private static HashMap<String, TeamScore> SCORES = new HashMap<>(NUM_OF_TEAMS);

    //=========================================================================

    // Get a team's total score (through the current game)
    public int getTotalTeamScore(String team) {

        if (TeamManager.isGameTeam(team)) {
            return SCORES.get(team).totals[0];
        }
        return 0;

    }

    // Get a player's total score (through the current game) if team is not readily available
    public int getTotalPlayerScore(String ign) {

        String team = TeamManager.getTeam(ign);
        return getTotalPlayerScore(team, ign);

    }

    // Get a player's total score (through the current game) if team is known
    private int getTotalPlayerScore(String team, String ign) {

        if (TeamManager.isGameTeam(team)) {
            return SCORES.get(team).players.get(ign)[0];
        }
        return 0;

    }

    //=========================================================================

    // Get a team's total score for the current game
    public static int getTeamScore(String team) {

        return getTeamScore(team, Games.CURRENT_GAME_NUM);

    }

    // Get a team's total score for a given game
    public static int getTeamScore(String team, int gameNum) {

        if (TeamManager.isGameTeam(team)) {
            return SCORES.get(team).totals[gameNum];
        }
        return 0;

    }

    // Get a player's score for the current game if team is not readily available
    public static int getPlayerScore(String ign) {

        return getPlayerScore(ign, Games.CURRENT_GAME_NUM);

    }

    // Get a player's score for a given game if team is not readily available
    public static int getPlayerScore(String ign, int gameNum) {

        String team = TeamManager.getTeam(ign);
        if (TeamManager.isGameTeam(team)) {
            return getPlayerScore(team, ign, gameNum);
        }
        return 0;

    }

    // Get a player's score for a given game if team is known
    private static int getPlayerScore(String team, String ign, int gameNum) {

        int[] playerScores = SCORES.get(team).players.get(ign);
        if (playerScores.length+1 < gameNum) {
            return 0;
        } else {
            return playerScores[gameNum];
        }

    }

    // Retrieve ALL score data. Only to be used for calculating placements
    protected static HashMap<String, TeamScore> getAllScores() {

        return SCORES;

    }

    //=========================================================================

    // add score for entire team, distributed evenly
    public static void addTeamScore(String team, int amount) {

        int remaining = amount;
        int each = amount / members(team).size();
        int count = 0;
        for (String ign : members(team)) {

            count++;
            if (count < members(team).size()) {
                addScore(ign, each);
            } else {
                addScore(ign, remaining);
            }
            remaining -= each;

        }

    }

    // Add to a player's score for the current game
    public static void addScore(String ign, int amount) {

        addScore(ign, amount, Games.CURRENT_GAME_NUM);

    }

    // Add to a player's score for a given game
    public static void addScore(String ign, int amount, int gameNum) {

        if (!TeamManager.isGamePlayer(ign)) {
            return;
        }

        // Add to specified game (default is Main.CURRENT_GAME_NUM)
        int newScore = getPlayerScore(ign, gameNum) + amount;
        setScore(ign, newScore, gameNum);
        PlacementManager.updatePlacements(gameNum);

        // Also add to games total in game number slot 0
        int totalScore = getPlayerScore(ign, 0) + amount;
        setScore(ign, totalScore, 0);
        PlacementManager.updatePlacements(0);

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;
        World world = player.getWorld();
        Sidebar.refreshQueued.put(world, true);

    }

    // Set a player's score for a given game
    private static void setScore(String ign, int score, int gameNum) {

        String team = TeamManager.getTeam(ign);

        if (TeamManager.isGameTeam(team)) {

            int[] playerScores = SCORES.get(team).players.get(ign);
            int existingScore = playerScores[gameNum];

            // Do nothing if gameNum is out of bounds
            if (playerScores.length >= gameNum+1) {

                playerScores[gameNum] = score;
                SCORES.get(team).players.put(ign, playerScores);
                // Also track team total by adding the difference
                SCORES.get(team).totals[gameNum] += score - existingScore;

            }

        }

    }

    //=========================================================================

    // Equalizes points for players across each team for the current game
    public void distributeScores() {

        distributeScores(Games.CURRENT_GAME_NUM);

    }

    // Equalizes points for players across each team for a given game
    public void distributeScores(int gameNum) {

        for (String team : TeamManager.teams()) {

            if (TeamManager.isGameTeam(team)) {
                distributeScoreAcrossOneTeam(team, gameNum);
            }

        }
        PlacementManager.updatePlacements(gameNum);
        PlacementManager.updatePlacements(0);
        Sidebar.queueAllRefresh();

    }

    // Equalizes scores for all players on a team for a given game
    private void distributeScoreAcrossOneTeam(String team, int gameNum) {

        int total = getPlayerScore(team, gameNum);
        int each = total / PLAYERS_PER_TEAM;
        for (String ign : Objects.requireNonNull(members(team))) {
            setScore(ign, gameNum, each);
        }

    }

    //=========================================================================

    // Add a player to existing team if team exists - DO NOT ADD NEW USES!
    protected static void addPlayerToTeam(String ign, String team) {

        if (!(Objects.equals(team, TeamManager.SPECTATOR) || Objects.equals(team, TeamManager.NONE))) {

            if (SCORES.get(team) == null) {
                registerNewTeam(team);
            }

            SCORES.get(team).players.put(ign, new int[Main.NUM_OF_GAMES + 1]);

        }

    }

    // Remove player from team if team exists - DO NOT ADD NEW USES!
    protected static boolean removePlayerFromTeam(String ign, String team) {

        if (SCORES.get(team) != null) {
            SCORES.get(team).players.remove(ign);
            return true;
        } else {
            return false;
        }

    }

    // Add a new team to the tracker (returns if team already exists) - DO NOT ADD NEW USES!
    protected static boolean registerNewTeam(String team) {

        if (TeamManager.isGameTeam(team)) {

            if (SCORES.get(team) == null) {

                SCORES.put(team, new TeamScore());
                SCORES.get(team).totals = new int[Main.NUM_OF_GAMES+1];

            } else {
                return false;
            }

        }
        return true;

    }

    //=========================================================================

    // Get collection of team members; return null if team is not registered
    protected static Set<String> members(String team) {

        if (SCORES.get(team) == null) {
            return new HashSet<>();
        }
        return SCORES.get(team).players.keySet();

    }

    // Clear all score data to reset game - returns false and does nothing if game is running
    protected static boolean clearScoreData() {

        if (!Main.GAME_IN_PROGRESS) {
            SCORES.clear();
            return true;
        } else {
            return false;
        }

    }

    // Verify number of teams and correct number of players on each team
    public static boolean verifyTeamSizes(Plugin plugin) {

        if (SCORES.size() != NUM_OF_TEAMS) {
            plugin.getLogger().warning("VerifyTeams: SCORES record size is " + SCORES.size() + " while NUM_OF_TEAMS is " + NUM_OF_TEAMS);
            return false;
        }

        for (String team : TeamManager.teams()) {

            if (TeamManager.isGameTeam(team)) {

                if (SCORES.get(team) == null) {
                    plugin.getLogger().warning("VerifyTeams: Team " + team + " has no players in ScoreTracker");
                    return false;
                }

                if (SCORES.get(team).players.size() != PLAYERS_PER_TEAM) {
                    plugin.getLogger().warning("VerifyTeams: Team " + team + " has uneven number of players in ScoreTracker");
                    return false;
                }

            }

        }
        return true;

    }

    //=========================================================================

    // Save score data to disk
    public static boolean backupScores() {

        return BackupToFile.SaveObject(SCORES, "ScoreTracker.dat");

    }

    // Load score data from disk
    public static boolean loadScoresFromBackup() {

        Object obj = BackupToFile.LoadObject("ScoreTracker.dat");
        if (obj == null) {
            return false;
        } else {
            //noinspection unchecked
            SCORES = (HashMap<String, TeamScore>) obj;
            return true;
        }

    }

    //=========================================================================

}
