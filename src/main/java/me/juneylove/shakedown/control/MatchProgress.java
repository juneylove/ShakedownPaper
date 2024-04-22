package me.juneylove.shakedown.control;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import me.juneylove.shakedown.Main;
import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.mechanics.*;
import me.juneylove.shakedown.mechanics.abilities.HealBow;
import me.juneylove.shakedown.mechanics.worlds.WorldSetting;
import me.juneylove.shakedown.mechanics.worlds.WorldSettings;
import me.juneylove.shakedown.scoring.TeamManager;
import me.juneylove.shakedown.ui.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class MatchProgress {

    private static boolean playIsActive = false;
    private static boolean kitSelectionIsActive = false;
    static HashMap<WorldSetting, Boolean> roundEndedEarly = new HashMap<>();
    static HashMap<WorldSetting, Boolean> gameEndedEarly = new HashMap<>();

    static NamespacedKey pauseBarKey = null;

    // ==========

    public static boolean playIsActive() {
        return playIsActive;
    }

    public static boolean kitSelectionIsActive() {
        return kitSelectionIsActive;
    }

    // ==========

    private static Multimap<WorldSetting, String> worldTeamMap;

    public static Set<WorldSetting> allActiveWorlds() {
        return worldTeamMap.keySet();
    }

    public static Collection<String> allActiveTeams() {
        return worldTeamMap.values();
    }

    public static Collection<String> teamsInWorld(WorldSetting worldSetting) {
        return worldTeamMap.get(worldSetting);
    }

    // ==========

    public static void pause() {

        LabelBarManager.pauseActiveTimers();

        pauseBarKey = new NamespacedKey(Main.getInstance(), "pause-label");
        LabelBarManager.pauseActiveTimers();
        //noinspection unchecked
        LabelBarManager.createNew(pauseBarKey, (List<Player>) Bukkit.getOnlinePlayers(), false);
        LabelBarManager.setFormat(pauseBarKey, LabelBar.Side.CENTER, new LabelBar.Text(TextFormat.smallText("match paused")));

    }

    public static void resume() {

        if (pauseBarKey == null) return; // game was not paused, defensive check

        LabelBarManager.resumePausedTimers();
        LabelBarManager.delete(pauseBarKey);
        pauseBarKey = null;

    }

    public static void endRoundEarly(WorldSetting worldSetting) {

        if (!playIsActive()) return;

        roundEndedEarly.put(worldSetting, true);
        if (!roundEndedEarly.containsValue(false)) {
            // end round if all worlds are ended early
            onRoundFinish();
        } else {
            LabelBarManager.setWorldFormat(worldSetting.getWorld(), LabelBar.Side.CENTER, new LabelBar.Text(TextFormat.smallText("end of round")));
        }

    }

    public static void endGameEarly(WorldSetting worldSetting) {

        if (!playIsActive()) return;

        gameEndedEarly.put(worldSetting, true);
        if (!gameEndedEarly.containsValue(false)) {
            // end game if all worlds are ended early
            Games.CURRENT_GAME.currentRoundNum = Games.CURRENT_GAME.rounds.size();
            onRoundFinish();
        } else {
            LabelBarManager.setWorldFormat(worldSetting.getWorld(), LabelBar.Side.CENTER, new LabelBar.Text(TextFormat.smallText("end of game")));
        }

    }

    public static void incrementRound() {

        Games.CURRENT_GAME.currentRoundNum++;

        if (Games.CURRENT_GAME.currentRoundNum >= Games.CURRENT_GAME.rounds.size()) {
            Games.CURRENT_GAME.currentRound = null;
        } else {
            Games.CURRENT_GAME.currentRound = Games.CURRENT_GAME.rounds.get(Games.CURRENT_GAME.currentRoundNum - 1);
        }

    }

    //=========================================================================

    public static void startKitSelection(Duration duration) {

        kitSelectionIsActive = true;

        for (WorldSetting worldSetting : Games.CURRENT_GAME.currentRound.worldSettings) {
            LabelBarManager.newWorldFormat(worldSetting.getWorld(), LabelBar.Side.CENTER, new LabelBar.SmallTimer((int) duration.toSeconds(), "select your kit: "), false);
        }

    }

    public static void endKitSelection() {

        kitSelectionIsActive = false;

        for (WorldSetting worldSetting : Games.CURRENT_GAME.currentRound.worldSettings) {

            LabelBarManager.clearWorldFormat(worldSetting.getWorld());

            if (Games.CURRENT_GAME.currentRound.kitSetting instanceof KitSettings.Selection selection) {
                selection.endKitSelection(worldSetting.getWorld());
            }

        }

    }

    public static void startPreRoundCountdown() {

        String prefix;
        if (Games.CURRENT_GAME.rounds.size() == 1) {
            prefix = "game starts in: ";
        } else {
            prefix = "round starts in: ";
        }

        for (WorldSetting worldSetting : Games.CURRENT_GAME.currentRound.worldSettings) {
            LabelBarManager.newWorldFormat(worldSetting.getWorld(), LabelBar.Side.CENTER, new LabelBar.SmallTimer(Games.CURRENT_GAME.currentRound.preRoundCountdownSeconds, prefix), false);
        }

    }

    public static void endPreRoundCountdown() {

        for (WorldSetting worldSetting : Games.CURRENT_GAME.currentRound.worldSettings) {
            LabelBarManager.clearWorldFormat(worldSetting.getWorld());
        }

    }

    //=========================================================================

    public static void startGame(GameSetting gameSetting) {

        if (gameSetting == null) return;

        Games.CURRENT_GAME = gameSetting;
        if (!gameSetting.name.equals(Games.LOBBY)) Games.CURRENT_GAME_NUM = Games.COMPLETED_GAMES.size() + 1;

        Games.CURRENT_GAME.currentRoundNum = 1;

        Games.CURRENT_GAME.currentRound = Games.CURRENT_GAME.rounds.get(0);

    }

    public static void endGame() {

        Games.CURRENT_GAME = null;
        Controller.EndMatch();
        Bukkit.getPlayer("juneylove").sendMessage("endGame called");

    }

    protected static void BackToLobby() {

        if (Games.CURRENT_GAME != Games.GAMES.get(Games.LOBBY) && Games.CURRENT_GAME != null) {
            Games.COMPLETED_GAMES.add(Games.CURRENT_GAME.name);
        }

        Controller.SaveMatchState();
        Games.CURRENT_GAME_NUM = 0;

        startGame(Games.GAMES.get(Games.LOBBY));

        if (Games.COMPLETED_GAMES.size() == Main.NUM_OF_GAMES) {

            Controller.EndMatch();
            for (WorldSetting worldSetting : Games.CURRENT_GAME.currentRound.worldSettings) {
                LabelBarManager.newWorldFormat(worldSetting.getWorld(), LabelBar.Side.CENTER, new LabelBar.Text("Thanks for playing!"));
            }

        }

    }

    //=========================================================================

    public static void beginRound() {

        List<WorldSetting> worldSettings = Games.CURRENT_GAME.currentRound.worldSettings;
        worldTeamMap = distributeTeams(worldSettings);
        HashMap<String, Location> spawnLocations = assignSpawns(worldTeamMap);
        changeWorlds(worldTeamMap, spawnLocations, worldSettings);

        for (WorldSetting worldSetting : Games.CURRENT_GAME.currentRound.worldSettings) {

            Games.CURRENT_GAME.currentRound.kitSetting.setInventories(worldSetting.getWorld());

            for (Player player : worldSetting.getWorld().getPlayers()) {
                player.setCooldown(GUIFormat.menuSelectItem, 0);
            }

        }

        // TODO: round name title for players

    }

    public static void onRoundStart() {

        playIsActive = true;

        LabelBarManager.startAllTimers();

        for (WorldSetting worldSetting : Games.CURRENT_GAME.currentRound.worldSettings) {
            roundEndedEarly.put(worldSetting, false);
            Games.CURRENT_GAME.currentRound.kitSetting.onRoundStart(worldSetting.getWorld());
            WorldSettings.removeRoundBarriers(worldSetting);
        }

        if (Games.CURRENT_GAME.teammateGlowEnabled) {

            for (String ign : TeamManager.allGamePlayers()) {
                Player player = Bukkit.getPlayer(ign);
                if (player == null) continue;
                Set<String> teammates = TeamManager.getMembers(TeamManager.getTeam(ign));
                GlowManager.addGlow(player, TextFormat.GetNamedColor(TeamManager.getTeam(ign)), teammates, Games.CURRENT_GAME.currentRound.roundDurationSeconds*20);
            }

        }

    }

    public static void onRoundFinish() {

        playIsActive = false;
        LabelBarManager.pauseActiveTimers();

    }

    public static void endRound() {

        if (!(Games.CURRENT_GAME.currentRound.kitSetting instanceof KitSettings.Persistent)) {
            HealBow.ClearHealers();
        }

        for (WorldSetting worldSetting : Games.CURRENT_GAME.currentRound.worldSettings) {

            for (CapturePoint point : worldSetting.capturePoints) {
                point.remove();
            }

        }

        if (Games.CURRENT_GAME.currentRoundNum == Games.CURRENT_GAME.rounds.size()-1) {
            Games.CURRENT_GAME.onGameEnd();
        }

        roundEndedEarly.clear();

    }

    //=========================================================================

    private static Multimap<WorldSetting, String> distributeTeams(List<WorldSetting> worldSettings) {

        Multimap<WorldSetting, String> worldTeamMap = ArrayListMultimap.create();
        List<String> teams = TeamManager.gameTeams();
        int teamsAssigned = 0;

        for (WorldSetting worldSetting: worldSettings) {

            int teamsPerWorld = Games.CURRENT_GAME.currentRound.teamsPerWorld;
            if (teamsPerWorld == 0) teamsPerWorld = teams.size();

            for (int i=0; i<teamsPerWorld; i++) {

                String team = teams.get(teamsAssigned);
                worldTeamMap.put(worldSetting, team);
                teamsAssigned++;

            }

        }

        return worldTeamMap;

    }

    private static HashMap<String, Location> assignSpawns(Multimap<WorldSetting, String> worldTeamMap) {

        HashMap<String, Location> spawnAssignments = new HashMap<>();

        for (WorldSetting worldSetting : worldTeamMap.keySet()) {

            List<String> players = new ArrayList<>();
            for (String team : worldTeamMap.get(worldSetting)) {
                players.addAll(TeamManager.getMembers(team));
            }

            spawnAssignments.putAll(worldSetting.spawnSetting.GetSpawnLocations(players));

        }

        return spawnAssignments;

    }

    private static void changeWorlds(Multimap<WorldSetting, String> worldTeamMap, HashMap<String, Location> spawnLocations, List<WorldSetting> worldSettings) {

        for (String ign : spawnLocations.keySet()) {

            Player player = Bukkit.getPlayer(ign);
            if (player != null) {
                player.setBedSpawnLocation(spawnLocations.get(ign), true);
                player.teleport(spawnLocations.get(ign));
            }

        }

        LabelBarManager.deleteAll();

        for (WorldSetting worldSetting : worldSettings) {
            onWorldChange(worldSetting);

            Collection<String> teams = worldTeamMap.get(worldSetting);
            WorldSettings.addRoundBarriers(worldSetting, teams);
            WorldSettings.setTeamInvulnerability(worldSetting, teams);
        }

    }

    public static void onWorldChange(WorldSetting worldSetting) {

        World world = worldSetting.getWorld();

        for (Player player : world.getPlayers()) {

            // Set health to game global default
            Respawn.SetHealth(player);

            player.removeMetadata("lastDamager", Main.getInstance());

            if (TeamManager.isGamePlayer(player.getName())) {

                player.setGameMode(Games.CURRENT_GAME.defaultGameMode);
                player.setFlying(false);
                player.setAllowFlight(false);
                player.clearActivePotionEffects();

                // Set hunger enabled/disabled (apply hunger effect to hide health bar
                // if hunger is disabled)
                if (!Games.CURRENT_GAME.hungerEnabled) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, PotionEffect.INFINITE_DURATION, 0, true, false, false));
                }
                player.setFoodLevel(20);

            }

        }

        Respawn.reset();

        Sidebar.resetAll(world);
        Sidebar.setFormats(world);
        LabelBarManager.setAllFormats(world);

    }

    //=========================================================================

}
