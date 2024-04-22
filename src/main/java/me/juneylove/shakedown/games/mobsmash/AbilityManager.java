package me.juneylove.shakedown.games.mobsmash;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import io.papermc.paper.event.entity.EntityLoadCrossbowEvent;
import me.juneylove.shakedown.Main;
import me.juneylove.shakedown.control.MatchProgress;
import me.juneylove.shakedown.games.mobsmash.kits.*;
import me.juneylove.shakedown.mechanics.Respawn;
import me.juneylove.shakedown.scoring.TeamManager;
import me.juneylove.shakedown.ui.GUIFormat;
import me.juneylove.shakedown.ui.Models;
import me.juneylove.shakedown.ui.ProgressBar;
import me.juneylove.shakedown.ui.TextFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;

public class AbilityManager {

    private static final String progressBarActive = "\uE045" + TextFormat.NEGATIVE_ONE_PX;
    private static final String progressBarReady  = "\uE046" + TextFormat.NEGATIVE_ONE_PX;
    private static final int progressBarWidth   = 35;
    private static final int halfProgressBarSpacing = 25;

    private static final TextComponent rightClickToReload = TextFormat.negativeSpace(58).append(Component.text("\uE070")).append(TextFormat.negativeSpace(59));
    private static final TextComponent reloading = TextFormat.negativeSpace(29).append(Component.text("\uE071")).append(TextFormat.negativeSpace(31));

    private static Instant nextGameProgressAddUltPoints;
    private static final int gameProgressAddUltPointsSeconds = 5; // temp value
    private static final int gameProgressUltPointsAmount = 5; // temp value
    private static final int ultPointsPerKill = 5; // temp value

    public static HashMap<String, MobKit> kits = new HashMap<>();

    public static void triggerAbility(Player player) {

        MobKit kit = kits.get(player.getName());
        if (kit.abilityCooldownRemainingTicks > 0) return;
        if (kit.abilityDurationRemainingTicks > 0) return;
        if (kit instanceof PolarBearKit polarBearKit && polarBearKit.entityToHeal() == null) return;

        kit.abilityDurationRemainingTicks = (int) (kit.abilityDurationSeconds * 20);

        kit.onAbilityStart();
    }

    public static void endAbility(Player player) {

        MobKit kit = kits.get(player.getName());
        kit.abilityCooldownRemainingTicks = (int) (kit.abilityCooldownSeconds * 20);
        kit.onAbilityEnd();

    }

    public static void triggerUltimate(Player player) {

        MobKit kit = kits.get(player.getName());
        if (kit.currentUltPoints < kit.ultPointsRequired) return;
        if (kit.ultimateDurationRemainingTicks > 0) return;
        if (kit instanceof EvokerKit && Respawn.TempSpecCount(TeamManager.getTeam(kit.ign)) == 0) return;

        kit.currentUltPoints = 0;
        kit.ultimateDurationRemainingTicks = (int) (kit.ultimateDurationSeconds * 20);

        kit.onUltimateStart();
    }

