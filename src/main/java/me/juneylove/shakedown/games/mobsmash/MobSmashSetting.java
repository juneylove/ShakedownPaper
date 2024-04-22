package me.juneylove.shakedown.games.mobsmash;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import io.papermc.paper.event.entity.EntityLoadCrossbowEvent;
import me.juneylove.shakedown.Main;
import me.juneylove.shakedown.control.Controller;
import me.juneylove.shakedown.control.MatchProgress;
import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.mobsmash.kits.DrownedKit;
import me.juneylove.shakedown.games.mobsmash.kits.SilverfishKit;
import me.juneylove.shakedown.games.mobsmash.kits.StrayKit;
import me.juneylove.shakedown.games.mobsmash.kits.WitherSkeletonKit;
import me.juneylove.shakedown.mechanics.*;
import me.juneylove.shakedown.mechanics.worlds.StructureWorld;
import me.juneylove.shakedown.mechanics.worlds.WorldSetting;
import me.juneylove.shakedown.scoring.TeamManager;
import me.juneylove.shakedown.ui.LabelBar;
import me.juneylove.shakedown.ui.Models;
import me.juneylove.shakedown.ui.GUIFormat;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class MobSmashSetting extends GameSetting {

    MobSmashControl control;
    MobSmashScore score;

    {
        name = "Mob Smash";

        defaultGameMode = GameMode.ADVENTURE;
        pvpEnabled = true;
        pvpKillScore = 40;
        pvpDamageScore = 3; // per HP, so 60 for full damage, 100 for kill + all damage

        respawnTimeSeconds = 3;
        moveItemsEnabled = true;
        itemDropsEnabled = true; // not actually allowing items to be dropped, just enabling custom handling

        allowTntPlace = true;

        teammateGlowEnabled = true;

        // Round initialization
        Round round1 = new Round();
        round1.name = "Round 1";
        round1.preRoundCountdownSeconds = 15;
        round1.roundDurationSeconds = 30;
        round1.postRoundCountdownSeconds = 5;

        round1.teamsPerWorld = 0;
        round1.numberOfLives = 0;

        //WorldSettings.WorldSetting worldSetting = new WorldSettings().new StandardWorld("world");
        WorldSetting worldSetting = new StructureWorld("mobsmash1.nbt");
        World world = worldSetting.getWorld();

        List<Location> locations = new ArrayList<>();
        locations.add(new Location(world, -18, 134, -18));
        locations.add(new Location(world, 18, 134, 18));
        worldSetting.spawnSetting = new Spawn().new OneTeamPerLocation(locations);

        List<Location> spawnBarriers1 = new ArrayList<>();
        spawnBarriers1.add(new Location(world, -15, 134, -16));
        spawnBarriers1.add(new Location(world, -15, 135, -16));
        spawnBarriers1.add(new Location(world, -15, 136, -16));
        spawnBarriers1.add(new Location(world, -16, 134, -15));
        spawnBarriers1.add(new Location(world, -16, 135, -15));
        spawnBarriers1.add(new Location(world, -16, 136, -15));

        List<Location> spawnBarriers2 = new ArrayList<>();
        spawnBarriers2.add(new Location(world, 15, 134, 16));
        spawnBarriers2.add(new Location(world, 15, 135, 16));
        spawnBarriers2.add(new Location(world, 15, 136, 16));
        spawnBarriers2.add(new Location(world, 16, 134, 15));
        spawnBarriers2.add(new Location(world, 16, 135, 15));
        spawnBarriers2.add(new Location(world, 16, 136, 15));

        List<List<Location>> spawnBarriers = new ArrayList<>();
        spawnBarriers.add(spawnBarriers1);
        spawnBarriers.add(spawnBarriers2);

        worldSetting.roundBarriers = spawnBarriers;

        worldSetting.invulnerableRegions.add(new InvulnerableRegion(-21, -15, 132, 139, -21, -15));
        worldSetting.invulnerableRegions.add(new InvulnerableRegion( 15,  21, 132, 139,  15,  21));

        worldSetting.capturePoints.add(new CapturePoint(new Location(world, 0, 136, 0)));
        worldSetting.capturePoints.add(new CapturePoint(new Location(world, -12, 130, 12)));
        worldSetting.capturePoints.add(new CapturePoint(new Location(world, 0, 141, 0)));
        worldSetting.capturePoints.add(new CapturePoint(new Location(world, 12, 130, -12)));

        round1.worldSettings.add(worldSetting);

        round1.kitSetting = new MobSmashKitSelect();

        rounds.add(round1);
        currentRound = round1;

        for (int i = 2; i< MobSmashScore.roundsNeededToWin*2; i++) {

            Round round = round1.copy();
            round.name = "Round " + i;
            rounds.add(round);

        }

        // ==========

        control = new MobSmashControl(this, worldSetting);
        score = new MobSmashScore(control);
        control.assignScore(score);

        // ==========

        labelBarFormats.put(LabelBar.Side.LEFT, new LabelBar.DualTeamLifeStatus(LabelBar.OWN_TEAM, LabelBar.OPPOSING_TEAM));
        labelBarFormats.put(LabelBar.Side.CENTER, new LabelBar.Timer(currentRound.roundDurationSeconds, LabelBar.ROUND_NAME));
        labelBarFormats.put(LabelBar.Side.RIGHT, score.new MobSmashScoreLabelBar("1", "2"));

    }

    @Override
    public boolean shouldLoadLootTables() {
        return false;
    }

    @Override
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent event) {

        event.setCancelled(true);

        if (Controller.isPaused()) return;
        if (Respawn.IsTempSpec(event.getPlayer().getName())) return;

        if (MatchProgress.playIsActive()) {
            AbilityManager.triggerAbility(event.getPlayer());
        }

    }

    @Override
    public void onPlayerDropItem(PlayerDropItemEvent event) {

        event.setCancelled(true);

        if (Controller.isPaused()) return;
        if (Respawn.IsTempSpec(event.getPlayer().getName())) return;

        if (MatchProgress.playIsActive()) {
            AbilityManager.triggerUltimate(event.getPlayer());
        }

    }

    @Override
    public void onPlayerInventoryClick(InventoryClickEvent event) {

        if (Objects.requireNonNull(event.getClickedInventory()).getType() == InventoryType.PLAYER
                && event.getSlot() >= 5 && event.getSlot() <= 8) {
            // armor slots are 5, 6, 7, 8 - cancel any armor slot clicks
            event.setCancelled(true);
            return;
        }

        if (MatchProgress.kitSelectionIsActive()
            && event.getClickedInventory() != null
            && event.getClickedInventory().getType() == InventoryType.CHEST) {

            if (currentRound.kitSetting instanceof MobSmashKitSelect mobSmashKitSelect) {
                event.setCancelled(true);
                mobSmashKitSelect.onKitSelect((Player) event.getWhoClicked(), event.getSlot());
            }

        }

    }

    @Override
    public void onPlayerInventoryClose(InventoryCloseEvent event) {
        if (MatchProgress.kitSelectionIsActive()) {
            if (currentRound.kitSetting instanceof MobSmashKitSelect mobSmashKitSelect) {
                mobSmashKitSelect.onCloseKitSelection((Player) event.getPlayer());
            }
        }
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {

        ItemStack itemInHand = event.getItem();

        if (MatchProgress.kitSelectionIsActive()) {

            if (itemInHand != null
                    && itemInHand.getType() == GUIFormat.menuSelectItem
                    && itemInHand.hasItemMeta()
                    && itemInHand.getItemMeta().getCustomModelData() == Models.KIT_SELECT.num) {

                if (currentRound.kitSetting instanceof MobSmashKitSelect mobSmashKitSelect) {
                    mobSmashKitSelect.openKitSelection(event.getPlayer());
                }

            }

        } else if (MatchProgress.playIsActive()) {
            AbilityManager.onUseItem(event);
        }

    }

    @Override
    public void onPlayerEnterVoid(Player player) {
        AbilityManager.onPlayerEnterVoid(player);
    }

    @Override
    public void onPlayerJump(PlayerJumpEvent event) {
        AbilityManager.onJump(event);
    }

    @Override
    public void onProjectileHit(ProjectileHitEvent event) {

        Projectile projectile = event.getEntity();

        if (projectile.getType() == EntityType.SMALL_FIREBALL) {

            if (projectile.hasMetadata("mobSmashKit") && projectile.hasMetadata("source")) {
                if (projectile.getMetadata("mobSmashKit").get(0).asString().equals("blaze")) {

                    if (event.getHitEntity() != null) {

                        if (event.getHitEntity() instanceof Player hitPlayer) {

                            // cancel for same team
                            String shooterIgn = projectile.getMetadata("source").get(0).asString();
                            if (TeamManager.sameTeam(hitPlayer.getName(), shooterIgn)) {
                                event.setCancelled(true);
                            }

                        } else if (event.getHitEntity().hasMetadata("source")) {

                            // cancel if entity is owned by same team
                            String shooterIgn = projectile.getMetadata("source").get(0).asString();
                            String entitySource = event.getHitEntity().getMetadata("source").get(0).asString();
                            if (TeamManager.sameTeam(entitySource, shooterIgn)) {
                                event.setCancelled(true);
                            }

                        }

                    }

                }
            }

        } else if (projectile.getType() == EntityType.SNOWBALL) {

            if (projectile.hasMetadata("mobSmashKit") && projectile.hasMetadata("source")) {
                if (projectile.getMetadata("mobSmashKit").get(0).asString().equals("silverfish")) {

                    for (int i = 0; i < SilverfishKit.bombSilverfish; i++) {

                        Silverfish silverfish = (Silverfish) projectile.getLocation().getWorld().spawnEntity(projectile.getLocation(), EntityType.SILVERFISH);
                        String shooterIgn = projectile.getMetadata("source").get(0).asString();
                        silverfish.setMetadata("source", new FixedMetadataValue(Main.getInstance(), shooterIgn));

                    }

                }
            }

        } else if (projectile.getType() == EntityType.WITHER_SKULL) {

            if (projectile.hasMetadata("mobSmashKit") && projectile.hasMetadata("source")) {
                if (projectile.getMetadata("mobSmashKit").get(0).asString().equals("witherskeleton")) {

                    String ign = projectile.getMetadata("source").get(0).asString();

                    // knock back nearby enemies
                    Collection<LivingEntity> nearbyEntities = projectile.getLocation().getNearbyLivingEntities(WitherSkeletonKit.ultimateKnockbackRadius);
                    for (LivingEntity nearbyEntity : nearbyEntities) {

                        if (nearbyEntity instanceof Player player
                                && (!TeamManager.isGamePlayer(player.getName())
                                || TeamManager.sameTeam(ign, nearbyEntity.getName()))) continue; // ignore spectators and teammates

                        Location loc = nearbyEntity.getEyeLocation();
                        Vector direction = loc.toVector().subtract(projectile.getLocation().toVector());

                        double distance = projectile.getLocation().distance(loc);
                        double multiplier = WitherSkeletonKit.ultimateMaxKnockbackMultiplier * Math.max(0, WitherSkeletonKit.ultimateKnockbackRadius - distance);

                        nearbyEntity.setVelocity(nearbyEntity.getVelocity().add(direction.multiply(multiplier)));
                        nearbyEntity.removeMetadata("lastDamager", Main.getInstance());
                        nearbyEntity.setMetadata("lastDamager", new FixedMetadataValue(Main.getInstance(), ign + "'s Blue Skull"));

                    }

                }
            }

        } else if (projectile.getType() == EntityType.TRIDENT) {

            Trident trident = (Trident) projectile;
            if (!(trident.getShooter() instanceof Player)) return;

            Block hitBlock = event.getHitBlock();
            if (hitBlock == null) return;

            // hit a block - check type
            if (hitBlock.getType() == Material.LIGHTNING_ROD) {

                Player thrower = (Player) trident.getShooter();
                if (AbilityManager.getKitType(thrower.getName()) != Kits.DROWNED) return;

                if (AbilityManager.getUltRemainingTicks(thrower.getName()) > 0
                    && AbilityManager.isDrownedLightningRod(thrower.getName(), hitBlock)) { // ensure lightning rod corresponds to this player

                    // drowned ult is active - lightning strike
                    LightningStrike lightning = (LightningStrike) trident.getWorld().spawnEntity(trident.getLocation(), EntityType.LIGHTNING);
                    lightning.setFlashCount(1);
                    lightning.setMetadata("mobSmashKit", new FixedMetadataValue(Main.getInstance(), "drowned"));
                    lightning.setMetadata("source", new FixedMetadataValue(Main.getInstance(), thrower.getName()));

                    hitBlock.setBlockData(Bukkit.createBlockData(Material.AIR)); // destroys lighting rod

                    AbilityManager.resetUltRemainingTicks(thrower.getName()); // end ult after lightning strike

                    trident.getWorld().playSound(DrownedKit.ULTIMATE_STRIKE_SOUND, trident);

                }

            }


        }

    }

    @Override
    public void onProjectileLaunch(ProjectileLaunchEvent event) {

        if (Controller.isPaused()) {
            event.setCancelled(true);
            return;
        }

        // don't allow players to shoot from spawn while invulnerable - may need to change later, unsure
        if (event.getEntity().getShooter() instanceof Player shooter) {

            if (InvulnerableRegion.isSpawnInvulnerable(shooter)) {
                event.setCancelled(true);
                return;
            }

            if (!MatchProgress.playIsActive()) {
                event.setCancelled(true);
                return;
            }

            Kits shooterKitType = AbilityManager.getKitType(shooter.getName());
            if (shooterKitType.kit instanceof StrayKit strayKit && event.getEntity() instanceof Arrow arrow) {

                arrow.setMetadata("source", new FixedMetadataValue(Main.getInstance(), shooter.getName()));
                arrow.setMetadata("mobSmashKit", new FixedMetadataValue(Main.getInstance(), "stray"));

                if (strayKit.abilityDurationRemainingTicks > 0) {
                    arrow.addCustomEffect(new PotionEffect(PotionEffectType.SLOW, (int) (StrayKit.slowArrowEffectDuration * 20), 0), true);
                }

            }

            AbilityManager.onProjectileLaunch(event);

        }

    }

    @Override
    public void onPlayerLoadCrossbow(EntityLoadCrossbowEvent event) {

        if (Controller.isPaused()) {
            event.setCancelled(true);
            return;
        }

        AbilityManager.onPlayerLoadCrossbow(event);

    }

    @Override
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        AbilityManager.onEntityDamageByEntity(event);
    }

    @Override
    public void onPlayerDeath(String lastDamagerIgn, Player target) {
        AbilityManager.onPlayerDeath(lastDamagerIgn, target);
    }

    @Override
    public void tickerConstant() {
        control.ticker();
        AbilityManager.abilityTicker();
        CapturePointManager.capturePointTicker();
    }

    @Override
    public void ticker1Second() {
        score.scoreTicker();
    }

    @Override
    public void onPlayerRespawn(Player player) {
        AbilityManager.onRespawn(player);
    }

    @Override
    public void onGameStart() {
        control.onGameStart();
    }

    @Override
    public void onGameEnd() {
        control.onGameEnd();
    }

    @Override
    public void onGamePause() {
        control.pause();
    }

    @Override
    public void onGameResume() {
        control.resume();
    }

}
