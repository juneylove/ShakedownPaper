package me.juneylove.shakedown;

import me.juneylove.shakedown.control.SdCommand;
import me.juneylove.shakedown.control.SdCommandTabCompleter;
import me.juneylove.shakedown.data.LoadTeams;
import me.juneylove.shakedown.data.SDMongo;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.listeners.PlayerJoinAttacher;
import me.juneylove.shakedown.listeners.PlayerQuitDetacher;
import me.juneylove.shakedown.listeners.eventlisteners.*;
import me.juneylove.shakedown.mechanics.worlds.WorldSettings;
import me.juneylove.shakedown.scoring.PlacementManager;
import me.juneylove.shakedown.ui.LabelBarManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class Main extends JavaPlugin {

    public static final int NUM_OF_GAMES = 3;
    public static boolean GAME_IN_PROGRESS = false;

    //=========================================================================

    private static Main instance;

    public static Main getInstance() {
        return instance;
    }

    //=========================================================================

    @Override
    public void onEnable() {

        getLogger().info("Enabling ShakedownPaper");
        instance = this;

        saveResource("TeamList.yml", true);
        saveResource("lootTables/level1.yml", true);
        saveResource("lootTables/level2.yml", true);
        saveResource("lootTables/level3.yml", true);
        saveResource("lootTables/level4.yml", true);

        if (Games.COMPLETED_GAMES.size() > 0) {
            Games.COMPLETED_GAMES.clear();
        }
        WorldSettings.deregisterAllStructures();
        LoadTeams.LoadFromFile();
        Games.LoadGameOptions();
        PlacementManager.initializePlacements();
        PlacementManager.recalculateAll();

        LabelBarManager.deleteAll();

        Objects.requireNonNull(this.getCommand("sd")).setExecutor(new SdCommand());
        Objects.requireNonNull(this.getCommand("sd")).setTabCompleter(new SdCommandTabCompleter());

        getServer().getPluginManager().registerEvents(new BlockCanBuildListener(), this);
        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new BlockDamageListener(), this);
        getServer().getPluginManager().registerEvents(new BlockFromToListener(), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(), this);
        getServer().getPluginManager().registerEvents(new CreatureSpawnListener(), this);
        getServer().getPluginManager().registerEvents(new DamageListener(), this);
        getServer().getPluginManager().registerEvents(new EntityChangeBlockListener(), this);
        getServer().getPluginManager().registerEvents(new EntityDamageByBlockListener(), this);
        getServer().getPluginManager().registerEvents(new EntityDamageByEntityListener(), this);
        getServer().getPluginManager().registerEvents(new EntityDeathListener(), this);
        getServer().getPluginManager().registerEvents(new EntityExplodeListener(), this);
        getServer().getPluginManager().registerEvents(new EntityLoadCrossbowListener(), this);
        getServer().getPluginManager().registerEvents(new EntityPickupItemListener(), this);
        getServer().getPluginManager().registerEvents(new EntityTargetLivingEntityListener(), this);
        getServer().getPluginManager().registerEvents(new FoodLevelChangeListener(), this);
        getServer().getPluginManager().registerEvents(new EntityRegainHealthListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryCloseListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryOpenListener(), this);
        getServer().getPluginManager().registerEvents(new ItemDropListener(), this);
        getServer().getPluginManager().registerEvents(new JumpListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerFishListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractEntityListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerPickupArrowListener(), this);
        getServer().getPluginManager().registerEvents(new ProjectileHitListener(), this);
        getServer().getPluginManager().registerEvents(new ProjectileLaunchListener(), this);
        getServer().getPluginManager().registerEvents(new StructureGrowListener(), this);
        getServer().getPluginManager().registerEvents(new SwapHandsListener(), this);

        getServer().getPluginManager().registerEvents(new PlayerJoinAttacher(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitDetacher(), this);

        //=========================================================================

        //SDMongo.connect();

    }

    @Override
    public void onDisable() {}

    //=========================================================================

}





// THINGS TO REMEMBER/TO DO LIST
//
// - if the idea of saplings for custom chests pans out, need to make sure
//   players can't obtain bone meal OR cancel BlockFertilizeEvents as well!