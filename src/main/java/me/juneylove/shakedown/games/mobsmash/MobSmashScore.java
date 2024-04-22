package me.juneylove.shakedown.games.mobsmash;

import me.juneylove.shakedown.control.MatchProgress;
import me.juneylove.shakedown.mechanics.worlds.WorldSetting;
import me.juneylove.shakedown.scoring.ScoreManager;
import me.juneylove.shakedown.ui.LabelBar;
import me.juneylove.shakedown.ui.TextFormat;
import me.juneylove.shakedown.ui.TitleManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MobSmashScore {

    static final int pointsToWinRound = 150;
    static final int scorePerSecond = 1;
    static final int roundsNeededToWin = 3; // 3 = best of 5, 4 = best of 7, etc.

    static final int teamPointsForWinningGame = 500; // ScoreManager points

    static boolean roundEndedEarly = false; // score limit reached

    static TextComponent roundVictory = Component.text(TextFormat.smallText("round victory")).color(NamedTextColor.GREEN);
    static TextComponent roundDraw = Component.text(TextFormat.smallText("round draw")).color(NamedTextColor.AQUA);
    static TextComponent roundDefeat = Component.text(TextFormat.smallText("round defeat")).color(NamedTextColor.RED);

    static TextComponent gameVictory = Component.text(TextFormat.smallText("game victory")).color(NamedTextColor.GREEN);
    static TextComponent gameDraw = Component.text(TextFormat.smallText("game draw")).color(NamedTextColor.AQUA);
    static TextComponent gameDefeat = Component.text(TextFormat.smallText("game defeat")).color(NamedTextColor.RED);

    HashMap<String, Integer> currentScore = new HashMap<>();
    HashMap<String, Integer> roundScore   = new HashMap<>();

    MobSmashControl control;

    protected MobSmashScore(MobSmashControl control) {
        this.control = control;
    }

    public void scoreTicker() {

        if (!MatchProgress.playIsActive()) return;

        String team = CapturePointManager.controllingTeam;
        WorldSetting worldSetting = findWorld(team);

        if (team != null && worldSetting != null) {

            int newScore = currentScore.get(team) + scorePerSecond;
            currentScore.put(team, newScore);

            if (newScore >= pointsToWinRound) {
                roundEndedEarly = true;
                winRound(List.of(team), worldSetting);
            }

        }

    }

    private void winRound(List<String> roundWinningTeams, WorldSetting worldSetting) {

        List<String> gameWinningTeams = new ArrayList<>();

        for (String team : roundWinningTeams) {

            int newRoundScore = roundScore.get(team) + 1;
            roundScore.put(team, newRoundScore);
            if (newRoundScore >= roundsNeededToWin) gameWinningTeams.add(team);

        }

        if (gameWinningTeams.size() == 0) {

            // if game has not been won, give round end titles

            TextComponent subtitle; // same for everyone

            if (roundWinningTeams.size() == 1) {

                String winSubtitle = "Winner: " + roundWinningTeams.get(0);
                subtitle = Component.text(winSubtitle);
                TitleManager.showToTeam(roundWinningTeams.get(0), roundVictory, subtitle);

            } else {

                // round tie
                StringBuilder drawSubtitleBuilder = new StringBuilder("Winners: ");
                for (String team : roundWinningTeams) {
                    drawSubtitleBuilder.append(team).append(", ");
                }
                String drawSubtitle = drawSubtitleBuilder.substring(0, drawSubtitleBuilder.length()-2); // remove last 2 chars (, )
                subtitle = Component.text(drawSubtitle);
                for (String team : roundWinningTeams) {
                    TitleManager.showToTeam(team, roundDraw, subtitle);
                }

            }

            for (String team : MatchProgress.teamsInWorld(worldSetting)) {
                if (!roundWinningTeams.contains(team)) {
                    TitleManager.showToTeam(team, roundDefeat, subtitle);
                }
            }

            // finish round
            control.onRoundFinish();

        } else {
            winGame(gameWinningTeams, worldSetting);
        }

    }

    private void winGame(List<String> gameWinningTeams, WorldSetting worldSetting) {

        // if roundsNeededToWin is reached, end game
        if (gameWinningTeams.size() > 0) { // defensive

            TextComponent subtitle; // same for everyone

            if (gameWinningTeams.size() == 1) {

                // normal win condition, one team wins
                ScoreManager.addTeamScore(gameWinningTeams.get(0), teamPointsForWinningGame);
                String winSubtitle = "Winner: " + gameWinningTeams.get(0);
                subtitle = Component.text(winSubtitle);
                TitleManager.showToTeam(gameWinningTeams.get(0), gameVictory, subtitle);

            } else {

                // tie condition
                int pointsForEachTeam = teamPointsForWinningGame / gameWinningTeams.size();
                StringBuilder drawSubtitleBuilder = new StringBuilder("Winners: ");
                for (String winningTeam : gameWinningTeams) {
                    ScoreManager.addTeamScore(winningTeam, pointsForEachTeam);
                    drawSubtitleBuilder.append(winningTeam).append(", ");
                }
                String drawSubtitle = drawSubtitleBuilder.substring(0, drawSubtitleBuilder.length()-2); // remove last 2 chars (, )
                subtitle = Component.text(drawSubtitle);
                for (String winningTeam : gameWinningTeams) {
                    TitleManager.showToTeam(winningTeam, gameDraw, subtitle);
                }

            }

            for (String team : MatchProgress.teamsInWorld(worldSetting)) {
                if (!gameWinningTeams.contains(team)) {
                    TitleManager.showToTeam(team, gameDefeat, subtitle);
                }
            }

        }

        control.onGameEnd();

    }

    private WorldSetting findWorld(String team) {

        for (WorldSetting worldSetting : MatchProgress.allActiveWorlds()) {
            if (MatchProgress.teamsInWorld(worldSetting).contains(team)) return worldSetting;
        }
        return null;

    }

    public void onWorldChange() {
        currentScore.clear();
        for (String team : MatchProgress.allActiveTeams()) {
            currentScore.put(team, 0);
            roundScore.putIfAbsent(team, 0);
        }
    }

    public void onRoundStart() {
        roundEndedEarly = false;
    }

    public void timeLimitReached() {

        // award round win to team with most points if time limit was reached
        if (!roundEndedEarly) {
            for (WorldSetting worldSetting : MatchProgress.allActiveWorlds()) {

                Collection<String> teams = MatchProgress.teamsInWorld(worldSetting);
                int max = 0;
                List<String> winningTeams = new ArrayList<>(); // will be size 1 unless a tie occurs
                for (String team : teams) {
                    if (currentScore.get(team) > max) {
                        max = currentScore.get(team);
                        winningTeams.clear();
                        winningTeams.add(team);
                    } else if (currentScore.get(team) == max) {
                        winningTeams.add(team);
                    }
                }
                winRound(winningTeams, worldSetting);

            }
        }

    }

    static final String roundOutline = "\uE06E";
    static final String roundFill    = "\uE06F";

    public class MobSmashScoreLabelBar implements LabelBar.Format {

        String team1;
        String team2;
        TextColor teamColor1;
        TextColor teamColor2;

        TextComponent wonRound1;
        TextComponent wonRound2;
        final TextComponent emptyRound = Component.text(roundOutline).color(NamedTextColor.WHITE);

        public MobSmashScoreLabelBar(String team1, String team2) {
            this.team1 = "Team1"; // TEMP
            this.team2 = "Team2"; // TEMP
        }

        @Override
        public TextComponent update() {

            this.teamColor1 = TextFormat.GetTextColor(team1);
            this.teamColor2 = TextFormat.GetTextColor(team2);
            this.wonRound1= Component.text(roundFill).color(teamColor1)
                    .append(TextFormat.negativeSpace(5))
                    .append(Component.text(roundOutline).color(NamedTextColor.WHITE));
            this.wonRound2= Component.text(roundFill).color(teamColor2)
                    .append(TextFormat.negativeSpace(5))
                    .append(Component.text(roundOutline).color(NamedTextColor.WHITE));

            int roundScore1;
            if (roundScore.get(team1) == null) {
                roundScore1 = 0;
            } else {
                roundScore1 = roundScore.get(team1);
            }
            int roundScore2;
            if (roundScore.get(team2) == null) {
                roundScore2 = 0;
            } else {
                roundScore2 = roundScore.get(team2);
            }

            // left to right
            TextComponent team1Rounds = Component.empty();
            for (int i=0; i<roundsNeededToWin; i++) {
                if (i < roundScore1) {
                    team1Rounds = team1Rounds.append(wonRound1);
                } else {
                    team1Rounds = team1Rounds.append(emptyRound);
                }
                if (i != roundsNeededToWin-1) {
                    team1Rounds = team1Rounds.append(Component.text(TextFormat.PLUS_ONE_PX));
                }
            }

            int score1;
            if (currentScore.get(team1) == null) {
                score1 = 0;
            } else {
                score1 = currentScore.get(team1);
            }
            int score2;
            if (currentScore.get(team2) == null) {
                score2 = 0;
            } else {
                score2 = currentScore.get(team2);
            }

            TextComponent score = Component.empty()
                    .append(Component.text(score1).color(teamColor1))
                    .append(Component.text(" - ")).color(NamedTextColor.WHITE)
                    .append(Component.text(score2).color(teamColor2));

            TextComponent team2Rounds = Component.empty();
            for (int i=0; i<roundsNeededToWin; i++) {
                if (i >= (roundsNeededToWin - roundScore2)) {
                    team2Rounds = team2Rounds.append(wonRound2);
                } else {
                    team2Rounds = team2Rounds.append(emptyRound);
                }
                if (i != roundsNeededToWin-1) {
                    team2Rounds = team2Rounds.append(Component.text(TextFormat.PLUS_ONE_PX));
                }
            }

            return team1Rounds.append(Component.space()).append(score).append(Component.space()).append(team2Rounds);

        }

    }

}
