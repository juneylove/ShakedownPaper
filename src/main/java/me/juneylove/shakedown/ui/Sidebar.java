package me.juneylove.shakedown.ui;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.mechanics.worlds.WorldSetting;
import me.juneylove.shakedown.scoring.PlacementManager;
import me.juneylove.shakedown.scoring.ScoreManager;
import me.juneylove.shakedown.scoring.TeamManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Sidebar {

    public static HashMap<World, Boolean> refreshQueued = new HashMap<>();

    @SuppressWarnings("FieldMayBeFinal")
    private static Multimap<World, Map.Entry<String, Format>> registry = ArrayListMultimap.create();

    static final String objectiveName = "title"; // Only objective name used in the current implementation, all teams & entries are registered to this
    static final int totalWidthChars = 24;
    static final int totalWidthPixels = totalWidthChars * 6;
    static final int rowLimit = 7; // Rows between header and footer (set separately for hybrid)
                                   // Do not set rowLimit lower than 7 !! it will break things - search rowLimit for deets

    static final String[] entries =
                   {TextFormat.NEGATIVE_ONE_PX + TextFormat.NEGATIVE_ONE_PX + TextFormat.NEGATIVE_ONE_PX + TextFormat.NEGATIVE_ONE_PX,
                    TextFormat.NEGATIVE_ONE_PX + TextFormat.NEGATIVE_ONE_PX + TextFormat.NEGATIVE_ONE_PX + TextFormat.NEGATIVE_SIX_PX,
                    TextFormat.NEGATIVE_ONE_PX + TextFormat.NEGATIVE_ONE_PX + TextFormat.NEGATIVE_SIX_PX + TextFormat.NEGATIVE_ONE_PX,
                    TextFormat.NEGATIVE_ONE_PX + TextFormat.NEGATIVE_ONE_PX + TextFormat.NEGATIVE_SIX_PX + TextFormat.NEGATIVE_SIX_PX,
                    TextFormat.NEGATIVE_ONE_PX + TextFormat.NEGATIVE_SIX_PX + TextFormat.NEGATIVE_ONE_PX + TextFormat.NEGATIVE_ONE_PX,
                    TextFormat.NEGATIVE_ONE_PX + TextFormat.NEGATIVE_SIX_PX + TextFormat.NEGATIVE_ONE_PX + TextFormat.NEGATIVE_SIX_PX,
                    TextFormat.NEGATIVE_ONE_PX + TextFormat.NEGATIVE_SIX_PX + TextFormat.NEGATIVE_SIX_PX + TextFormat.NEGATIVE_ONE_PX,
                    TextFormat.NEGATIVE_ONE_PX + TextFormat.NEGATIVE_SIX_PX + TextFormat.NEGATIVE_SIX_PX + TextFormat.NEGATIVE_SIX_PX,
                    TextFormat.NEGATIVE_SIX_PX + TextFormat.NEGATIVE_ONE_PX + TextFormat.NEGATIVE_ONE_PX + TextFormat.NEGATIVE_ONE_PX,
                    TextFormat.NEGATIVE_SIX_PX + TextFormat.NEGATIVE_ONE_PX + TextFormat.NEGATIVE_ONE_PX + TextFormat.NEGATIVE_SIX_PX,
                    TextFormat.NEGATIVE_SIX_PX + TextFormat.NEGATIVE_ONE_PX + TextFormat.NEGATIVE_SIX_PX + TextFormat.NEGATIVE_ONE_PX,
                    TextFormat.NEGATIVE_SIX_PX + TextFormat.NEGATIVE_ONE_PX + TextFormat.NEGATIVE_SIX_PX + TextFormat.NEGATIVE_SIX_PX,
                    TextFormat.NEGATIVE_SIX_PX + TextFormat.NEGATIVE_SIX_PX + TextFormat.NEGATIVE_ONE_PX + TextFormat.NEGATIVE_ONE_PX,
                    TextFormat.NEGATIVE_SIX_PX + TextFormat.NEGATIVE_SIX_PX + TextFormat.NEGATIVE_ONE_PX + TextFormat.NEGATIVE_SIX_PX,
                    TextFormat.NEGATIVE_SIX_PX + TextFormat.NEGATIVE_SIX_PX + TextFormat.NEGATIVE_SIX_PX + TextFormat.NEGATIVE_ONE_PX,
                    TextFormat.NEGATIVE_SIX_PX + TextFormat.NEGATIVE_SIX_PX + TextFormat.NEGATIVE_SIX_PX + TextFormat.NEGATIVE_SIX_PX};

    static final int globalRowLimit = entries.length;

    static final TextComponent titleDisplayName = Component.text("» ").color(TextFormat.ACCENT_COLOR)
            .append(Component.text(TextFormat.smallText("mc shakedown"))).color(TextFormat.TITLE_COLOR)
            .append(Component.text(" « ").color(TextFormat.ACCENT_COLOR));
    static final TextColor dividerColor = NamedTextColor.DARK_GRAY;
    static final TextComponent singleDividerLine = Component.text(TextFormat.singleLine(totalWidthPixels)).color(dividerColor);
    static final TextComponent doubleDividerLine = Component.text(TextFormat.doubleLine(totalWidthPixels)).color(dividerColor);

    //=========================================================================

    public static void setFormats(World world) {

        for (Player player : world.getPlayers()) {

            Scoreboard scoreboard = player.getScoreboard();
            String ign = player.getName();

            if (scoreboard.getObjective(objectiveName) == null) {
                scoreboard.registerNewObjective(objectiveName, Criteria.DUMMY, titleDisplayName);
            }

            // Use game setting for game players, "Teams" setting for spectators/none
            if (TeamManager.isGamePlayer(player.getName())) {

                Map.Entry<String, Format> entry = Map.entry(ign, Games.CURRENT_GAME.sidebarFormat);
                if (entry.getValue().rowLimit > globalRowLimit) entry.getValue().rowLimit = globalRowLimit;
                registry.put(world, entry);

            } else {

                Map.Entry<String, Format> entry = Map.entry(ign, new Teams(rowLimit));
                registry.put(world, entry);

            }

        }

        updateWorld(world);

    }

    public static void updateAll() {

        for (WorldSetting worldSetting : Games.CURRENT_GAME.currentRound.worldSettings) {

            World world = worldSetting.getWorld();
            if (world != null && registry.containsKey(world)) {

                if (refreshQueued.get(world) || Games.CURRENT_GAME.constantScoreboardRefresh) {
                    updateWorld(world);
                }

            }

        }

    }

    public static void queueAllRefresh() {
        refreshQueued.replaceAll((w, v) -> true);
    }

    public static void updateWorld(World world) {

        for (Map.Entry<String, Format> entry : registry.get(world)) {

            String ign = entry.getKey();
            if (Bukkit.getPlayer(ign) == null) continue;
            Scoreboard scoreboard = Objects.requireNonNull(Bukkit.getPlayer(ign)).getScoreboard();

            standardTitle(scoreboard);
            entry.getValue().update(scoreboard, ign);
            setFooterRow(scoreboard);

        }
        refreshQueued.put(world, false);

    }

    public static void resetAll(World world) {

        for (Player player : world.getPlayers()) {

            if (player.getScoreboard().getObjective(objectiveName) != null) {
                //noinspection ConstantConditions
                player.getScoreboard().getObjective(objectiveName).unregister();
            }

        }

        registry.removeAll(world);

    }

    //=========================================================================

    public static abstract class Format {

        int rowLimit;

        abstract void update(Scoreboard scoreboard, String ign);

    }

    public static class None extends Format {

        @Override
        public void update(Scoreboard scoreboard, String ign) {}

    }

    public static class Teams extends Format {

        public Teams(int rowLimit) {
            this.rowLimit = rowLimit;
        }

        @Override
        public void update(Scoreboard scoreboard, String ign) {

            standingsRow(scoreboard);
            setFirstNTeams(scoreboard, ign, 3);
            if (ScoreManager.NUM_OF_TEAMS > 3) {
                setRemainingTeams(scoreboard, ign, rowLimit);
            }

        }

    }

    public static class Individuals extends Format {

        public Individuals(int rowLimit) {
            this.rowLimit = rowLimit;
        }

        @Override
        public void update(Scoreboard scoreboard, String ign) {

            standingsRow(scoreboard);
            setIndividual(scoreboard, ign, rowLimit);

        }

    }

    public static class Hybrid extends Format {

        public Hybrid(int rowLimit) {
            this.rowLimit = rowLimit;
        }

        @Override
        public void update(Scoreboard scoreboard, String ign) {

            standingsRow(scoreboard);
            setFirstNTeams(scoreboard, ign, 3);
            setHybridTeam(scoreboard, ign, rowLimit-5);

        }

    }

    //=========================================================================

    private static void standardTitle(Scoreboard scoreboard) {

        Objective obj = scoreboard.getObjective(objectiveName);
        //noinspection ConstantConditions
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        Team subtitleRow = scoreboard.getTeam("subtitleRow");
        if (subtitleRow == null) {
            subtitleRow = scoreboard.registerNewTeam("subtitleRow");
        }
        subtitleRow.addEntry(TextFormat.NEGATIVE_ONE_PX);
        subtitleRow.prefix(Component.text(TextFormat.smallText( "game: " + Games.CURRENT_GAME.name)));
        obj.getScore(TextFormat.NEGATIVE_ONE_PX).setScore(18);

        Team dividerRow = scoreboard.getTeam("dividerRow");
        if (dividerRow == null) {
            dividerRow = scoreboard.registerNewTeam("dividerRow");
        }
        dividerRow.addEntry(TextFormat.NEGATIVE_SIX_PX);
        dividerRow.prefix(doubleDividerLine);
        obj.getScore(TextFormat.NEGATIVE_SIX_PX).setScore(17);

    }

    private static void standingsRow(Scoreboard scoreboard) {

        Objective obj = scoreboard.getObjective(objectiveName);
        //noinspection ConstantConditions
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        Team standingsRow = scoreboard.getTeam("standingsRow");
        if (standingsRow == null) {
            standingsRow = scoreboard.registerNewTeam("standingsRow");
        }
        standingsRow.addEntry(TextFormat.NEGATIVE_ONE_PX + TextFormat.NEGATIVE_SIX_PX);
        standingsRow.prefix(Component.text(TextFormat.smallText("standings:")).color(NamedTextColor.DARK_GRAY));
        obj.getScore(TextFormat.NEGATIVE_ONE_PX + TextFormat.NEGATIVE_SIX_PX).setScore(16);

    }

    private static void setFooterRow(Scoreboard scoreboard) {

        Objective obj = scoreboard.getObjective(objectiveName);

        Team footerRow = scoreboard.getTeam("footerRow");
        if (footerRow == null) {
            footerRow = scoreboard.registerNewTeam("footerRow");
        }
        footerRow.addEntry(TextFormat.NEGATIVE_SIX_PX + TextFormat.NEGATIVE_ONE_PX);
        footerRow.prefix(Component.text(TextFormat.smallText("                         " + TextFormat.RAISED_AT + "handle")).color(TextFormat.ACCENT_COLOR));
        //noinspection ConstantConditions
        obj.getScore(TextFormat.NEGATIVE_SIX_PX + TextFormat.NEGATIVE_ONE_PX).setScore(0);

    }

    //=========================================================================

    private static void setIndividual(Scoreboard scoreboard, String ign, int rows) {

        Objective obj = scoreboard.getObjective(objectiveName);
        int PlayersPlace = 1;
        for (int i = 1; i<= ScoreManager.NUM_OF_GAME_PLAYERS; i++) {
            if (PlacementManager.getPlayerPlaceByNumber(i).equals(ign)) {
                PlayersPlace = i;
                break;
            }
        }

        // First, determine if a break is needed
        boolean breakNeeded = PlayersPlace > rows;

        int playersToList; // ABOVE break if there is one
        if (breakNeeded) {
            playersToList = rows - 4;
        } else {
            playersToList = rows;
        }

        if (playersToList > ScoreManager.NUM_OF_GAME_PLAYERS) {
            playersToList = ScoreManager.NUM_OF_GAME_PLAYERS;
        }

        // List out players before the break, if there is one, otherwise all of them
        for (int i=1; i<=playersToList; i++) {

            String teamName = "row" + i+3; // SCOREBOARD team, not TeamTracker team
            Team row = scoreboard.getTeam(teamName);
            if (row == null) {
                row = scoreboard.registerNewTeam(teamName);
            }
            row.addEntry(entries[i-1]);
            row.prefix(playerRowByPlace(i, false, ign));
            //noinspection ConstantConditions
            obj.getScore(entries[i-1]).setScore(13-i);

        }
        // Now complete if no break needed :D

        if (breakNeeded) {

            int rowNumber = playersToList + 1; // starts counting after title rows

            int FirstPlaceAfterBreak = PlayersPlace-1;

            if (PlayersPlace == ScoreManager.NUM_OF_GAME_PLAYERS) {
                FirstPlaceAfterBreak--;
            }

            // Draw break row first
            Team breakRow = scoreboard.getTeam("breakRow");
            if (breakRow == null) {
                breakRow = scoreboard.registerNewTeam("breakRow");
            }
            breakRow.addEntry(entries[rowNumber]);
            breakRow.prefix(Component.text(" \u22EE" + " ".repeat(33) + "\u22EE "));
            //noinspection ConstantConditions
            obj.getScore(entries[rowNumber]).setScore(13-rowNumber);

            rowNumber++;

            // Now set 3 team rows: n-1, n, n+1
            Team rowNMinus1 = scoreboard.getTeam("rowNMinus1");
            if (rowNMinus1 == null) {
                rowNMinus1 = scoreboard.registerNewTeam("rowNMinus1");
            }
            rowNMinus1.addEntry(entries[rowNumber]);
            rowNMinus1.prefix(playerRowByPlace(FirstPlaceAfterBreak, false, ign));
            obj.getScore(entries[rowNumber]).setScore(13-rowNumber);

            rowNumber++;

            Team rowN = scoreboard.getTeam("rowN");
            if (rowN == null) {
                rowN = scoreboard.registerNewTeam("rowN");
            }
            rowN.addEntry(entries[rowNumber]);
            rowN.prefix(playerRowByPlace(FirstPlaceAfterBreak+1, false, ign));
            obj.getScore(entries[rowNumber]).setScore(13-rowNumber);

            rowNumber++;

            Team rowNPlus1 = scoreboard.getTeam("rowNPlus1");
            if (rowNPlus1 == null) {
                rowNPlus1 = scoreboard.registerNewTeam("rowNPlus1");
            }
            rowNPlus1.addEntry(entries[rowNumber]);
            rowNPlus1.prefix(playerRowByPlace(FirstPlaceAfterBreak+2, false, ign));
            obj.getScore(entries[rowNumber]).setScore(13-rowNumber);

        }



    }

    private static void setHybridTeam(Scoreboard scoreboard, String ign, int maxPlayers) {

        Objective obj = scoreboard.getObjective(objectiveName);

        int rowNumber = 4; // Starts after first 3 teams/rows are already listed

        // Create divider row first
        Team hybridDivider = scoreboard.getTeam("hybridDivider");
        if (hybridDivider == null) {
            hybridDivider = scoreboard.registerNewTeam("hybridDivider");
        }
        hybridDivider.addEntry(entries[rowNumber]);
        hybridDivider.prefix(singleDividerLine);
        //noinspection ConstantConditions
        obj.getScore(entries[rowNumber]).setScore(13-rowNumber);

        rowNumber++;

        Team hybridTeamRow = scoreboard.getTeam("hybridTeamRow");
        if (hybridTeamRow == null) {
            hybridTeamRow = scoreboard.registerNewTeam("hybridTeamRow");
        }
        hybridTeamRow.addEntry(entries[rowNumber]);
        hybridTeamRow.prefix(teamRowByName(TeamManager.getTeam(ign), false, ""));
        obj.getScore(entries[rowNumber]).setScore(13-rowNumber);

        // Now for the mess of handling displaying an arbitrary number of team members...
        String team = TeamManager.getTeam(ign);
        Set<String> members = TeamManager.getMembers(team);
        if (members != null) {

            // Bound the number of team members to display by the maximum
            int membersToDisplay = members.size();
            if (membersToDisplay > maxPlayers) {
                membersToDisplay = maxPlayers;
            }

            // Identify where the player falls in the team lineup
            int PlayersPlaceInTeam = 1;
            for (int i=1; i<=members.size(); i++) {
                if (PlacementManager.getPlacementWithinTeamByNumber(team, i).equals(ign)) {
                    PlayersPlaceInTeam = i;
                    break;
                }
            }

            // Set firstPlaceToDisplay so that the player is always included
            int firstPlaceToDisplay;
            if (maxPlayers >= members.size()) {
                firstPlaceToDisplay = 1;
            } else {
                firstPlaceToDisplay = PlayersPlaceInTeam + 1 - (maxPlayers - 1); // This will make it so the player is displayed *second to last* unless in last place, that's the goal
            }

            // Bound firstPlaceToDisplay on minimum and maximum ends
            if (firstPlaceToDisplay < 1) {
                firstPlaceToDisplay = 1;
            }
            if (firstPlaceToDisplay + (membersToDisplay-1) > members.size()) {
                firstPlaceToDisplay = members.size() - (membersToDisplay-1);
            }

            // Ok NOW we're finally ready to actually display the team members
            for (int i = firstPlaceToDisplay; i<=firstPlaceToDisplay + (membersToDisplay-1); i++) {

                rowNumber++;

                String rowName = "hybridTeamMember" + i;
                Team row = scoreboard.getTeam(rowName);
                if (row == null) {
                    row = scoreboard.registerNewTeam(rowName);
                }
                row.addEntry(entries[rowNumber]);
                row.prefix(formatHybridPlayerRow(i, team, false, ign));
                obj.getScore(entries[rowNumber]).setScore(13-rowNumber);

            }

        }

    }

    private static void setFirstNTeams(Scoreboard scoreboard, String ign, int n) {

        Objective obj = scoreboard.getObjective(objectiveName);
        String team = TeamManager.getTeam(ign);

        for (int i=1; i<=n; i++) {

            String rowName = "row" + i;
            Team row = scoreboard.getTeam(rowName);
            if (row == null) {
                row = scoreboard.registerNewTeam(rowName);
            }
            row.addEntry(entries[i-1]);
            row.prefix(teamRowByPlace(i, team));
            //noinspection ConstantConditions
            obj.getScore(entries[i-1]).setScore(13-i);

        }

    }

    private static void setRemainingTeams(Scoreboard scoreboard, String ign, int rows) {

        Objective obj = scoreboard.getObjective(objectiveName);

        String team = TeamManager.getTeam(ign);
        int PlayersTeamPlace = 1;
        for (int i = 1; i<= ScoreManager.NUM_OF_TEAMS; i++) {
            if (PlacementManager.getTeamPlaceByNumber(i).equals(team)) {
                PlayersTeamPlace = i;
                break;
            }
        }

        // First, determine if a break is needed
        boolean breakNeeded = PlayersTeamPlace > rows;

        int additionalTopTeams;
        if (breakNeeded) {
            // Format: below the break will be 3 teams, + 1 row for the break - determine how many more should be in the top list
            additionalTopTeams = (rows - 3) - 4; // 3 for teams already in the top list, 4 for break and below
            // this is why rowLimit cannot be less than 7 !!!!
        } else {
            additionalTopTeams = rows - 3; // 3 for teams already in top list, that's it
        }

        // Limit number of additional top teams by total number
        if (additionalTopTeams > ScoreManager.NUM_OF_TEAMS - 3) {
            additionalTopTeams = ScoreManager.NUM_OF_TEAMS - 3;
        }

        // List out additional top teams, if any
        for (int i=1; i<=additionalTopTeams; i++) {

            String teamName = "row" + i+3; // SCOREBOARD team, not TeamTracker team
            Team row = scoreboard.getTeam(teamName);
            if (row == null) {
                row = scoreboard.registerNewTeam(teamName);
            }
            row.addEntry(entries[2+i]);
            row.prefix(teamRowByPlace(3+i, team));
            //noinspection ConstantConditions
            obj.getScore(entries[2+i]).setScore(10-i);

        }
        // Scoreboard is now complete if no break needed :D

        if (breakNeeded) {

            int rowNumber = 3 + additionalTopTeams + 1; // starts counting after title rows

            int FirstPlaceAfterBreak = PlayersTeamPlace - 1;

            if (PlayersTeamPlace == ScoreManager.NUM_OF_TEAMS) {
                FirstPlaceAfterBreak--;
            }

            // Draw break row first
            Team breakRow = scoreboard.getTeam("breakRow");
            if (breakRow == null) {
                breakRow = scoreboard.registerNewTeam("breakRow");
            }
            breakRow.addEntry(entries[rowNumber]);
            breakRow.prefix(Component.text(" \u22EE" + " ".repeat(33) + "\u22EE "));
            //noinspection ConstantConditions
            obj.getScore(entries[rowNumber]).setScore(13-rowNumber);

            rowNumber++;

            // Now set 3 team rows: n-1, n, n+1
            Team rowNMinus1 = scoreboard.getTeam("rowNMinus1");
            if (rowNMinus1 == null) {
                rowNMinus1 = scoreboard.registerNewTeam("rowNMinus1");
            }
            rowNMinus1.addEntry(entries[rowNumber]);
            rowNMinus1.prefix(teamRowByPlace(FirstPlaceAfterBreak, team));
            obj.getScore(entries[rowNumber]).setScore(13-rowNumber);

            rowNumber++;

            Team rowN = scoreboard.getTeam("rowN");
            if (rowN == null) {
                rowN = scoreboard.registerNewTeam("rowN");
            }
            rowN.addEntry(entries[rowNumber]);
            rowN.prefix(teamRowByPlace(FirstPlaceAfterBreak+1, team));
            obj.getScore(entries[rowNumber]).setScore(13-rowNumber);

            rowNumber++;

            Team rowNPlus1 = scoreboard.getTeam("rowNPlus1");
            if (rowNPlus1 == null) {
                rowNPlus1 = scoreboard.registerNewTeam("rowNPlus1");
            }
            rowNPlus1.addEntry(entries[rowNumber]);
            rowNPlus1.prefix(teamRowByPlace(FirstPlaceAfterBreak+2, team));
            obj.getScore(entries[rowNumber]).setScore(13-rowNumber);

        }

    }

    //=========================================================================

    private static Component formatHybridPlayerRow(int place, String team, @SuppressWarnings("SameParameterValue") boolean omitPlaceNumber, String highlightIgn) {

        String playerName = PlacementManager.getPlacementWithinTeamByNumber(team, place);
        return playerRowByName(playerName, omitPlaceNumber, highlightIgn, "  "); // hardcode pad for now

    }

    private static Component playerRowByPlace(int place, @SuppressWarnings("SameParameterValue") boolean omitPlaceNumber, String highlightIgn) {

        String playerName = PlacementManager.getPlayerPlaceByNumber(place);
        return playerRowByName(playerName, omitPlaceNumber, highlightIgn, "");

    }

    private static Component playerRowByName(String ign, boolean omitPlaceNumber, String highlightIgn, String pad) {

        //  This value accounts for ties; the other "place" is more of just a row number
        int placeNumber = PlacementManager.getPlacementWithinTeam(ign);

        int nameWidth;
        if (omitPlaceNumber) {
            nameWidth = TextFormat.getTextWidth(pad + "» " + ign);
        } else {
            nameWidth = TextFormat.getTextWidth(pad + placeNumber + ". " + ign);
        }

        int padWidth = TextFormat.getTextWidth(pad);

        String score = Integer.toString(ScoreManager.getPlayerScore(ign));
        int scoreWidth = TextFormat.getTextWidth(score);

        TextComponent.Builder str = Component.text();

        TextComponent playerName;
        if (omitPlaceNumber) {
            playerName = Component.text(pad + "» " + ign + " ");
        } else {
            playerName = Component.text(pad + placeNumber + ". " + ign + " ");
        }

        if (ign.equals(highlightIgn)) {
            str.append(playerName.color(TextFormat.HIGHLIGHT_COLOR));
        } else {
            str.append(playerName);
        }

        int numOfPeriods = (totalWidthPixels - padWidth - (nameWidth+4) - (scoreWidth+4)) / 2;

        if ((TextFormat.getTextWidth(str.content()) + scoreWidth + padWidth) % 2 == 1) { // Odd number of pixels
            str.append(Component.text(TextFormat.PLUS_ONE_PX));
        }

        TextComponent dots = Component.text(".".repeat(numOfPeriods) + " ");
        if (ign.equals(highlightIgn)) {
            str.append(dots).color(TextFormat.HIGHLIGHT_COLOR);
        } else {
            str.append(dots).color(NamedTextColor.GRAY);
        }

        str.append(Component.text(score).color(TextFormat.SCORE_COLOR));

        return str.build();

    }

    private static Component teamRowByPlace(int place, String highlightTeam) {

        String teamName = PlacementManager.getTeamPlaceByNumber(place);
        return teamRowByName(teamName, false, highlightTeam);

    }

    private static Component teamRowByName(String teamName, @SuppressWarnings("SameParameterValue") boolean omitPlaceNumber, String highlightTeam) {

        //  This value accounts for ties; the other "place" is more of just a row number
        int placeNumber = PlacementManager.getPlacement(teamName);

        int nameWidth;
        if (omitPlaceNumber) {
            nameWidth = TextFormat.getTextWidth("» " + teamName);
        } else {
            nameWidth = TextFormat.getTextWidth(placeNumber + ". " + teamName);
        }

        String score = Integer.toString(ScoreManager.getTeamScore(teamName));
        int scoreWidth = TextFormat.getTextWidth(score);

        TextComponent.Builder str = Component.text();

        TextComponent team;
        if (omitPlaceNumber) {
            team =  Component.text("» " + TextFormat.FormatTeamName(teamName) + " ");
        } else {
            team =  Component.text(". " + TextFormat.FormatTeamName(teamName) + " ");
        }

        if (teamName.equals(highlightTeam)) {
            str.append(team.color(TextFormat.HIGHLIGHT_COLOR));
        } else {
            str.append(team);
        }

        if ((TextFormat.getTextWidth(str.content()) + scoreWidth) % 2 == 1) { // Odd number of pixels
            str.append(Component.text(TextFormat.PLUS_ONE_PX));
        }

        int numOfPeriods = (totalWidthPixels - (nameWidth+4) - (scoreWidth+4)) / 2;

        TextComponent dots = Component.text(".".repeat(numOfPeriods) + " ");
        if (teamName.equals(highlightTeam)) {
            str.append(dots).color(TextFormat.HIGHLIGHT_COLOR);
        } else {
            str.append(dots).color(NamedTextColor.GRAY);
        }

        return str.build();

    }

    //=========================================================================

}
