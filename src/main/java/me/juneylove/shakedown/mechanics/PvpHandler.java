package me.juneylove.shakedown.mechanics;

import me.juneylove.shakedown.Main;
import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.mechanics.InvulnerableRegion;
import me.juneylove.shakedown.mechanics.abilities.HealBow;
import me.juneylove.shakedown.mechanics.Respawn;
import me.juneylove.shakedown.scoring.ScoreManager;
import me.juneylove.shakedown.scoring.TeamManager;
import me.juneylove.shakedown.ui.Messaging;
import me.juneylove.shakedown.ui.TextFormat;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.List;

public class PvpHandler {

    public static void onDamage(EntityDamageByEntityEvent event, Player source, Player target) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        String sourceIgn = source.getName();

        // Cancel damage in the case of friendly fire hit
        if (!game.friendlyFireEnabled) {

            if (TeamManager.sameTeam(sourceIgn, target.getName())) {
                event.setCancelled(true);
            }

        }

        if (event.isCancelled() && !InvulnerableRegion.isSpawnInvulnerable(target)) {
            meleeThroughCancel(source, target);
            return;
        }

        // Manage predetermined damage amount and per-damage score
        if (game.predeterminedDamageHp != 0) {

            event.setDamage(game.predeterminedDamageHp);
            if (game.pvpDamageScore != 0) {
                ScoreManager.addScore(sourceIgn, game.pvpDamageScore);
            }

        } else {

            if (game.pvpDamageScore != 0) {
                ScoreManager.addScore(sourceIgn, (int) (game.pvpDamageScore * event.getFinalDamage()));
            }

        }

        // Track last damager
        target.removeMetadata("lastDamager", Main.getInstance());
        target.setMetadata("lastDamager", new FixedMetadataValue(Main.getInstance(), sourceIgn));

