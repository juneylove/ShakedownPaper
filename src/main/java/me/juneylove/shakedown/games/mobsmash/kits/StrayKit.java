package me.juneylove.shakedown.games.mobsmash.kits;

import me.juneylove.shakedown.Main;
import me.juneylove.shakedown.control.MatchProgress;
import me.juneylove.shakedown.games.mobsmash.BowKit;
import me.juneylove.shakedown.mechanics.Respawn;
import me.juneylove.shakedown.ui.GUIFormat;
import me.juneylove.shakedown.ui.TextFormat;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class StrayKit extends BowKit {

    public static double slowArrowEffectDuration = 4.0;

    static double freezeDuration = 5.0;
    TextComponent frozenText = Component.text(TextFormat.smallText("frozen")).color(NamedTextColor.AQUA);

    Set<FrozenEnemy> frozenEnemies = new HashSet<>();
    BukkitTask ultimateTask = null;

    final Sound ABILITY_SOUND = Sound.sound(Key.key("entity.stray.ambient"), Sound.Source.WEATHER, 1.0f, 1.0f);
    final Sound ULTIMATE_SOUND = Sound.sound(Key.key("entity.skeleton.converted_to_stray"), Sound.Source.WEATHER, 5.0f, 1.0f);

    {
        abilityDurationSeconds = 5;
        abilityCooldownSeconds = 10;
        ultimateDurationSeconds = 6;
        ultPointsRequired = 5;

        maxArrowAmount = 2; // temp

        displayName = Component.text("Stray").color(TextColor.color(0x566d6d)).decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false);

        description = new ArrayList<>();
        description.add(Component.text("Primary weapon: Bow").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.text("Secondary weapon: Wooden Sword").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(abilityDivider);
        description.add(Component.text("Slowness arrows").color(abilityColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(ultimateDivider);
        description.add(Component.text("Freeze arrows").color(ultimateColor)
                .decoration(TextDecoration.ITALIC, false));

        details = new ArrayList<>();
        details.add(detailsDivider);
        details.add(detailsHeader);
        details.add(Component.text("Your ")

                .append(Component.text("ability").color(abilityColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.swapOffhand"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" turns all fired arrows into slowness arrows for " + TextFormat.formatNumber(abilityDurationSeconds) + " seconds. Your "))

                .append(Component.text("ultimate").color(ultimateColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.drop"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" freezes (for " + TextFormat.formatNumber(freezeDuration) + " seconds) all enemies shot within "
                        + TextFormat.formatNumber(ultimateDurationSeconds) + " seconds. "))
                .append(Component.text("Frozen enemies cannot move, cannot be damaged, and cannot capture objectives.")));
        details.add(detailsDivider);

        kit = new ItemStack[]{GUIFormat.unbreakable(Material.WOODEN_SWORD), GUIFormat.unbreakable(Material.BOW), new ItemStack(Material.ARROW, maxArrowAmount)};

    }

    @Override
    public void onDeath() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        abilityDurationRemainingTicks = 0;

        if (ultimateDurationRemainingTicks > 0) {
            ultimateDurationRemainingTicks = 0;
            onUltimateEnd();
        }

    }

    @Override
    public void onAbilityStart() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        player.getWorld().playSound(ABILITY_SOUND, player);

    }

    @Override
    public void abilityRun() {}

    @Override
    public void onAbilityEnd() {}

    @Override
    public void onUltimateStart() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        player.getWorld().playSound(ULTIMATE_SOUND, player);

    }

    @Override
    public void ultimateRun() {

        for (FrozenEnemy frozenEnemy : frozenEnemies) {
            frozenEnemy.run();
        }

        // cancel task once all enemies are unfrozen, if ultimate duration has
        // finished and we actually have a task
        if (ultimateDurationRemainingTicks == 0
            && ultimateTask != null
            && frozenEnemies.size() == 0) {
            ultimateTask.cancel();
            ultimateTask = null;
        }

    }

    @Override
    public void onUltimateEnd() {

        if (frozenEnemies.size() > 0) {
            ultimateTask = new StrayRunnable().runTaskTimer(Main.getInstance(), 0, 1);
        }

    }

    // ==========

    public class StrayRunnable extends BukkitRunnable {

        @Override
        public void run() {
            ultimateRun();
        }

    }

    public void freezeEnemy(String ign) {

        frozenEnemies.add(new FrozenEnemy(ign, this.ign));

    }

    private class FrozenEnemy {

        String ign;
        String frozenBy;
        BlockDisplay bottomIceBlock;
        BlockDisplay topIceBlock;
        Instant timeFrozen;

        FrozenEnemy(String ign, String frozenBy) {
            this.ign = ign;
            this.frozenBy = frozenBy;
            freeze();
        }

        public void freeze() {

            Player player = Bukkit.getPlayer(ign);
            if (player == null) return;

            timeFrozen = Instant.now();

            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,         (int) freezeDuration*20, 7, true, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, (int) freezeDuration*20, 0, true, false, false));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,     (int) freezeDuration*20, 1, true, false, false));

            player.setMetadata("frozenBy", new FixedMetadataValue(Main.getInstance(), frozenBy));

            Title.Times times = Title.Times.times(Duration.ZERO, Duration.ofMillis((long) (1000*(freezeDuration-1.0))), Duration.ofSeconds(1));
            player.showTitle(Title.title(frozenText, Component.empty(), times));

            Location iceLocation = player.getLocation().add(-0.5, 0, -0.5);
            iceLocation.setPitch(0.0f);
            iceLocation.setYaw(0.0f);
            bottomIceBlock = (BlockDisplay) player.getWorld().spawnEntity(iceLocation, EntityType.BLOCK_DISPLAY);
            bottomIceBlock.setBlock(Bukkit.createBlockData(Material.ICE));
            topIceBlock = (BlockDisplay) player.getWorld().spawnEntity(iceLocation.add(0, 1, 0), EntityType.BLOCK_DISPLAY);
            topIceBlock.setBlock(Bukkit.createBlockData(Material.ICE));

        }

        public void run() {

            Player player = Bukkit.getPlayer(ign);
            if (player == null) return;

            if (Respawn.IsTempSpec(ign) || !MatchProgress.playIsActive()
                   || Instant.now().isAfter(timeFrozen.plusMillis((long) (freezeDuration*1000)))) {
                unfreeze();
                return;
            }

            Location iceLocation = player.getLocation().add(-0.5, 0, -0.5);
            iceLocation.setPitch(0.0f);
            iceLocation.setYaw(0.0f);
            bottomIceBlock.teleport(iceLocation);
            bottomIceBlock.setVelocity(player.getVelocity());
            topIceBlock.teleport(iceLocation.add(0, 1, 0));
            topIceBlock.setVelocity(player.getVelocity());

        }

        void unfreeze() {

            if (bottomIceBlock != null) bottomIceBlock.remove();
            if (topIceBlock != null) topIceBlock.remove();

            frozenEnemies.remove(this);

            Player player = Bukkit.getPlayer(ign);
            if (player == null) return;

            // only technically needed if they're unfrozen early by round ending, but good to have
            player.removePotionEffect(PotionEffectType.SLOW);
            player.removePotionEffect(PotionEffectType.INVISIBILITY);
            player.removePotionEffect(PotionEffectType.WEAKNESS);

            player.clearTitle();

            player.removeMetadata("frozenBy", Main.getInstance());

        }

    }

}
