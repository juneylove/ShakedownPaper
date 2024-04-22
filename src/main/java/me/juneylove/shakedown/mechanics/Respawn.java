package me.juneylove.shakedown.mechanics;

import me.juneylove.shakedown.Main;
import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.scoring.TeamManager;
import me.juneylove.shakedown.ui.TextFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@SuppressWarnings("FieldMayBeFinal")
public class Respawn {

    private static List<String> TEMP_SPECTATE_IGNS = new ArrayList<>();              // Players waiting for respawn AND players who are dead for the rest of the game / out of lives
    private static HashMap<String, Instant> RESPAWN_TIMES = new HashMap<>();         // Specific times that players will respawn
    private static HashMap<String, ItemStack[]> SAVED_INVENTORIES = new HashMap<>(); // used ONLY for respawn events
    private static HashMap<String, Integer> REMAINING_LIVES = new HashMap<>();       // Only used if lives are not infinite (game setting numberOfLivesPerRound=0 -> infinite lives)

    //=========================================================================

    public static void TempSpecOn(Player player) {

        TEMP_SPECTATE_IGNS.add(player.getName());
        new TempSpecRunnable(player).runTask(Main.getInstance());

    }

    public static void RespawnPlayer(Player player) {

        RESPAWN_TIMES.remove(player.getName());

        TEMP_SPECTATE_IGNS.remove(player.getName());
        player.setVisibleByDefault(true);
        player.setCollidable(true);
        player.setAllowFlight(false);
        player.setFlying(false);
        player.resetTitle();

        player.removeMetadata("lastDamager", Main.getInstance());

        Location spawnLocation = player.getBedSpawnLocation();
        if (spawnLocation == null) {
            spawnLocation = player.getWorld().getSpawnLocation();
        }

        player.teleport(spawnLocation, PlayerTeleportEvent.TeleportCause.SPECTATE);
        SetHealth(player);

        // Restore inventory with defensive check in case it didn't save
        if (SAVED_INVENTORIES.containsKey(player.getName())) {
            //player.getInventory().setContents(SAVED_INVENTORIES.get(player.getName()));
        }

        Games.CURRENT_GAME.onPlayerRespawn(player);
        Games.CURRENT_GAME.currentRound.kitSetting.onRespawn(player);

    }

    public static boolean IsTempSpec(String ign) {

        return TEMP_SPECTATE_IGNS.contains(ign);

    }

    public static int TempSpecCount(String team) {

        Set<String> members = TeamManager.getMembers(team);
        int total = 0;
        for (String member : members) {
            if (IsTempSpec(member)) total++;
        }

        return total;

    }

    public static List<String> TempSpecMembers(String team) {

        Set<String> members = TeamManager.getMembers(team);
        List<String> tempSpecMembers = new ArrayList<>();
        for (String member : members) {
            if (IsTempSpec(member)) tempSpecMembers.add(member);
        }

        return tempSpecMembers;

    }

    public static int TeammatesAlive(String team) {

        int total = 0;
        for (String ign : TeamManager.getMembers(team)) {
            if (!IsTempSpec(ign)) total++;
        }
        return total;

    }

    public static int RemainingLives(Player player) {

        return REMAINING_LIVES.getOrDefault(player.getName(), 1);

    }

    public static void NewDeath(Player player) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        int remainingLives;
        if (REMAINING_LIVES.containsKey(player.getName())) {
            remainingLives = REMAINING_LIVES.get(player.getName()) - 1;
        } else {
            remainingLives = game.currentRound.numberOfLives - 1;
        }

        // numberOfLives == 0 -> infinite respawns
        if (remainingLives > 0 || game.currentRound.numberOfLives == 0) {

            if (game.currentRound.numberOfLives != 0) {
                REMAINING_LIVES.put(player.getName(), remainingLives);
            }

            // Player will respawn - set up respawn info if not instant
            int respawnDelay = Games.CURRENT_GAME.respawnTimeSeconds;
            if (respawnDelay > 0) {
                Instant respawnTime = Instant.now().plusSeconds(respawnDelay);
                RESPAWN_TIMES.put(player.getName(), respawnTime);
                SAVED_INVENTORIES.put(player.getName(), player.getInventory().getContents());
            } else {
                RespawnPlayer(player);
            }

        }

        if (Games.CURRENT_GAME.respawnTimeSeconds > 0) {
            TempSpecOn(player);
        }

    }

    public static void SetHealth(Player player) {

        String ign = player.getName();

        if (TeamManager.isGamePlayer(ign)) {

            double health = Games.CURRENT_GAME.maxHealthHearts * 2;
            Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(health);
            player.setHealth(health);

        }

    }

    public static void reset() {

        for (String ign : RESPAWN_TIMES.keySet()) {

            Player player = Bukkit.getPlayer(ign);
            if (player == null) continue;

            RespawnPlayer(player);

        }

        TEMP_SPECTATE_IGNS.clear();
        SAVED_INVENTORIES.clear();
        REMAINING_LIVES.clear();

    }

    //=========================================================================

    public static void RespawnTicker() {

        for (String ign : RESPAWN_TIMES.keySet()) {

            Player player = Bukkit.getPlayer(ign);
            if (player == null) continue;

            Instant now = Instant.now();
            Instant respawnTime = RESPAWN_TIMES.get(player.getName());

            if (now.isAfter(respawnTime)) {

                // Respawn time has expired, take player to spawn and remove from list
                RespawnPlayer(player);

            } else {

                // Give player a title to show how many seconds are left
                Duration remainingDuration = Duration.between(now, respawnTime);
                long remainingSeconds = remainingDuration.abs().toSeconds() + 1;
                TextComponent titleText = Component.text(TextFormat.smallText("respawning in"), NamedTextColor.RED);
                TextComponent subtitleText = Component.text("\u203A " + remainingSeconds + " \u2039", NamedTextColor.YELLOW);
                Title title = Title.title(titleText, subtitleText, Title.Times.times(Duration.ZERO, Duration.ofMillis(1050), Duration.ZERO));
                player.showTitle(title);

            }

        }

    }

    //=========================================================================

}
