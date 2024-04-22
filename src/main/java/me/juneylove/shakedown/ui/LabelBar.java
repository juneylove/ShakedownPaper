package me.juneylove.shakedown.ui;

import me.juneylove.shakedown.Main;
import me.juneylove.shakedown.mechanics.Respawn;
import me.juneylove.shakedown.scoring.TeamManager;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class LabelBar {

    private final BossBar.Color color = BossBar.Color.PINK;
    private final BossBar.Overlay style = BossBar.Overlay.PROGRESS;
    private final int MARGIN_PX = 3;
    private final int X_RIGHT_LIMIT = 200;
    private final int X_LEFT_LIMIT = -X_RIGHT_LIMIT;

    public static final String OWN_TEAM = "own_team";
    public static final String OPPOSING_TEAM = "opposing_team";
    public static final String ROUND_NAME = "round_name";

    //=========================================================================

    BossBar spacerBar;
    BossBar bossBar;

    protected NamespacedKey key;
    private NamespacedKey spacerKey;
    List<Player> players = new ArrayList<>();

    HashMap<Side, TextComponent> labels = new HashMap<>();
    HashMap<Side, TextComponent> background = new HashMap<>();
    protected HashMap<Side, Format> formats = new HashMap<>();

    protected Component fullString = Component.empty();

    // Display control
    int xMinLeft = X_LEFT_LIMIT;
    int xMaxLeft = X_LEFT_LIMIT;
    int xMinCenter = 0;
    int xMaxCenter = 0;
    int xMinRight = X_RIGHT_LIMIT;
    int xMaxRight = X_RIGHT_LIMIT;

    //=========================================================================

    protected LabelBar(NamespacedKey key, List<Player> players) {

        this (key, players, true);

    }

    protected LabelBar(NamespacedKey key, List<Player> players, boolean includeSpacer) {

        this.key = key;
        this.players.addAll(players);
        create(includeSpacer);

    }

    protected LabelBar(NamespacedKey key, Player... players) {

        this(key, Arrays.asList(players));

    }

    //=========================================================================

    protected void addPlayer(Player player) {
        players.add(player);
        if (spacerBar != null) spacerBar.addViewer(player);
        bossBar.addViewer(player);
    }

    protected void addPlayer(String ign) {
        addPlayer(Bukkit.getPlayer(ign));
    }

    protected void removePlayer(Player player) {
        players.remove(player);
        if (spacerBar != null) spacerBar.removeViewer(player);
        bossBar.removeViewer(player);
    }

    protected void removePlayer(String ign) {
        removePlayer(Bukkit.getPlayer(ign));
    }

    protected void reAddPlayer(Player player) {

        for (Player player1 : players) {
            if (player1.getName().equals(player.getName())) {

                players.remove(player1);
                players.add(player);

                if (spacerBar != null) {
                    spacerBar.removeViewer(player1);
                    spacerBar.addViewer(player);
                }

                bossBar.removeViewer(player1);
                bossBar.addViewer(player);

            }
        }

    }

    //=========================================================================

    private void create(boolean includeSpacer) {

        String namespace = Main.getInstance().getName().toLowerCase();
        String rawKey = key.toString().substring(namespace.length()+1); // key without namespace
        spacerKey = NamespacedKey.fromString((rawKey + "-spacer"), Main.getInstance()); // avoid duplicating namespace in key

        Bukkit.removeBossBar(key);
        Bukkit.removeBossBar(spacerKey);

        if (includeSpacer) spacerBar = BossBar.bossBar(Component.empty(), 0, color, style);
        bossBar = BossBar.bossBar(fullString, 0, color, style);

        for (Player player : players) {
            if (includeSpacer) spacerBar.addViewer(player);
            bossBar.addViewer(player);
        }

    }

    protected void enable(Side side, Format format) {

        if (format instanceof None) {
            disable(side);
            return;
        }

        this.formats.put(side, format);
        TextComponent label = format.update();
        this.labels.put(side, label);

        int backgroundWidth = TextFormat.getTextWidth(label) + 2*MARGIN_PX;
        this.background.put(side, TextFormat.backgroundString(backgroundWidth));

        switch (side) {

            case LEFT:

                xMinLeft = X_LEFT_LIMIT;
                xMaxLeft = xMinLeft + backgroundWidth;
                break;

            case RIGHT:

                xMaxRight = X_RIGHT_LIMIT;
                xMinRight = xMaxRight - backgroundWidth;
                break;

            case CENTER:

                xMaxCenter = backgroundWidth / 2;
                xMinCenter = xMaxCenter - backgroundWidth;
                break;

        }

        update();

    }

    private void disable(Side side) {

        this.labels.remove(side);
        this.formats.remove(side);
        this.background.remove(side);

        switch (side) {

            case LEFT:

                xMinLeft = X_LEFT_LIMIT;
                xMaxLeft = X_LEFT_LIMIT;
                break;

            case RIGHT:

                xMaxRight = X_RIGHT_LIMIT;
                xMinRight = X_RIGHT_LIMIT;
                break;

            case CENTER:

                xMaxCenter = 0;
                xMinCenter = 0;
                break;

        }

        update();

    }

    protected void update() {

        Component backgroundPlusLabelLeft;
        Component backgroundPlusLabelCenter;
        Component backgroundPlusLabelRight;

        if (labels.containsKey(Side.LEFT)) {
            labels.put(Side.LEFT, formats.get(Side.LEFT).update());
            int backgroundWidth = TextFormat.getTextWidth(labels.get(Side.LEFT)) + 2*MARGIN_PX + 1;
            xMaxLeft = xMinLeft + backgroundWidth;
            background.put(Side.LEFT, TextFormat.backgroundString(backgroundWidth));
            backgroundPlusLabelLeft = Component.empty()
                    .append(background.get(Side.LEFT))
                    .append(TextFormat.negativeSpace((xMaxLeft - xMinLeft) - MARGIN_PX - 1))
                    .append(labels.get(Side.LEFT))
                    .append(TextFormat.padSpaces(MARGIN_PX));
        } else {
            backgroundPlusLabelLeft = Component.empty();
        }
        if (labels.containsKey(Side.CENTER)) {
            labels.put(Side.CENTER, formats.get(Side.CENTER).update());
            int backgroundWidth = TextFormat.getTextWidth(labels.get(Side.CENTER)) + 2*MARGIN_PX + 1;
            xMaxCenter = backgroundWidth / 2;
            xMinCenter = xMaxCenter - backgroundWidth;
            background.put(Side.CENTER, TextFormat.backgroundString(backgroundWidth));
            backgroundPlusLabelCenter = Component.empty()
                    .append(background.get(Side.CENTER))
                    .append(TextFormat.negativeSpace((xMaxCenter - xMinCenter) - MARGIN_PX - 1))
                    .append(labels.get(Side.CENTER))
                    .append(TextFormat.padSpaces(MARGIN_PX));
        } else {
            backgroundPlusLabelCenter = Component.empty();
        }
        if (labels.containsKey(Side.RIGHT)) {
            labels.put(Side.RIGHT, formats.get(Side.RIGHT).update());
            int backgroundWidth = TextFormat.getTextWidth(labels.get(Side.RIGHT)) + 2*MARGIN_PX + 1;
            xMinRight = xMaxRight - backgroundWidth;
            background.put(Side.RIGHT, TextFormat.backgroundString(backgroundWidth));
            backgroundPlusLabelRight = Component.empty()
                    .append(background.get(Side.RIGHT))
                    .append(TextFormat.negativeSpace((xMaxRight - xMinRight) - MARGIN_PX - 1))
                    .append(labels.get(Side.RIGHT));
        } else {
            backgroundPlusLabelRight = Component.empty();
        }

        int leftPad = xMinCenter - xMaxLeft;
        int rightPad = xMinRight - xMaxCenter;

        fullString = Component.empty()
                .append(backgroundPlusLabelLeft)
                .append(TextFormat.padSpaces(leftPad))
                .append(backgroundPlusLabelCenter)
                .append(TextFormat.padSpaces(rightPad))
                .append(backgroundPlusLabelRight);

        if (bossBar != null) {
            bossBar.name(fullString);
        }

    }

    protected void delete() {
        if (spacerBar != null) {
            Bukkit.getServer().hideBossBar(spacerBar);
        }
        Bukkit.getServer().hideBossBar(bossBar);
    }

    //=========================================================================

    public interface Format {
        TextComponent update();
    }

    public static class None implements Format {

        @Override
        public TextComponent update() {
            return Component.empty();
        }

    }

    public static class Text implements Format {

        TextComponent text;

        public Text(String text) {
            this.text = Component.text(text);
        }

        @Override
        public TextComponent update() {
             return text;
        }

    }

    public static class Timer implements Format {

        boolean active;
        boolean paused = false;
        long initialDuration;
        long remainingSeconds;
        Instant zeroTime;
        String prefix;

        public Timer(int seconds, String prefix) {
            this.initialDuration = seconds;
            this.prefix = prefix;
            reset();
        }

        public Timer (int seconds) {
            this(seconds, "");
        }

        public void start() {
            this.zeroTime = Instant.now().plusSeconds(remainingSeconds);
            this.active = true;
            this.paused = false;
        }

        public void pause() {

            if (zeroTime == null) {
                this.remainingSeconds = initialDuration;
            } else {
                this.remainingSeconds = Duration.between(Instant.now(), zeroTime).toSeconds();
            }
            this.active = false;
            this.paused = true;

        }

        public void reset() {
            this.remainingSeconds = this.initialDuration;
            this.active = false;
        }

        @Override
        public TextComponent update() {

            long seconds;
            if (active) {
                seconds = Duration.between(Instant.now(), zeroTime).toSeconds();
            } else {
                seconds = remainingSeconds;
            }
            if (seconds < 0) seconds = 0;

            String str = prefix + seconds / 60 + ":" + String.format("%02d", seconds % 60);
            return Component.text(str);

        }

    }

    public static class SmallTimer extends Timer implements Format {

        public SmallTimer(int seconds, String prefix) {
            super(seconds, prefix);
        }

        public SmallTimer(int seconds) {
            super(seconds);
        }

        @Override
        public TextComponent update() {

            TextComponent normalText = super.update();
            String rawText = normalText.content();
            return Component.text(TextFormat.smallText(rawText));

        }
    }

    public static class Stopwatch extends Timer implements Format {

        long accruedSeconds = 0;

        public Stopwatch(String prefix) {
            super(0, prefix);
        }

        @Override
        public void start() {
            this.zeroTime = Instant.now().minusSeconds(accruedSeconds);
            this.active = true;
            this.paused = false;
        }

        @Override
        public void pause() {

            if (zeroTime == null) {
                this.accruedSeconds = 0;
            } else {
                this.accruedSeconds = Duration.between(zeroTime, Instant.now()).toSeconds();
            }
            this.active = false;
            this.paused = true;

        }

        @Override
        public TextComponent update() {

            long seconds;
            if (zeroTime == null) {
                seconds = 0;
            } else if (active) {
                seconds = Duration.between(zeroTime, Instant.now()).toSeconds();
            } else {
                seconds = accruedSeconds;
            }
            String str = prefix + seconds / 60 + ":" + String.format("%02d", seconds % 60);
            return Component.text(str);

        }

    }

    public static class SmallStopwatch extends Stopwatch implements Format {

        public SmallStopwatch(String prefix) {
            super(prefix);
        }

        @Override
        public TextComponent update() {

            TextComponent normalText = super.update();
            String rawText = normalText.content();
            return Component.text(TextFormat.smallText(rawText));

        }

    }

    public static class TeamLifeStatus implements Format {

        String team;
        final TextComponent spacer = Component.text(TextFormat.PLUS_ONE_PX + TextFormat.PLUS_ONE_PX);
        TextComponent alive;
        TextComponent dead =  TextFormat.DEAD_MEMBER;

        public TeamLifeStatus(String team) {
            this.team = team;
            this.alive =  TextFormat.GetTeamMemberIcon(team);
        }

        @Override
        public TextComponent update() {

            int teamSize = TeamManager.teamSize(team);
            int teammatesAlive = Respawn.TeammatesAlive(team);

            TextComponent full = Component.empty();

            for (int i=0; i<teamSize; i++) {

                if (i<teammatesAlive) {
                    full = full.append(alive);
                } else {
                    full = full.append(dead);
                }

                if (i != teamSize-1) {
                    full = full.append(spacer);
                }

            }

            return full;

        }

    }

    public static class DualTeamLifeStatus implements Format {

        TeamLifeStatus left;
        TeamLifeStatus right;

        final TextComponent divider = Component.text(" | ").color(NamedTextColor.WHITE);

        public DualTeamLifeStatus(String team1, String team2) {
            left = new TeamLifeStatus(team1);
            right = new TeamLifeStatus(team2);
        }

        @Override
        public TextComponent update() {
            return left.update().append(divider).append(right.update());
        }
    }

    public static class AllTeamsLifeStatus implements Format {

        @Override
        public TextComponent update() {

            StringBuilder str = new StringBuilder();

            for (String team : TeamManager.gameTeams()){

                str.append(TextFormat.GetTeamLogo(team));
                if (Respawn.TeammatesAlive(team) == 0) {
                    str.append(TextFormat.negativeSpace(9)).append(TextFormat.DEAD_TEAM);
                }
                str.append(TextFormat.PLUS_ONE_PX).append(TextFormat.PLUS_ONE_PX);

            }
            String full = str.toString();
            return Component.text(full.substring(0, full.length()-2)); // get rid of last two 1-px characters

        }

    }

    //=========================================================================

    public enum Side {

        LEFT,
        CENTER,
        RIGHT

    }

    //=========================================================================

}
