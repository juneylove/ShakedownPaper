package me.juneylove.shakedown.games.chorusvolley;

import me.juneylove.shakedown.Main;
import me.juneylove.shakedown.control.Controller;
import me.juneylove.shakedown.control.MatchProgress;
import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.mechanics.*;
import me.juneylove.shakedown.mechanics.worlds.StructureWorld;
import me.juneylove.shakedown.mechanics.worlds.WorldSetting;
import me.juneylove.shakedown.ui.GUIFormat;
import me.juneylove.shakedown.ui.LabelBar;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ChorusVolleySetting extends GameSetting {

    List<Player> playersWhoFishedThisTick = new ArrayList<>();

    BallHandler ballHandler;
    ChorusVolleyControl control;
    ChorusVolleyScore score;

    {

        name = "Chorus Volley";
        defaultGameMode = GameMode.ADVENTURE;
        respawnTimeSeconds = 3;
        moveItemsEnabled = true;

        // Round initialization
        Round round1 = new Round();
        round1.name = "Round 1";
        round1.preRoundCountdownSeconds = 5;
        round1.roundDurationSeconds = 120;
        round1.postRoundCountdownSeconds = 5;

        round1.teamsPerWorld = 0;
        round1.numberOfLives = 0;

        StructureWorld worldSetting = new StructureWorld("chorusvolley1.nbt");
        World world = worldSetting.getWorld();

        List<Location> barriers1 = new ArrayList<>();
        List<Location> barriers2 = new ArrayList<>();
        for (int y=129; y <=132; y++) {
            for (int z=-3; z<=3; z++) {
                barriers1.add(new Location(world, -19, y, z));
                barriers2.add(new Location(world, 19, y, z));
            }
        }
        worldSetting.roundBarriers = List.of(barriers1, barriers2);

        List<Location> spawns1 = new ArrayList<>();
        spawns1.add(new Location(world, -21, 129, -2, -90f, 0f));
        spawns1.add(new Location(world, -21, 129, -1, -90f, 0f));
        spawns1.add(new Location(world, -21, 129,  0, -90f, 0f));
        spawns1.add(new Location(world, -21, 129,  1, -90f, 0f));
        spawns1.add(new Location(world, -21, 129,  2, -90f, 0f));
        List<Location> spawns2 = new ArrayList<>();
        spawns2.add(new Location(world, 21, 129,  2, 90f, 0f));
        spawns2.add(new Location(world, 21, 129,  1, 90f, 0f));
        spawns2.add(new Location(world, 21, 129,  0, 90f, 0f));
        spawns2.add(new Location(world, 21, 129, -1, 90f, 0f));
        spawns2.add(new Location(world, 21, 129, -2, 90f, 0f));

        worldSetting.spawnSetting = new Spawn().new OneListPerTeam(List.of(spawns1, spawns2));

        round1.worldSettings.add(worldSetting);

        ItemStack[] kit = new ItemStack[]{GUIFormat.unbreakable(Material.BOW)};
        round1.kitSetting = new KitSettings().new Uniform(kit);

        rounds.add(round1);
        currentRound = round1;

        labelBarFormats.put(LabelBar.Side.CENTER, new LabelBar.Timer(currentRound.roundDurationSeconds, LabelBar.ROUND_NAME));

        worldSetting.clearEntities();

        // ==========

        ballHandler = new BallHandler();
        control = new ChorusVolleyControl(this, worldSetting);
        score = new ChorusVolleyScore(worldSetting, control);
        ballHandler.assignScore(score);
        control.assignClasses(score, ballHandler);
        PowerupPadManager.assignWorld(world);

    }

    @Override
    public void onGameStart() {
        control.startGame();
    }

    @Override
    public void onGameEnd() {
        control.endGame();
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {

        if (Controller.isPaused()) return;

        if (event.getAction().isLeftClick()) {

            // casting a fishing rod counts as a left click for some reason
            // BUT the fishing event is fired first, so we can keep track of whether it should be a fishing event
            // if so, cancel the left click event
            if (playersWhoFishedThisTick.contains(event.getPlayer())) return;

            event.setCancelled(true);
            ballHandler.onPlayerPunch(event.getPlayer());

        }

    }

    @Override
    public void onProjectileHit(ProjectileHitEvent event) {

        if (event.getHitEntity() instanceof ShulkerBullet) {
            event.setCancelled(true);
            return;
        }

        if (event.getEntity() instanceof Arrow arrow) {
            arrow.remove();
            return;
        }

        if (event.getEntity() instanceof ShulkerBullet && event.getHitEntity() instanceof Player) {
            event.setCancelled(true);
        }

    }

    @Override
    public void onEntityDamage(EntityDamageEvent event) {

        if (event.getEntity() instanceof ShulkerBullet) {
            event.setCancelled(true);
        }

    }

    @Override
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

        if (event.getEntity() instanceof ShulkerBullet) {
            event.setCancelled(true);

            if (event.getDamager() instanceof Player player) {
                ballHandler.onPlayerPunch(player);
            }

        }

    }

    @Override
    public void onProjectileLaunch(ProjectileLaunchEvent event) {

        if (event.getEntity() instanceof Arrow arrow && arrow.getShooter() instanceof Player) {
            new ArrowRunnable(arrow, ballHandler).runTaskTimer(Main.getInstance(), 0, 1);
        }

    }

    @Override
    public void onPlayerFish(PlayerFishEvent event) {

        playersWhoFishedThisTick.add(event.getPlayer());

        if (event.getState() == PlayerFishEvent.State.FISHING) {
            new FishHookRunnable(event.getHook(), ballHandler).runTaskTimer(Main.getInstance(), 0, 1);
        }

    }

    @Override
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!MatchProgress.playIsActive()) return;
        if (Controller.isPaused()) return;
        PowerupPadManager.onPlayerMove(event);
    }

    @Override
    public void tickerConstant() {
        ballHandler.ballTicker();
        PowerupPadManager.ticker();
        control.ticker();
        playersWhoFishedThisTick.clear();
    }

    @Override
    public void onGamePause() {
        control.pause();
        ballHandler.pauseBall();
    }

    @Override
    public void onGameResume() {
        control.resume();
        ballHandler.resumeBall();
    }

}
