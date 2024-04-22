package me.juneylove.shakedown.scoring;

import me.juneylove.shakedown.Main;
import me.juneylove.shakedown.games.Games;

import java.util.*;

import static me.juneylove.shakedown.scoring.ScoreManager.*;

public class PlacementManager {

    @SuppressWarnings({"unchecked", "FieldMayBeFinal"})
    private static HashMap<String, Integer>[] PLAYER_PLACEMENTS = new HashMap[Main.NUM_OF_GAMES+1];
    @SuppressWarnings({"unchecked", "FieldMayBeFinal"})
    private static HashMap<String, Integer>[] TEAM_PLACEMENTS = new HashMap[Main.NUM_OF_GAMES+1];

    //=========================================================================

    // Methods for either place or team for simplicity :D
    public static int getPlacement(String input) {

        return getPlacement(input, Games.CURRENT_GAME_NUM);

    }

    public static int getPlacement(String input, int gameNum) {

        if (TeamManager.isGameTeam(input)) {
            return getTeamPlacement(input, gameNum);
        } else if (TeamManager.isGamePlayer(input)) {
            return getPlayerPlacement(input, gameNum);
        }
        return 0;

    }

    public static int getPlacementWithinTeam(String ign) {

        return getPlacementWithinTeam(ign, Games.CURRENT_GAME_NUM);

    }

    public static int getPlacementWithinTeam(String ign, int gameNum) {

        HashMap<String, Integer> playerScores = new HashMap<>();

        // We gonna calculate this manually every time because it's not too intensive
        // also i don't wanna deal with an array of hashmaps of hashmaps
        String team = TeamManager.getTeam(ign);
        Set<String> members = TeamManager.getMembers(team);
        if (members != null) {

            for (String member : members) {
                playerScores.put(member, getPlayerScore(member, gameNum));
            }

        }

        int placement = 1;
        for (String player : playerScores.keySet()) {

            if (playerScores.get(player) > playerScores.get(ign)) {
                placement++;
            }

        }
        return placement;

    }

    // Get team's placement for current game
    public static int getTeamPlacement(String team) {

        return getTeamPlacement(team, Games.CURRENT_GAME_NUM);

    }

    // Get team's placement for specified game
    public static int getTeamPlacement(String team, int gameNum) {

        if (!TeamManager.isGameTeam(team)) {
            return 0;
        }

        Integer place = TEAM_PLACEMENTS[gameNum].get(team);
        if (place != null) {
            return place;
        }
        return 0;

    }

    // Get player's placement for current game
    public static int getPlayerPlacement(String ign) {

        return getPlayerPlacement(ign, Games.CURRENT_GAME_NUM);

    }

    // Get player's placement for specified game
    public static int getPlayerPlacement(String ign, int gameNum) {

        if (!TeamManager.isGamePlayer(ign)) {
            return 0;
        }

        Integer place = PLAYER_PLACEMENTS[gameNum].get(ign);
        if (place != null) {
            return place;
        }
        return 0;

    }

    //=========================================================================

    // Get which team is in a given place for the current game
    public static String getTeamPlaceByNumber(int place) {

        return getTeamPlaceByNumber(place, Games.CURRENT_GAME_NUM);

    }

    // Get which team is in a given place for a specified game
    public static String getTeamPlaceByNumber(int requestedPlace, int gameNum) {

        int i;
        for (i = requestedPlace; i > 0; i--) {

            if (TEAM_PLACEMENTS[gameNum].containsValue(i)) {
                break;
            }

        }

        // i don't know how to explain this but this is the right place i think
        return getTiedTeams(i, gameNum).get(requestedPlace-i);

    }

    // Get which player is in a given place for the current game
    public static String getPlayerPlaceByNumber(int place) {

        return getPlayerPlaceByNumber(place, Games.CURRENT_GAME_NUM);

    }

    // Get which player is in a given place for a specified game
    public static String getPlayerPlaceByNumber(int requestedPlace, int gameNum) {

        int i;
        for (i = requestedPlace; i > 0; i--) {

            if (PLAYER_PLACEMENTS[gameNum].containsValue(i)) {
                break;
            }

        }

        // i don't know how to explain this but this is the right place i think
        return getTiedPlayers(i, gameNum).get(requestedPlace-i);

    }

    public static String getPlacementWithinTeamByNumber(String team, int requestedPlace) {

        return getPlacementWithinTeamByNumber(team, requestedPlace, Games.CURRENT_GAME_NUM);

    }

    public static String getPlacementWithinTeamByNumber(String team, int requestedPlace, int gameNum) {

        HashMap<String, Integer> playerScores = new HashMap<>();

        Set<String> members = TeamManager.getMembers(team);
        if (members != null) {
            for (String member : members) {
                playerScores.put(member, getPlacementWithinTeam(member, gameNum));
            }
        }

        int i;
        for (i = requestedPlace; i > 0; i--) {
            if (playerScores.containsValue(i)) {
                break;
            }
        }

        // i don't know how to explain this but this is the right place i think
        return getTiedPlayersWithinTeam(team, i, gameNum).get(requestedPlace-i);

    }

    //=========================================================================

