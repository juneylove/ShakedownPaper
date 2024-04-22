package me.juneylove.shakedown.ui;

import me.juneylove.shakedown.Main;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.scoring.TeamManager;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class LabelBarManager {

    @SuppressWarnings("FieldMayBeFinal")
    private static HashMap<NamespacedKey, LabelBar> registry = new HashMap<>();

    //=========================================================================

    public static void createNew(NamespacedKey key, List<Player> players, boolean includeSpacer) {

        if (!barExists(key)) {
            LabelBar bar = new LabelBar(key, players, includeSpacer);
            registry.put(key, bar);
        }

    }

    public static void createNew(NamespacedKey key, List<Player> players) {

        createNew(key, players, true);

    }

    public static void createNew(NamespacedKey key, Player... players) {

        createNew(key, Arrays.asList(players));

    }

    public static void createNew(NamespacedKey key, World world) {

        createNew(key, world.getPlayers());

    }

    public static void createNew(NamespacedKey key, String worldName) {

        createNew(key, Objects.requireNonNull(Bukkit.getWorld(worldName)));

    }

    //=========================================================================

    public static boolean barExists(NamespacedKey key) {
        return registry.containsKey(key);
    }

    public static void setTeamFormat(String team, LabelBar.Side side, LabelBar.Format format) {
        setFormat(getTeamKey(team), side, format);
    }

    public static void setWorldFormat(World world, LabelBar.Side side, LabelBar.Format format) {
        setFormat(getWorldKey(world.getName()), side, format);
    }

    public static void setFormat(NamespacedKey key, LabelBar.Side side, LabelBar.Format format) {

        if (barExists(key)) registry.get(key).enable(side, format);

    }

    public static void newWorldFormat(World world, LabelBar.Side side, LabelBar.Format format) {
        newWorldFormat(world, side, format, true);
    }

    public static void newWorldFormat(World world, LabelBar.Side side, LabelBar.Format format, boolean includeSpacer) {

        if (format instanceof LabelBar.TeamLifeStatus) return;
        if (format instanceof LabelBar.AllTeamsLifeStatus) return;

        NamespacedKey key = getWorldKey(world.getName());

        createNew(key, world.getPlayers(), includeSpacer);
        setFormat(key, side, format);

        if (format instanceof LabelBar.Timer timer) {
            timer.start();
        }

    }

    public static void clearWorldFormat(World world) {

        NamespacedKey key = getWorldKey(world.getName());

        delete(key);

    }

    public static void setAllFormats(World world) {

        setAllFormats(world.getPlayers());

    }

    public static void setAllFormats(List<Player> players) {

        // Create separate bars for each team
        List<String> teams = new ArrayList<>();
        for (Player player : players) {

            String team = TeamManager.getTeam(player.getName());
            if (!teams.contains(team)) teams.add(team);

        }

        for (String team : teams) {

            List<Player> teamPlayers = new ArrayList<>();
            Set<String> members = TeamManager.getMembers(team);
            for (String member : members) {
                if (Bukkit.getPlayer(member) != null) teamPlayers.add(Bukkit.getPlayer(member));
            }

            NamespacedKey key = getTeamKey(team);

            if (TeamManager.isGameTeam(team)) {

                createNew(key, teamPlayers);
                for (LabelBar.Side side : Games.CURRENT_GAME.labelBarFormats.keySet()) {

                    // Adapt specific instances of certain formats on a per-team basis
                    // this feels so jank lol
                    LabelBar.Format format = Games.CURRENT_GAME.labelBarFormats.get(side);
                    if (format instanceof LabelBar.TeamLifeStatus status) {

                        if (status.team.equals(LabelBar.OWN_TEAM)) {

                            format = new LabelBar.TeamLifeStatus(team);

                        } else if (status.team.equals(LabelBar.OPPOSING_TEAM)) {

                            String otherTeam = findOpposingTeam(team, teams);
                            if (otherTeam == null) {
                                format = new LabelBar.None();
                            } else {
                                format = new LabelBar.TeamLifeStatus(otherTeam);
                            }

                        }

                    } else if (format instanceof LabelBar.DualTeamLifeStatus) {

                        // assume own team + opposing team for now
                        String otherTeam = findOpposingTeam(team, teams);
                        format = new LabelBar.DualTeamLifeStatus(team, otherTeam);

                        // Also reset all timers because they're buggy and annoying
                    } else if (format instanceof LabelBar.SmallTimer timer) {

                        timer.reset();
                        if (timer.prefix.equals(LabelBar.ROUND_NAME)) {
                            format = new LabelBar.SmallTimer((int) timer.initialDuration, Games.CURRENT_GAME.currentRound.name + ": ");
                        }

                    } else if (format instanceof LabelBar.Timer timer) {

                        timer.reset();
                        if (timer.prefix.equals(LabelBar.ROUND_NAME)) {
                            format = new LabelBar.Timer((int) timer.initialDuration, Games.CURRENT_GAME.currentRound.name + ": ");
                        }

                    }

                    setFormat(key, side, format);

                }

            } else if (team.equals(TeamManager.SPECTATOR)) {

                createNew(key, teamPlayers);
                setFormat(key, LabelBar.Side.CENTER, new LabelBar.Timer(Games.CURRENT_GAME.currentRound.roundDurationSeconds, Games.CURRENT_GAME.currentRound.name + ": "));

            }

        }

    }

    @NotNull
    private static NamespacedKey getWorldKey(String world) {
        String keyTag = Games.CURRENT_GAME.name + "-" + Games.CURRENT_GAME.currentRound.name + "-" + world;
        keyTag = keyTag.toLowerCase().replace(' ', '_');
        NamespacedKey key = new NamespacedKey(Main.getInstance(), keyTag);
        return key;
    }

    @NotNull
    private static NamespacedKey getTeamKey(String team) {
        String keyTag = Games.CURRENT_GAME.name + "-" + Games.CURRENT_GAME.currentRound.name + "-" + team;
        keyTag = keyTag.toLowerCase().replace(' ', '_');
        NamespacedKey key = new NamespacedKey(Main.getInstance(), keyTag);
        return key;
    }

    private static String findOpposingTeam(String team, List<String> teams) {

        for (String otherTeam : teams) {

            if (otherTeam.equals(team)) continue;
            if (!TeamManager.isGameTeam(otherTeam)) continue;
            return otherTeam;

        }
        return "Team2"; // TEMP, SHOULD BE NULL

    }

    public static void updateAll() {

        for (LabelBar bar : registry.values()) {
            bar.update();
        }

    }

    public static void resumePausedTimers() {

        for (LabelBar bar : registry.values()) {
            for (LabelBar.Format format : bar.formats.values()) {

                if (format instanceof LabelBar.Timer timer && timer.paused) {
                    timer.start();
                }

            }
        }

    }

    public static void startAllTimers() {

        for (LabelBar bar : registry.values()) {
            for (LabelBar.Format format : bar.formats.values()) {

                if (format instanceof LabelBar.Timer timer) {
                    timer.start();
                }

            }
        }

    }

    public static void pauseActiveTimers() {

        for (LabelBar bar : registry.values()) {
            for (LabelBar.Format format : bar.formats.values()) {

                if (format instanceof LabelBar.Timer timer && timer.active) {
                    timer.pause();
                }

            }
        }

    }

    public static void reAddPlayer(Player player) {

        for (LabelBar bar : registry.values()) {
            bar.reAddPlayer(player);
        }

    }

    public static void delete(NamespacedKey key) {

        if (registry.get(key) != null) registry.get(key).delete();
        registry.remove(key);

    }

    public static void deleteAll() {

        for (NamespacedKey key : registry.keySet()) {
            delete(key);
        }

    }

    //=========================================================================

}