    public static void onRespawn(Player player) {

        //noinspection ConstantConditions
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(kits.get(player.getName()).maxHealth);
        player.setHealth(kits.get(player.getName()).maxHealth);

        player.clearActivePotionEffects();
        player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, PotionEffect.INFINITE_DURATION, 0, true, false, false));

        kits.get(player.getName()).applyKit();

    }

    public static void addUltPoints(Player player, int points) {
        if (kits.get(player.getName()) == null) return;
        addUltPoints(kits.get(player.getName()), points);
    }

    public static void addUltPoints(MobKit kit, int points) {
        kit.currentUltPoints = Math.min(kit.ultPointsRequired, kit.currentUltPoints + points);
    }

    public static void abilityTicker() {

        // Give ult points to all players every couple seconds
        if (MatchProgress.playIsActive()) {
            if (nextGameProgressAddUltPoints != null && Instant.now().isAfter(nextGameProgressAddUltPoints)) {

                nextGameProgressAddUltPoints = Instant.now().plusSeconds(gameProgressAddUltPointsSeconds);

                for (MobKit kit : kits.values()) {
                    addUltPoints(kit, gameProgressUltPointsAmount);
                }

            }
        }

        for (MobKit kit : kits.values()) {

            kit.passiveRun();

            Player player = Bukkit.getPlayer(kit.ign);
            if (player == null) continue;

            // Check ability cooldowns
            if (kit.abilityCooldownRemainingTicks > 0) kit.abilityCooldownRemainingTicks--;

            // Check active ability durations
            if (kit.abilityDurationRemainingTicks > 0) {
                kit.abilityRun();
                if (kit.abilityDurationRemainingTicks == 1) endAbility(player);
                kit.abilityDurationRemainingTicks--;
            }

            // Check active ultimate durations
            if (kit.ultimateDurationRemainingTicks > 0) {
                kit.ultimateRun();
                if (kit.ultimateDurationRemainingTicks == 1) kit.onUltimateEnd();
                kit.ultimateDurationRemainingTicks--;
            }

            // Update progress bars
            if (MatchProgress.playIsActive()) {

                TextComponent reloadIndicator = Component.empty();
                if (kit instanceof BowKit bowKit) {

                    ItemStack mainHand = player.getInventory().getItemInMainHand();
                    ItemStack offHand = player.getInventory().getItemInOffHand();

                    if (player.getCooldown(Material.BOW) > 0 || player.getCooldown(Material.CROSSBOW) > 0) {

                        // reload in progress
                        reloadIndicator = reloading;

                    } else if (bowKit.reloadNeeded) {

                        if (mainHand.getType() == Material.BOW || mainHand.getType() == Material.CROSSBOW || GUIFormat.isMenuIcon(mainHand, Models.ARROW0)) {
                            reloadIndicator = rightClickToReload;
                        } else if (offHand.getType() == Material.BOW || offHand.getType() == Material.CROSSBOW || GUIFormat.isMenuIcon(offHand, Models.ARROW0)) {
                            reloadIndicator = rightClickToReload;
                        }

                    } else if ((mainHand.getType() == Material.ARROW || offHand.getType() == Material.ARROW)
                            && bowKit.getTotalArrows(player) < bowKit.maxArrowAmount) {
                        reloadIndicator = rightClickToReload;
                    }

                }

                TextComponent abilityBar;
                if (kit.abilityCooldownRemainingTicks > 0) {
                    // Ability is recharging
                    int max = (int) (kit.abilityCooldownSeconds * 20);
                    int current = max - kit.abilityCooldownRemainingTicks;
                    abilityBar = ProgressBar.progressBar(current / ((double) max), progressBarWidth, NamedTextColor.GRAY);
                } else if (kit.abilityDurationRemainingTicks > 0) {
                    // Ability is in use
                    int max = (int) (kit.abilityDurationSeconds * 20);
                    int current = kit.abilityDurationRemainingTicks;
                    abilityBar = ProgressBar.progressBar(current / ((double) max), progressBarWidth, MobKit.abilityColor)
                            .append(TextFormat.negativeSpace(progressBarWidth+1))
                            .append(Component.text(progressBarActive));
                } else {

                    // Ability is ready - append "ready" indicator unless kit is Polar Bear with no teammate in line of sight
                    // also adjust color accordingly
                    if (kit instanceof PolarBearKit polarBearKit && polarBearKit.entityToHeal() == null) {

                        abilityBar = ProgressBar.progressBar(1.0, progressBarWidth, NamedTextColor.GRAY);

                    } else {

                        abilityBar = ProgressBar.progressBar(1.0, progressBarWidth, MobKit.abilityColor)
                                .append(TextFormat.negativeSpace(progressBarWidth + 1))
                                .append(Component.text(progressBarReady));

                    }

                }

                TextComponent ultimateBar;
                if (kit.ultimateDurationRemainingTicks > 0) {
                    // Ultimate is in use
                    int max = (int) (kit.ultimateDurationSeconds * 20);
                    int current = kit.ultimateDurationRemainingTicks;
                    ultimateBar = ProgressBar.progressBar(current / ((double) max), progressBarWidth, MobKit.ultimateColor)
                            .append(TextFormat.negativeSpace(progressBarWidth+1))
                            .append(Component.text(progressBarActive));
                } else if (kit.currentUltPoints < kit.ultPointsRequired) {
                    // Ultimate is charging
                    int max = kit.ultPointsRequired;
                    int current = kit.currentUltPoints;
                    ultimateBar = ProgressBar.progressBar(current / ((double) max), progressBarWidth, NamedTextColor.GRAY);
                } else {

                    // Ultimate is ready - append "ready" indicator unless kit is Evoker kit with no dead teammates
                    // also adjust color accordingly
                    if (kit instanceof EvokerKit && Respawn.TempSpecCount(TeamManager.getTeam(kit.ign)) == 0) {

                        ultimateBar = ProgressBar.progressBar(1.0, progressBarWidth, NamedTextColor.GRAY);

                    } else {

                        ultimateBar = ProgressBar.progressBar(1.0, progressBarWidth, MobKit.ultimateColor)
                                .append(TextFormat.negativeSpace(progressBarWidth + 1))
                                .append(Component.text(progressBarReady));

                    }

                }

                TextComponent actionBarText = abilityBar
                        .append(TextFormat.padSpaces(halfProgressBarSpacing))
                        .append(reloadIndicator)
                        .append(TextFormat.padSpaces(halfProgressBarSpacing))
                        .append(ultimateBar);
                player.sendActionBar(actionBarText);

            }

        }

    }

    public static void onRoundStart() {

        nextGameProgressAddUltPoints = Instant.now().plusSeconds(gameProgressAddUltPointsSeconds);

    }

    public static void onRoundFinish() {

        for (MobKit kit : kits.values()) {

            if (kit.abilityDurationRemainingTicks > 0) {
                kit.onAbilityEnd();
            }
            if (kit.ultimateDurationRemainingTicks > 0) {
                kit.onUltimateEnd();
            }

            kit.abilityDurationRemainingTicks = 0;
            kit.abilityCooldownRemainingTicks = 0;

            kit.ultimateDurationRemainingTicks = 0;
            kit.currentUltPoints = 0;

            Player player = Bukkit.getPlayer(kit.ign);
            if (player == null) continue;

            player.clearActivePotionEffects();
            player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, PotionEffect.INFINITE_DURATION, 0, true, false, false));
            player.sendActionBar(Component.empty());
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
            player.setHealth(20.0);

        }

    }

    public static void onEndKitSelect() {

        for (MobKit kit : kits.values()) {

            Player player = Bukkit.getPlayer(kit.ign);
            if (player == null) continue;

            //noinspection ConstantConditions
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(kit.maxHealth);
            player.setHealth(kit.maxHealth);

            kit.applyKit();

        }

    }

    public static void onUseItem(PlayerInteractEvent event) {

        MobKit kit = kits.get(event.getPlayer().getName());
        if (kit != null) kit.onUseItem(event);

    }

    public static void onJump(PlayerJumpEvent event) {

        MobKit kit = kits.get(event.getPlayer().getName());
        if (kit == null) return;

        if (event.getPlayer().hasMetadata("frozenBy")) {
            event.setCancelled(true);
        } else {
            kit.onJump(event);
        }



    }

    public static void onPlayerDeath(String lastDamagerIgn, Player target) {

        if (Respawn.IsTempSpec(target.getName())) {
            target.sendActionBar(Component.empty());
        }

        MobKit killerKit = kits.get(lastDamagerIgn);
        if (killerKit == null) return;

        addUltPoints(killerKit, ultPointsPerKill);

        MobKit killedKit = kits.get(target.getName());
        if (killedKit == null) return;
        killedKit.onDeath();

        if (killerKit instanceof ZombieKit zombieKit) {
            if (zombieKit.ultimateDurationRemainingTicks > 0) {

                // spawn zombie at target's location
                // TODO: TEST
                Zombie zombie = (Zombie) target.getWorld().spawnEntity(target.getLocation(), EntityType.ZOMBIE);
                zombie.setMetadata("source", new FixedMetadataValue(Main.getInstance(), lastDamagerIgn));
                zombie.setMetadata("mobSmashKit", new FixedMetadataValue(Main.getInstance(), "zombie"));
                zombie.setShouldBurnInDay(false);

            }
        } else if (killerKit instanceof AxolotlKit axolotlKit) {
            if (axolotlKit.ultimateDurationRemainingTicks > 0) {

                // give regen to axolotl kit and nearby teammates
                // TODO: TEST
                Player player = Bukkit.getPlayer(axolotlKit.ign);
                if (player == null) return;

                Collection<Player> nearbyPlayers = player.getLocation().getNearbyPlayers(AxolotlKit.regenRadius);
                for (Player nearbyPlayer : nearbyPlayers) {

                    if (!TeamManager.isGamePlayer(nearbyPlayer.getName()) || !TeamManager.sameTeam(axolotlKit.ign, nearbyPlayer.getName())) continue;

                    nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, (int) (AxolotlKit.regenDuration*20), 1));

                }

            }
        }

    }

    public static void onEntityDamageByEntity(EntityDamageByEntityEvent event) {

        if (event.getEntity() instanceof Player target) {

            if (!TeamManager.isGamePlayer(target.getName()) || Respawn.IsTempSpec(target.getName())) {
                event.setCancelled(true);
            }

            MobKit targetKit = kits.get(target.getName());
            if (targetKit != null) targetKit.onDamageByEntity(event);

            if (!event.isCancelled()) {

                if (event.getDamager() instanceof Arrow arrow) {

                    handleArrowHit(event, target, arrow);

                } else if (event.getDamager() instanceof Player damager) {

                    boolean critical = event.isCritical();

                    handlePlayerMelee(target, targetKit, damager, critical);

                } else if (event.getDamager() instanceof PufferFish fish) {

                    if (fish.hasMetadata("mobSmashKit")
                        && fish.getMetadata("mobSmashKit").get(0).asString().equals("pufferfish")
                        && fish.hasMetadata("source")) {

                        // cancel pufferfish attacks for source's team
                        String sourceIgn = fish.getMetadata("source").get(0).asString();
                        String sourceTeam = TeamManager.getTeam(sourceIgn);

                        if (sourceTeam.equals(TeamManager.getTeam(target.getName()))) {
                            event.setCancelled(true);
                        } else {
                            target.removeMetadata("lastDamager", Main.getInstance());
                            target.setMetadata("lastDamager", new FixedMetadataValue(Main.getInstance(), sourceIgn + "'s Pufferfish"));
                            fish.setHealth(0.0); // only allow fish to attack once
                        }

                    } else if (fish.hasMetadata("mobSmashKit")
                            && fish.getMetadata("mobSmashKit").get(0).asString().equals("pufferfish_ultimate")
                            && fish.hasMetadata("source")) {

                        // this fish is basically a display, don't let it attack anyone
                        event.setCancelled(true);

                    }

                } else if (event.getDamager() instanceof WitherSkull) {

                    event.setCancelled(true); // no damage from wither skulls, only knockback

                } else if (event.getDamager() instanceof Zombie zombie) {

                    if (zombie.hasMetadata("mobSmashKit")
                            && zombie.getMetadata("mobSmashKit").get(0).asString().equals("zombie")
                            && zombie.hasMetadata("source")) {

                        // cancel pufferfish attacks for source's team
                        String sourceIgn = zombie.getMetadata("source").get(0).asString();
                        String sourceTeam = TeamManager.getTeam(sourceIgn);

                        if (sourceTeam.equals(TeamManager.getTeam(target.getName()))) {
                            event.setCancelled(true);
                        } else {
                            target.removeMetadata("lastDamager", Main.getInstance());
                            target.setMetadata("lastDamager", new FixedMetadataValue(Main.getInstance(), sourceIgn + "'s Zombie"));
                        }

                    }

                } else if (event.getDamager() instanceof Fireball fireball) {

                    if (fireball.hasMetadata("mobSmashKit")
                            && fireball.getMetadata("mobSmashKit").get(0).asString().equals("ghast")
                            && fireball.hasMetadata("source")) {

                        String sourceIgn = fireball.getMetadata("source").get(0).asString();

                        // cancel fireball damage for teammates but not the ghast kit who shot it
                        if (TeamManager.sameTeam(sourceIgn, target.getName()) && !sourceIgn.equals(target.getName())) {
                            event.setCancelled(true);
                        }

                    } else if (fireball.hasMetadata("mobSmashKit")
                            && fireball.getMetadata("mobSmashKit").get(0).asString().equals("blaze")
                            && fireball.hasMetadata("source")) {

                        String sourceIgn = fireball.getMetadata("source").get(0).asString();

                        // cancel fireball damage for teammates but not the blaze kit who shot it
                        if (TeamManager.sameTeam(sourceIgn, target.getName()) && !sourceIgn.equals(target.getName())) {
                            event.setCancelled(true);
                        }

                    }

                } else if (event.getDamager() instanceof LightningStrike lightningStrike) {

                    if (lightningStrike.hasMetadata("mobSmashKit")
                            && lightningStrike.getMetadata("mobSmashKit").get(0).asString().equals("drowned")
                            && lightningStrike.hasMetadata("source")) {

                        String sourceIgn = lightningStrike.getMetadata("source").get(0).asString();

                        // cancel damage for teammates but not the drowned kit who caused it
                        if (TeamManager.sameTeam(sourceIgn, target.getName()) && !sourceIgn.equals(target.getName())) {
                            event.setCancelled(true);
                        } else {
                            event.setDamage(DrownedKit.lightningDamage); // vanilla amount is way too much lol
                        }

                    }

                }

            }

        } else if (event.getEntity() instanceof PufferFish fish) {

            if (fish.hasMetadata("mobSmashKit")
                && fish.getMetadata("mobSmashKit").get(0).asString().equals("pufferfish_ultimate")
                && fish.hasMetadata("source")) {

                // this fish is basically a display, don't let anyone attack it either
                event.setCancelled(true);

            }

        }

    }

    private static void handlePlayerMelee(Player target, MobKit targetKit, Player damager, boolean critical) {

        // handle specific weapons/items and their attack effects, if event is not cancelled
        ItemStack weapon = damager.getActiveItem();

        // TODO: TEST
        if (weapon.equals(PufferfishKit.sword)) {

            target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, (int) (PufferfishKit.poisonSwordInflictDuration * 20), 0, true, false, true));

        } else if (weapon.equals(SlimeKit.sword)) {

            target.addPotionEffect(new PotionEffect(PotionEffectType.POISON, (int) (SlimeKit.poisonSwordInflictDuration * 20), 0, true, false, true));

        } else if (weapon.equals(WitherSkeletonKit.witherSword)) {

            if (!(targetKit instanceof WitherSkeletonKit)) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, (int) (WitherSkeletonKit.witherDuration * 20), 0, true, false, true));
            }

        } else if (critical && kits.get(damager.getName()) instanceof BatKit batKit) {
            if (batKit.ultimateDurationRemainingTicks > 0) {

                // give half heart of regen to bat kit and nearby teammates
                // TODO: TEST
                Player player = Bukkit.getPlayer(batKit.ign);
                if (player == null) return;

                Collection<Player> nearbyPlayers = player.getLocation().getNearbyPlayers(BatKit.healRadius);
                for (Player nearbyPlayer : nearbyPlayers) {

                    if (!TeamManager.isGamePlayer(nearbyPlayer.getName()) || !TeamManager.sameTeam(batKit.ign, nearbyPlayer.getName())) continue;

                    nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 1, 0));

                }

            }
        }

    }

    private static void handleArrowHit(EntityDamageByEntityEvent event, Player target, Arrow arrow) {

        if (arrow.hasMetadata("mobSmashKit")
                && arrow.getMetadata("mobSmashKit").get(0).asString().equals("stray")
                && arrow.hasMetadata("source")) {

            String shooterIgn = arrow.getMetadata("source").get(0).asString();
            String shooterTeam = TeamManager.getTeam(shooterIgn);

            if (shooterTeam.equals(TeamManager.getTeam(target.getName()))
                    && !shooterIgn.equalsIgnoreCase(target.getName())) {
                event.setCancelled(true);

            } else {

                StrayKit shooterKit = (StrayKit) kits.get(shooterIgn);
                if (shooterKit != null && shooterKit.ultimateDurationRemainingTicks > 0) {
                    shooterKit.freezeEnemy(target.getName());
                }

            }

        }

    }

    //=========================================================================

    public static int getUltRemainingTicks(String ign) {

        MobKit kit = kits.get(ign);
        if (kit == null) return 0;
        return kit.ultimateDurationRemainingTicks;

    }

    public static void resetUltRemainingTicks(String ign) {

        MobKit kit = kits.get(ign);
        if (kit == null) return;
        kit.ultimateDurationRemainingTicks = 0;

    }

    public static Kits getKitType(String ign) {

        MobKit kit = kits.get(ign);
        if (kit == null) return null;
        return Kits.getByIndex(kit.kitID);

    }

    public static boolean isDrownedLightningRod(String ign, Block block) {

        MobKit kit = kits.get(ign);
        if (kit == null) return false;

        if (kit instanceof DrownedKit drownedKit) {

            return drownedKit.lightningRodLoc.equals(block.getLocation());

        } else {
            return false;
        }

    }

    //=========================================================================

    public static void onPlayerEnterVoid(Player player) {

        player.setLastDamageCause(new EntityDamageEvent(player, EntityDamageEvent.DamageCause.VOID, 100.0));
        player.setHealth(0.0);

    }

    public static void onProjectileLaunch(ProjectileLaunchEvent event) {

        if (event.getEntity().getShooter() instanceof Player shooter && event.getEntity() instanceof Arrow arrow) {

            arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
            if (kits.get(shooter.getName()) instanceof BowKit bowKit) {
                bowKit.onFireArrow();
            }

        }

        if (event.getEntity().getShooter() instanceof Player shooter && event.getEntity() instanceof Trident trident) {

            if (kits.get(shooter.getName()) instanceof DrownedKit drownedKit) {
                drownedKit.onTridentThrow(trident);
            }

        }

    }

    public static void onPlayerLoadCrossbow(EntityLoadCrossbowEvent event) {

        MobKit kit = kits.get(event.getEntity().getName());
        if (kit != null) kit.onLoadCrossbow(event);

    }
}