    public static List<String> getTies(String input) {

        return getTies(input, Games.CURRENT_GAME_NUM);

    }

    public static List<String> getTies(String input, int gameNum) {

        // i is the next highest recorded place - will be the listed number for the placement
        // Collect all teams/players listed for place i
        if (TeamManager.isGameTeam(input)) {

            int place = getTeamPlacement(input);
            return getTiedTeams(place, gameNum);

        } else if (TeamManager.isGamePlayer(input)) {

            int place = getPlayerPlacement(input);
            return getTiedPlayers(place, gameNum);

        }

        return null;

    }

    public static List<String> getTiedTeams(int place) {

        return getTiedTeams(place, Games.CURRENT_GAME_NUM);

    }

    public static List<String> getTiedTeams(int place, int gameNum) {

        List<String> results = new ArrayList<>();

        int i;
        for (i = place; i > 0; i--) {
            if (TEAM_PLACEMENTS[gameNum].containsValue(i)) {
                break;
            }
        }

        for (String team : TEAM_PLACEMENTS[gameNum].keySet()) {
            if (TEAM_PLACEMENTS[gameNum].get(team) == i) {
                results.add(team);
            }
        }

        Collections.sort(results);
        return results;

    }

    public static List<String> getTiedPlayers(int place) {

        return getTiedPlayers(place, Games.CURRENT_GAME_NUM);

    }

    public static List<String> getTiedPlayers(int place, int gameNum) {

        List<String> results = new ArrayList<>();

        int i;
        for (i = place; i > 0; i--) {
            if (PLAYER_PLACEMENTS[gameNum].containsValue(i)) {
                break;
            }
        }

        for (String player : PLAYER_PLACEMENTS[gameNum].keySet()) {
            if (PLAYER_PLACEMENTS[gameNum].get(player) == i) {
                results.add(player);
            }
        }

        Collections.sort(results);
        return results;

    }

    public static List<String> getTiedPlayersWithinTeam(String team, int place) {

        return getTiedPlayersWithinTeam(team, place, Games.CURRENT_GAME_NUM);

    }

    public static List<String> getTiedPlayersWithinTeam(String team, int place, int gameNum) {

        List<String> results = new ArrayList<>();

        HashMap<String, Integer> playerScores = new HashMap<>();

        Set<String> members = TeamManager.getMembers(team);
        if (members != null) {
            for (String member : members) {
                playerScores.put(member, getPlacementWithinTeam(member, gameNum));
            }
        }

        int i;
        for (i = place; i > 0; i--) {
            if (playerScores.containsValue(i)) {
                break;
            }
        }

        for (String player : playerScores.keySet()) {
            if (playerScores.get(player) == i) {
                results.add(player);
            }
        }

        Collections.sort(results);
        return results;

    }

    //=========================================================================

    public static boolean clearPlacementData() {

        if (!Main.GAME_IN_PROGRESS) {

            for (int i = 0; i<= Main.NUM_OF_GAMES; i++) {
                TEAM_PLACEMENTS[i].clear();
                PLAYER_PLACEMENTS[i].clear();
            }
            return true;

        } else {
            return false;
        }

    }

    // Only used in initialization/after clearing all data
    public static void initializePlacements() {

        for (int i = 0; i <= Main.NUM_OF_GAMES; i++) {
            PLAYER_PLACEMENTS[i] = new HashMap<>(NUM_OF_GAME_PLAYERS);
            TEAM_PLACEMENTS[i] = new HashMap<>(NUM_OF_TEAMS);
        }

    }

    // Update placements (individual and team) for specified game
    protected static void updatePlacements(int gameNum) {

        HashMap<String, TeamScore> scores = ScoreManager.getAllScores();
        HashMap<String, Integer> teamScores = new HashMap<>();
        HashMap<String, Integer> playerScores = new HashMap<>();

        // Extract scores for current game
        for (String team : scores.keySet()) {

            Set<String> members = TeamManager.getMembers(team);
            if (members != null) {

                teamScores.put(team, scores.get(team).totals[gameNum]);

                for (String ign : members) {

                    playerScores.put(ign, scores.get(team).players.get(ign)[gameNum]);

                }

            }

        }

        // Fill out team placements for given game
        for (String team : scores.keySet()) {

            Integer placement = 1;
            for (String teamName : scores.keySet()) {

                // Increment placement for each team with a higher score
                if (teamScores.get(teamName) > teamScores.get(team)) {
                    placement++;
                }

            }
            TEAM_PLACEMENTS[gameNum].put(team, placement);

        }

        // Fill out player placements for given game
        for (String ign : playerScores.keySet()) {

            Integer placement = 1;
            for (String player : playerScores.keySet()) {

                // Increment placement for each player with a higher score
                if (playerScores.get(player) > playerScores.get(ign)) {
                    placement++;
                }

            }
            PLAYER_PLACEMENTS[gameNum].put(ign,placement);

        }

    }

    // Recalculate all in the event of a file restore
    public static void recalculateAll() {

        for (int i = 0; i<= Main.NUM_OF_GAMES; i++) {
            updatePlacements(i);
        }

    }

    //=========================================================================

}