        if (game.pvpDingEnabled) {

            target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_IRON_XYLOPHONE, 1.0f, 1.0f);

        }

        game.onPlayerDamage(sourceIgn, target);

    }

    public static void onKill(PlayerDeathEvent event, Player source, Player target) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        if (game.deathFireworkEnabled) {

            Firework firework = (Firework) target.getWorld().spawnEntity(target.getLocation(), EntityType.FIREWORK);
            FireworkMeta meta = firework.getFireworkMeta();
            Color color = TextFormat.GetColor(TeamManager.getTeam(target.getName()));
            FireworkEffect effect = FireworkEffect.builder().withColor(color).build();
            meta.addEffect(effect);
            firework.setFireworkMeta(meta);
            firework.detonate();

        }

        String sourceIgn;
        if (source != null) {
            sourceIgn = source.getName();
        } else {
            sourceIgn = null;
        }

        String targetIgn = target.getName();
        EntityDamageEvent lastDamageEvent = target.getLastDamageCause();

        // Respawn timer (if enabled) or spectate
        Respawn.NewDeath(target);

        // Below section (specifically Respawn.RemainingLives()) MUST be called AFTER the
        // above call to Respawn.NewDeath(), because that's where remaining lives are updated
        boolean keepInventory;
        if (game.currentRound.numberOfLives != 0) { // Ignore keepInventoryOnFinalDeath if infinite lives because it's irrelevant

            if (Respawn.RemainingLives(target) > 0) {
                keepInventory = game.keepInventoryOnDeath;
            } else {
                keepInventory = game.keepInventoryOnFinalDeath;
            }

        } else {
            keepInventory = game.keepInventoryOnDeath;
        }

        if (keepInventory) {

            event.setKeepInventory(true);
            event.setKeepLevel(true);
            event.getDrops().clear();
            event.setDroppedExp(0);

        }

        // Set global death message to null so we can manage it separately per-user
        event.deathMessage(Component.empty());

        List<MetadataValue> meta = target.getMetadata("lastDamager");
        String lastDamager;
        if (meta.size() > 0) {
            lastDamager = meta.get(0).asString();
        } else if (source != null) {
            lastDamager = sourceIgn;
        } else {
            lastDamager = null;
        }
        // Clear same metadata when the target dies so it's reset in case of respawn/game change
        target.removeMetadata("lastDamager", Main.getInstance());

        if (lastDamageEvent == null) {
            Messaging.deathMessage(lastDamager, targetIgn, target.getWorld(), EntityDamageEvent.DamageCause.CUSTOM);
        } else {
            Messaging.deathMessage(lastDamager, targetIgn, target.getWorld(), lastDamageEvent.getCause());
        }

        // account for lastDamager = "Player1's ability" etc (truncate ' and everything after to get username for scoring)
        if (lastDamager != null && lastDamager.contains("'")) {
            int endIndex = lastDamager.indexOf("'");
            lastDamager = lastDamager.substring(0, endIndex);
        }

        // Score tracking
        if (lastDamager != null) {
            ScoreManager.addScore(lastDamager, game.pvpKillScore);
        }

        game.onPlayerDeath(lastDamager, target);

    }

    public static void onProjectileHit(ProjectileHitEvent event, String sourceIgn, Player target) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        if (!game.friendlyFireEnabled && sourceIgn != null) {

            if (TeamManager.sameTeam(sourceIgn, target.getName())) {

                if (!sourceIgn.equalsIgnoreCase(target.getName())) {

                    // Hit a teammate - cancel the hit unless the shooter has a HealBow
                    if (!(event.getEntity() instanceof Arrow && HealBow.IsHealer(sourceIgn))) {
                        event.setCancelled(true);
                    }

                }

            }

        }

    }

    public static void onProjectileDamage(EntityDamageByEntityEvent event, Projectile projectile, Player target) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        if (projectile.getShooter() instanceof Player source) {

            String sourceIgn = source.getName();

            if (TeamManager.isGamePlayer(sourceIgn)) {

                // A game player fired the arrow - Check friendly fire
                if (!game.friendlyFireEnabled) {

                    // Cancel damage for others on their team if no friendly fire
                    if (TeamManager.sameTeam(sourceIgn, target.getName())) {

                        // but not the shooter themself ofc
                        if (!sourceIgn.equalsIgnoreCase(target.getName())) {

                            // Hit a teammate - heal if active, and cancel damage
                            if (HealBow.IsHealer(sourceIgn) && projectile instanceof AbstractArrow arrow) {
                                HealBow.HealTeammate(source, target, arrow);
                            }
                            event.setCancelled(true);
                            return;

                        }

                    }

                }

                if (!sourceIgn.equalsIgnoreCase(target.getName())) { // only update last damager if player did not damage themself
                    target.removeMetadata("lastDamager", Main.getInstance());
                    target.setMetadata("lastDamager", new FixedMetadataValue(Main.getInstance(), sourceIgn));
                }

            }

        }

    }

    public static void onExplosionDamage(EntityDamageByEntityEvent event, Explosive explosive, Player target) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        String targetIgn = target.getName();
        String sourceIgn = explosive.getMetadata("source").get(0).asString();

        if (TeamManager.isGamePlayer(sourceIgn)) {

            // A game player placed/shot the explosive - Check friendly fire
            if (!game.friendlyFireEnabled) {

                // Cancel damage for others on their team if no friendly fire
                if (TeamManager.sameTeam(sourceIgn, targetIgn)) {

                    // but not the source themself ofc
                    if (!sourceIgn.equalsIgnoreCase(targetIgn)) {
                        event.setCancelled(true);
                        return;
                    }

                }

            }

            target.removeMetadata("lastDamager", Main.getInstance());
            target.setMetadata("lastDamager", new FixedMetadataValue(Main.getInstance(), sourceIgn));

        } else {

            // Cancel if placer can't be identified or is not on a game team
            event.setCancelled(true);

        }

    }

    public static void meleeThroughCancel(Player source, Player target) {

        double playerReach = 3.0;

        // this is so stupid
        // the goal: find the *next* entity along the player's line of sight
        // beyond the initial target.
        // so.
        // we need the *far side* intersection of the player's line of sight
        // and the target's hitbox, so we can start raytracing from there -
        // we'll get this by raytracing from the edge of the player's reach
        // back to the target's hitbox and getting the hit location from that.
        Vector direction = source.getEyeLocation().getDirection();
        Vector reach = direction.multiply(playerReach);
        Vector start1 = source.getEyeLocation().toVector().add(reach);
        RayTraceResult result1 = target.getBoundingBox().rayTrace(start1, direction.multiply(-1), playerReach);
        if (result1 == null) return; // *should* never happen, theoretically
        Vector farSide = result1.getHitPosition();

        // now create a point slightly beyond this far side intersection
        // to start our actual raytrace without hitting the original target
        double farSideDistance = start1.distance(farSide);
        double newStartDistance = farSideDistance + 0.05;
        Vector start2 = start1.add(direction.multiply(newStartDistance));
        Location start2Loc = new Location(source.getWorld(), 0, 0, 0).add(start2);
        double remainingDistance = playerReach - newStartDistance;
        RayTraceResult result2 = source.getWorld().rayTraceEntities(start2Loc, direction, remainingDistance);

        if (result2 == null) return;
        if (result2.getHitEntity() == null) return;

        source.attack(result2.getHitEntity());

    }

}
