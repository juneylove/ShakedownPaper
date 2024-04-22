package me.juneylove.shakedown.games.rapidodge;

import me.juneylove.shakedown.control.MatchProgress;
import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.mechanics.*;
import me.juneylove.shakedown.mechanics.worlds.StructureWorld;
import me.juneylove.shakedown.mechanics.worlds.WorldSetting;
import me.juneylove.shakedown.ui.LabelBar;
import me.juneylove.shakedown.ui.Sidebar;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class RapidodgeSetting extends GameSetting {

    BarManager barManager;
    RapidodgeScore score;
    PowerupManager powerupManager;
    RapidodgeControl control;

    {
        name = "Rapidodge";

        defaultGameMode = GameMode.ADVENTURE;
        isTeamGame = false;

        spectatorsCanFly = false;
        spectatorsAreInvisible = false;

        moveItemsEnabled = true;

        // ==========

        Round round1 = new Round();
        round1.name = "Round 1";
        round1.preRoundCountdownSeconds = 15;
        round1.roundDurationSeconds = 180;
        round1.postRoundCountdownSeconds = 5;

        round1.teamsPerWorld = 0;
        round1.numberOfLives = 1;

        StructureWorld worldSetting = new StructureWorld("rapidodge1.nbt");
        World world = worldSetting.getWorld();
        List<SpawnRegion.ISpawnRegion> spawnRegions = new ArrayList<>();
        spawnRegions.add(new SpawnRegion().new XZRegion(world, -3, 0, -1, 10));
        worldSetting.spawnSetting = new Spawn().new RandomIndividual(spawnRegions);
        worldSetting.roundBarriers = getSpawnBarriers(world);
        worldSetting.voidPlane = 125;
        worldSetting.spectatorSpawn = new Location(world, -19.5, 153, 9.5);
        round1.worldSettings.add(worldSetting);

        clearBlocks(world); // temp (?)
        world.setGameRule(GameRule.DO_TILE_DROPS, false);

        round1.kitSetting = new KitSettings().new NoKit();

        rounds.add(round1);
        currentRound = round1;

        // ==========

        labelBarFormats.put(LabelBar.Side.CENTER, new LabelBar.Timer(currentRound.roundDurationSeconds));

        sidebarFormat = new Sidebar.Individuals(12);

        // ==========

        barManager = new BarManager(world);
        score = new RapidodgeScore(worldSetting);
        powerupManager = new PowerupManager(world, barManager);
        control = new RapidodgeControl(this, score, barManager, powerupManager, worldSetting);
        score.assignControl(control);

        worldSetting.clearEntities();

    }

    protected void clearBlocks(World world) {

        clearRegion(world, -21, 145, -1, 15, 150, 10);
        clearRegion(world, -11, 145, -15, 0, 150, 20);

    }

    @SuppressWarnings("SameParameterValue")
    private void clearRegion(World world, int x1, int y1, int z1, int x2, int y2, int z2) {

        for (int x=x1; x<x2+1; x++) {
            for (int y=y1; y<y2+1; y++) {
                for (int z=z1; z<z2+1; z++) {
                    world.getBlockAt(x, y, z).setType(Material.AIR);
                }
            }
        }

    }

    // ==========

    @Override
    public boolean shouldLoadLootTables() {
        return false;
    }

    @Override
    public void tickerConstant() {

        control.ticker();

        if (MatchProgress.playIsActive()) {
            barManager.ticker();
            powerupManager.ticker();
        }

    }

    @Override
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        powerupManager.onPlayerInteractEntity(event);
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        powerupManager.onPlayerInteract(event);
    }

    @Override
    public void onPlayerEnterVoid(Player player) {
        player.setLastDamageCause(new EntityDamageEvent(player, EntityDamageEvent.DamageCause.VOID, 100.0));
        player.setHealth(0.0);
    }

    @Override
    public void onPlayerDeath(String lastDamagerIgn, Player target) {
        score.onElimination(target);
    }

    @Override
    public void onGamePause() {
        control.pause();
    }

    @Override
    public void onGameResume() {
        control.resume();
    }

    @Override
    public void onGameStart() {
        control.onGameStart();
    }

    @Override
    public void onGameEnd() {
        control.onGameEnd();
    }

    // ==========

    private List<List<Location>> getSpawnBarriers(World world) {

        List<Location> spawnBarriers = new ArrayList<>();

        for (int y=145; y<148; y++) {

            spawnBarriers.add(new Location(world, -7, y, 3));
            spawnBarriers.add(new Location(world, -7, y, 4));
            spawnBarriers.add(new Location(world, -7, y, 5));
            spawnBarriers.add(new Location(world, -7, y, 6));
            spawnBarriers.add(new Location(world, -4, y, 3));
            spawnBarriers.add(new Location(world, -4, y, 4));
            spawnBarriers.add(new Location(world, -4, y, 5));
            spawnBarriers.add(new Location(world, -4, y, 6));
            spawnBarriers.add(new Location(world, -6, y, 3));
            spawnBarriers.add(new Location(world, -5, y, 3));
            spawnBarriers.add(new Location(world, -6, y, 6));
            spawnBarriers.add(new Location(world, -5, y, 6));

            for (int z=-1; z<11; z++) {
                spawnBarriers.add(new Location(world, 1, y, z));
                spawnBarriers.add(new Location(world, -12, y, z));
            }
            for (int x=-11; x<1; x++) {
                spawnBarriers.add(new Location(world, x, y, -2));
                spawnBarriers.add(new Location(world, x, y, 11));
            }

        }
        return List.of(spawnBarriers);

    }

}
