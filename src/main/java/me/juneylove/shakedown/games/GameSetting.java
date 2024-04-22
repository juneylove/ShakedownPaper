package me.juneylove.shakedown.games;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import io.papermc.paper.event.entity.EntityLoadCrossbowEvent;
import me.juneylove.shakedown.mechanics.KitSettings;
import me.juneylove.shakedown.mechanics.MovementDetectRegion;
import me.juneylove.shakedown.mechanics.worlds.WorldSetting;
import me.juneylove.shakedown.ui.LabelBar;
import me.juneylove.shakedown.ui.Sidebar;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class GameSetting {

    // Game name
    public String name = "Unknown";

    // Game data
    public List<Round>  rounds = new ArrayList<>();
    public int          currentRoundNum = 0;
    public Round        currentRound = null;
    public GameMode     defaultGameMode = GameMode.SURVIVAL;
    public boolean      isTeamGame = true;

    // PVP Settings
    public boolean pvpEnabled = false;
    public boolean friendlyFireEnabled = false;
    public boolean pvpDingEnabled = true;
    public boolean deathFireworkEnabled = true;
    public int     pvpKillScore = 0;          // in ADDITION to damage score below
    public int     predeterminedDamageHp = 0; // If set to 0, leaves damage unchanged from vanilla
    public int     pvpDamageScore = 0;        // if above value is 0, this is per HP. if above value is nonzero, this is per hit.
    public boolean infiniteArrows = false;

    // Respawn/health settings
    public boolean keepInventoryOnDeath = true;
    public boolean keepInventoryOnFinalDeath = true; // Ignored if numberOfLives = 0 (infinite)
    public int     respawnTimeSeconds = 5;
    public int     maxHealthHearts = 10;
    public boolean hungerEnabled = false;            // If false, saturation is permanently set to 0
    public boolean spectatorsCanFly = true;
    public boolean spectatorsAreInvisible = true;

    // World settings
    public boolean blockDamageEnabled = false;     // Cactus, dripstone, etc.
    public boolean fallDamageEnabled = false;
    public boolean itemDropsEnabled = false;       // Ability for player to drop items
    public boolean moveItemsEnabled = false;       // Ability to move items around in inventory
    public boolean entityInteractsEnabled = false; // Breeding animals, riding horses, etc.
    public boolean chestInteractsEnabled = false;  // Opening chests
    public boolean mobDropsEnabled = false;

    // TNT settings
    public boolean immediateTntPrime = true;
    public int     tntFuseTicks = 60;
    public boolean explosionsDestroyBlocks = false;

    // Block place/break settings
    public boolean allowFireExtinguish = true;
    public boolean allowTntPlace = false;        // Also controls breaking of tnt (assuming it's not ignited immediately)
    public boolean allowConcretePlace = false;   // Also controls breaking of concrete
    public boolean allowCobwebPlace = false;     // Also controls breaking of cobwebs
    public boolean allowOtherBlockPlace = false; // Also controls breaking of other blocks
    public boolean concreteInstabreak = false;
    public boolean infiniteConcrete = false;     // If true, disables block drops for concrete (overrides next line)
    public boolean blockDropsEnabled = false;    // Whether blocks drop when mined

    // UI/scoring settings
    public boolean constantScoreboardRefresh = false; // Once per second. If false, will update for each world when score changes in that world
    public boolean distributeScoresEvenly = false;    // Distributes at end of game
    public boolean teammateGlowEnabled = false;
    public HashMap<LabelBar.Side, LabelBar.Format> labelBarFormats = new HashMap<>();
    public Sidebar.Format sidebarFormat = new Sidebar.None();

    //=========================================================================

    public boolean shouldLoadLootTables() {

        for (Round round : rounds) {
            for (WorldSetting worldSetting : round.worldSettings) {
                if (worldSetting.lootChestLocations.size() > 0) return true;
            }
        }
        return false;

    }

    //=========================================================================

    public void onGameStart() {}

    public void onGameEnd() {}

    public void onPlayerDamage(String damagerIgn, Player target) {}

    public void onPlayerDeath(String lastDamagerIgn, Player target) {}

    public void onPlayerRespawn(Player player) {}

    public void onBlockPlace(Player player, Block block) {}

    public void onBlockBreak(Player player, Block block) {}

    public void onPlayerDropItem(PlayerDropItemEvent event) {}

    public void onPlayerInventoryClick(InventoryClickEvent event) {}

    public void onPlayerInventoryClose(InventoryCloseEvent event) {}

    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {}

    public void onPlayerInteract(PlayerInteractEvent event) {}

    public void onPlayerEnterRegion(Player player, MovementDetectRegion region) {}

    public void onPlayerMove(PlayerMoveEvent event) {}

    public void onPlayerEnterVoid(Player player) {}

    public void onPlayerJump(PlayerJumpEvent event) {}

    public void onProjectileHit(ProjectileHitEvent event) {}

    public void onProjectileLaunch(ProjectileLaunchEvent event) {}

    public void onPlayerFish(PlayerFishEvent event) {}

    public void onPlayerLoadCrossbow(EntityLoadCrossbowEvent event) {}

    public void onEntityDamage(EntityDamageEvent event) {}

    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {}

    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {}

    public void tickerConstant() {}

    public void ticker1Second() {}

    public void ticker5Second() {}

    public void ticker15Second() {}

    public void onGamePause() {}

    public void onGameResume() {}

    //=========================================================================

    @SuppressWarnings("InnerClassMayBeStatic")
    public class Round {

        public String name;                       // Defaults to "Round " + round number
        public List<WorldSetting> worldSettings = new ArrayList<>(); // Throws an error if none are specified for first round, but can copy from previous rounds if any were specified there
        public int teamsPerWorld;                 // TODO: verify stuff like this on load

        public KitSettings.KitSetting kitSetting;
        public int numberOfLives;

        public int preRoundCountdownSeconds;      // After world change
        public int roundDurationSeconds;          // Between countdowns
        public int postRoundCountdownSeconds;     // Before next world change

        public Round copy() {

            Round round = new Round();
            round.name = this.name;
            round.worldSettings = this.worldSettings;
            round.teamsPerWorld = this.teamsPerWorld;
            round.kitSetting = this.kitSetting;
            round.numberOfLives = this.numberOfLives;
            round.preRoundCountdownSeconds = this.preRoundCountdownSeconds;
            round.roundDurationSeconds = this.roundDurationSeconds;
            round.postRoundCountdownSeconds = this.postRoundCountdownSeconds;

            this.kitSetting.copiedToNextRound = true;
            return round;

        }

    }

    //=========================================================================

}
