package me.juneylove.shakedown.control;

import me.juneylove.shakedown.Main;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.games.Lobby;
import me.juneylove.shakedown.games.chorusvolley.ChorusVolleySetting;
import me.juneylove.shakedown.games.fenceduel.FenceDuelSetting;
import me.juneylove.shakedown.games.mobsmash.MobSmashSetting;
import me.juneylove.shakedown.games.rapidodge.RapidodgeSetting;
import me.juneylove.shakedown.mechanics.GlowManager;
import me.juneylove.shakedown.mechanics.LootChestTable;
import me.juneylove.shakedown.mechanics.LootChests;
import me.juneylove.shakedown.mechanics.Respawn;
import me.juneylove.shakedown.scoring.PlacementManager;
import me.juneylove.shakedown.scoring.ScoreManager;
import me.juneylove.shakedown.scoring.TeamManager;
import me.juneylove.shakedown.ui.LabelBarManager;
import me.juneylove.shakedown.ui.Sidebar;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class Controller {

    static NamespacedKey key;

    static Plugin plugin = null;

    private static final long CONSTANT_GAME_TICK_PERIOD = 1;
    static BukkitTask TickerTask = null;

    private static void GetPlugin() {
        plugin  = Main.getInstance();
    }

    //=========================================================================

    protected static void TickerConstant() {

        // Update immediate events like death effects and chat messages
        if (LootChestTable.loaded) {
            LootChests.ClearRecentOpens();
        }

        GlowManager.glowTicker();

        Games.CURRENT_GAME.tickerConstant();

    }

    protected static void Ticker1Second() {

        // Update scores, scoreboard, boss bars, timers
        Sidebar.updateAll();
        LabelBarManager.updateAll();
        Respawn.RespawnTicker();

        Games.CURRENT_GAME.ticker1Second();

    }

    protected static void Ticker5Second() {

        // Update game progress/phases

        Games.CURRENT_GAME.ticker5Second();

    }

    protected static void Ticker15Second() {

        // Might not use

        Games.CURRENT_GAME.ticker15Second();

    }

    //=========================================================================

    // Start match - this is the very beginning, should ONLY be called from player (op) command
    // Returns false if teams are not configured properly (evenly), or if game is already in progress
    protected static boolean StartMatch() {

        GetPlugin();

        if (Main.GAME_IN_PROGRESS || TickerTask != null) {
            plugin.getLogger().warning("Controller: Cannot start match, match already in progress");
            return false;
        }

        if (!TeamManager.verifyTeamsIntegrity(plugin)) {
            plugin.getLogger().warning("Controller: Aborted starting match, team data not configured properly");
            return false;
        }

        plugin.getLogger().info("Controller: Starting match");

        // Everything is ready, start the match :D
        PlacementManager.initializePlacements();
        PlacementManager.recalculateAll();
        TickerTask = new TickerRunnable().runTaskTimer(plugin, 0, CONSTANT_GAME_TICK_PERIOD);
        Main.GAME_IN_PROGRESS = true;
        //Games.CURRENT_GAME = new MobSmashSetting();
        //Games.CURRENT_GAME = new RapidodgeSetting();
        //Games.CURRENT_GAME = new FenceDuelSetting();
        Games.CURRENT_GAME = new ChorusVolleySetting();
        //Games.CURRENT_GAME = new Lobby();
        Games.CURRENT_GAME.onGameStart();

        // TEMP FOR TESTING
        new LootChestTable().LoadLootTables();

        return true;

    }

    // End match - should only be called automatically, or in the event of a reset
    protected static void EndMatch() {

        if (Main.GAME_IN_PROGRESS) {
            TickerTask.cancel();
            TickerTask = null;
            Main.GAME_IN_PROGRESS = false;
        } else {
            plugin.getLogger().warning("Controller: EndMatch called while match not in progress!");
        }

    }

    // Pause match progress, returns false if game is not running
    protected static boolean PauseMatch() {

        if (Main.GAME_IN_PROGRESS) {
            TickerTask.cancel();
            Games.CURRENT_GAME.onGamePause();
            Main.GAME_IN_PROGRESS = false;
            return true;
        }
        return false;

    }

    // Resume match, return false if game is already running
    protected static boolean ResumeMatch() {

        // "TickerTask != null" -> game has been initialized/started
        if (!Main.GAME_IN_PROGRESS && TickerTask != null) {
            TickerTask = new TickerRunnable().runTaskTimer(plugin, 0, CONSTANT_GAME_TICK_PERIOD);
            Games.CURRENT_GAME.onGameResume();
            Main.GAME_IN_PROGRESS = true;
            return true;
        }
        return false;

    }

    public static boolean isPaused() {
        return (TickerTask != null && !Main.GAME_IN_PROGRESS);
    }

    //=========================================================================

    public static boolean SaveMatchState() {

        int numOfFailures = 0;

        if (plugin == null) {
            GetPlugin();
        }

        if (!TeamManager.backupTeams()) {
            plugin.getLogger().warning("SaveMatchState: Failed to save team information");
            numOfFailures++;
        }
        if (!ScoreManager.backupScores()) {
            plugin.getLogger().warning("SaveMatchState: Failed to save score information");
            numOfFailures++;
        }
        if (!Games.SavePlayedGames()) {
            plugin.getLogger().warning("SaveMatchState: Failed to save game information");
            numOfFailures++;
        }

        if (numOfFailures > 0) {
            plugin.getLogger().warning("SaveMatchState: " + numOfFailures + " total failure(s) in save process");
            return false;
        }

        return true;

    }

    public static boolean RestoreMatchState() {

        int numOfFailures = 0;

        if (plugin == null) {
            GetPlugin();
        }

        if (!TeamManager.loadTeamsFromBackup()) {
            plugin.getLogger().warning("RestoreMatchState: Failed to load team information");
            numOfFailures++;
        }
        if (!ScoreManager.loadScoresFromBackup()) {
            plugin.getLogger().warning("RestoreMatchState: Failed to load score information");
            numOfFailures++;
        }
        if (!Games.LoadPlayedGames()) {
            plugin.getLogger().warning("RestoreMatchState: Failed to load game information");
            numOfFailures++;
        }

        if (numOfFailures > 0) {
            plugin.getLogger().warning("SaveMatchState: " + numOfFailures + " total failure(s) in load process");
            return false;
        }

        return true;

    }

    //=========================================================================
}
