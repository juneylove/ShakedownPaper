package me.juneylove.shakedown.games.fenceduel;

import me.juneylove.shakedown.control.Controller;
import me.juneylove.shakedown.control.MatchProgress;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.mechanics.worlds.WorldSetting;
import me.juneylove.shakedown.ui.LabelBar;
import me.juneylove.shakedown.ui.TextFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.title.Title;

import java.time.Duration;

public class FenceDuelScore {

    protected static final int roundsNeededToWin = 3; // 3 = best of 5, 4 = best of 7, etc.

    static final TextComponent roundVictory = Component.text(TextFormat.smallText("round win")).color(NamedTextColor.GREEN);
    static final TextComponent roundDraw = Component.text(TextFormat.smallText("round draw")).color(NamedTextColor.AQUA);
    static final TextComponent roundDefeat = Component.text(TextFormat.smallText("round loss")).color(NamedTextColor.RED);

    static final TextComponent gameVictory = Component.text(TextFormat.smallText("victory")).color(NamedTextColor.GREEN);
    static final TextComponent gameDraw = Component.text(TextFormat.smallText("draw")).color(NamedTextColor.AQUA);
    static final TextComponent gameDefeat = Component.text(TextFormat.smallText("defeat")).color(NamedTextColor.RED);

    static final Title.Times times = Title.Times.times(Duration.ZERO, Duration.ofSeconds(4), Duration.ofSeconds(1));

    private final TeleportManager teleportManager;
    private FenceDuelControl control;

    int redRoundsWon = 0;
    int blueRoundsWon = 0;

    FenceDuelScore(TeleportManager teleportManager) {
        this.teleportManager = teleportManager;
    }

    public void assignControl(FenceDuelControl control) {
        this.control = control;
    }

    protected boolean isScoreLimitReached() {
        return (redRoundsWon >= roundsNeededToWin || blueRoundsWon >= roundsNeededToWin);
    }

    protected void winRound(boolean red) {

        if (!MatchProgress.playIsActive()) return;
        if (Controller.isPaused()) return;

        if (red) {

            redRoundsWon++;
            teleportManager.getRedPlayer().showTitle(Title.title(roundVictory, Component.empty(), times));
            teleportManager.getBluePlayer().showTitle(Title.title(roundDefeat, Component.empty(), times));

        }
        else {

            blueRoundsWon++;
            teleportManager.getBluePlayer().showTitle(Title.title(roundVictory, Component.empty(), times));
            teleportManager.getRedPlayer().showTitle(Title.title(roundDefeat, Component.empty(), times));

        }

        control.onRoundFinish();

    }

    public void endRoundDraw() {

        redRoundsWon++;
        blueRoundsWon++;
        teleportManager.getRedPlayer().showTitle(Title.title(roundDraw, Component.empty(), times));
        teleportManager.getBluePlayer().showTitle(Title.title(roundDraw, Component.empty(), times));

    }

    protected void onScoreLimitReached() {

        if (redRoundsWon >= roundsNeededToWin && blueRoundsWon >= roundsNeededToWin) {
            // draw
            teleportManager.getRedPlayer().showTitle(Title.title(gameDraw, Component.empty(), times));
            teleportManager.getBluePlayer().showTitle(Title.title(gameDraw, Component.empty(), times));
        } else if (redRoundsWon >= roundsNeededToWin) {
            // red wins
            teleportManager.getRedPlayer().showTitle(Title.title(gameVictory, Component.empty(), times));
            teleportManager.getBluePlayer().showTitle(Title.title(gameDefeat, Component.empty(), times));
        } else if (blueRoundsWon >= roundsNeededToWin) {
            // blue wins
            teleportManager.getBluePlayer().showTitle(Title.title(gameVictory, Component.empty(), times));
            teleportManager.getRedPlayer().showTitle(Title.title(gameDefeat, Component.empty(), times));
        }

    }

    // ==========

    static final String roundOutline = "\uE06E";
    static final String roundFill    = "\uE06F";
    static final TextColor red = NamedTextColor.RED;
    static final TextColor blue = NamedTextColor.BLUE;
    static final TextComponent emptyRound = Component.text(roundOutline).color(NamedTextColor.WHITE);

    protected class LeftFenceDuelBar implements LabelBar.Format {

        TextComponent redWonRound;

        protected LeftFenceDuelBar() {
            this.redWonRound= Component.text(roundFill).color(red)
                    .append(TextFormat.negativeSpace(5))
                    .append(Component.text(roundOutline).color(NamedTextColor.WHITE));
        }

        @Override
        public TextComponent update() {

            TextComponent output = Component.empty()
                    .append(Component.text("■ ").color(red));

            for (int i=0; i<roundsNeededToWin; i++) {

                if (i<redRoundsWon) {
                    output = output.append(redWonRound);
                } else {
                    output = output.append(emptyRound);
                }
                if (i != roundsNeededToWin-1) {
                    output = output.append(Component.text(TextFormat.PLUS_ONE_PX));
                }

            }

            return output;

        }

    }

    protected class RightFenceDuelBar implements LabelBar.Format {

        TextComponent blueWonRound;

        protected RightFenceDuelBar() {
            this.blueWonRound= Component.text(roundFill).color(blue)
                    .append(TextFormat.negativeSpace(5))
                    .append(Component.text(roundOutline).color(NamedTextColor.WHITE));
        }

        @Override
        public TextComponent update() {

            TextComponent output = Component.empty();

            for (int i=0; i<roundsNeededToWin; i++) {

                if (i != 0) {
                    output = output.append(Component.text(TextFormat.PLUS_ONE_PX));
                }
                if (i >= (roundsNeededToWin-blueRoundsWon)) {
                    output = output.append(blueWonRound);
                } else {
                    output = output.append(emptyRound);
                }

            }

            output = output.append(Component.text(" ■").color(blue));

            return output;

        }

    }

}
