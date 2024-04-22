package me.juneylove.shakedown.games.mobsmash.kits;

import me.juneylove.shakedown.Main;
import me.juneylove.shakedown.games.mobsmash.MobKit;
import me.juneylove.shakedown.mechanics.Respawn;
import me.juneylove.shakedown.ui.GUIFormat;
import me.juneylove.shakedown.ui.TextFormat;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BlazeKit extends MobKit {

    final double floatHeight = 5.0;
    final double fireballSpeed = 0.5;
    final int fireChargeEachCooldownTicks = 9; // Time between each fireball
    final int fireChargeRefillCooldownTicks = 100; // Time between firing first fireball and restock

    final Sound SHOOT_SOUND = Sound.sound(Key.key("entity.blaze.shoot"), Sound.Source.WEATHER, 1.0f, 1.0f);
    final Sound ABILITY_SOUND = Sound.sound(Key.key("entity.blaze.burn"), Sound.Source.WEATHER, 1.0f, 1.0f);
    final Sound ULTIMATE_SOUND = Sound.sound(Key.key("entity.blaze.ambient"), Sound.Source.WEATHER, 5.0f, 1.0f);

    int fireChargeSlot = 36;
    int fireChargeRefillRemainingTicks = 0;
    ItemStack fireCharges;

    {
        abilityDurationSeconds = 10;
        abilityCooldownSeconds = 30;
        ultimateDurationSeconds = 10;
        ultPointsRequired = 5;

        displayName = Component.text("Blaze").color(TextColor.color(0xffd529)).decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false);

        description = new ArrayList<>();
        description.add(Component.text("Primary weapon: Fireball Burst").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.text("Secondary weapon: Wooden Sword").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(abilityDivider);
        description.add(Component.text("Gain fire resistance for " + TextFormat.formatNumber(abilityDurationSeconds) + " seconds").color(abilityColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(ultimateDivider);
        description.add(Component.text("Float up into the air for " + TextFormat.formatNumber(ultimateDurationSeconds) + " seconds").color(ultimateColor)
                .decoration(TextDecoration.ITALIC, false));

        details = new ArrayList<>();
        details.add(detailsDivider);
        details.add(detailsHeader);
        details.add(Component.text("Right click your fire charges to shoot, and they will refill to 3 in "
                        + TextFormat.formatNumber(fireChargeRefillCooldownTicks/20.0) + " seconds. Your ")

                .append(Component.text("ability").color(abilityColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.swapOffhand"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" gives fire resistance for " + TextFormat.formatNumber(abilityDurationSeconds) + " seconds. Your "))

                .append(Component.text("ultimate").color(ultimateColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.drop"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" also includes slow falling, allowing you to float across gaps.")));
        details.add(detailsDivider);

        fireCharges = GUIFormat.customItemName(new ItemStack(Material.FIRE_CHARGE, 3), Component.text("Fireball Burst").color(NamedTextColor.DARK_RED).decorate(TextDecoration.BOLD));
        fireCharges = GUIFormat.addLore(fireCharges, List.of(Component.text("Right click to shoot").color(NamedTextColor.YELLOW)));

        kit = new ItemStack[]{fireCharges, GUIFormat.unbreakable(Material.WOODEN_SWORD)};

    }

    @Override
    public void onDeath() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        abilityDurationRemainingTicks = 0;
        ultimateDurationRemainingTicks = 0;

    }

    @Override
    public void applyKit() {
        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;
        fireChargeSlot = player.getInventory().first(Material.FIRE_CHARGE);
    }

    @Override
    public void passiveRun() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        if (Respawn.IsTempSpec(ign)) {
            fireChargeRefillRemainingTicks = 0;
            return;
        }

        if (fireChargeRefillRemainingTicks == 1) {
            player.getInventory().remove(Material.FIRE_CHARGE);
            player.getInventory().setItem(fireChargeSlot, fireCharges);
        }

        if (fireChargeRefillRemainingTicks > 0) {
            fireChargeRefillRemainingTicks--;
        }

    }

    @Override
    public void onUseItem(PlayerInteractEvent event) {

        if (!event.getAction().isRightClick()) return;

        ItemStack itemInHand = event.getItem();

        if (itemInHand != null && itemInHand.getType() == Material.FIRE_CHARGE) {

            event.setCancelled(true);
            Player player = Bukkit.getPlayer(ign);
            if (player == null) return;

            if (player.getCooldown(Material.FIRE_CHARGE) > 0) return;

            player.setCooldown(Material.FIRE_CHARGE, fireChargeEachCooldownTicks);

            fireChargeSlot = player.getInventory().first(Material.FIRE_CHARGE);

            HashMap<Integer,? extends ItemStack> allFireCharges = player.getInventory().all(Material.FIRE_CHARGE);
            int totalFireCharges = 0;
            for (ItemStack stack : allFireCharges.values()) {
                totalFireCharges += stack.getAmount();
            }
            int remainingFireCharges = Math.min(totalFireCharges, 3) - 1;

            if (totalFireCharges > 2) {
                fireChargeRefillRemainingTicks = fireChargeRefillCooldownTicks;
            }

            ItemStack newFireCharges = fireCharges.clone();
            newFireCharges.setAmount(remainingFireCharges);
            player.getInventory().remove(Material.FIRE_CHARGE);
            player.getInventory().setItem(fireChargeSlot, newFireCharges);

            // ok with all that taken care of, actually shoot the thing
            Vector direction = player.getEyeLocation().getDirection();
            Vector velocity = direction.multiply(fireballSpeed);
            Location fireballSpawn = player.getEyeLocation().add(direction.multiply(0.5)); // spawn 0.5 blocks in front of player's looking direction
            Entity fireball = player.getWorld().spawnEntity(fireballSpawn, EntityType.SMALL_FIREBALL);
            fireball.setMetadata("mobSmashKit", new FixedMetadataValue(Main.getInstance(), "blaze"));
            fireball.setMetadata("source", new FixedMetadataValue(Main.getInstance(), ign));
            fireball.setVelocity(velocity);
            player.getWorld().playSound(SHOOT_SOUND, fireball);

        }

    }

    @Override
    public void onAbilityStart() {
        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;
        player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, (int) abilityDurationSeconds*20, 1));
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
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, (int) ultimateDurationSeconds*20 + 50, 1, true, false, false));
        player.getWorld().playSound(ULTIMATE_SOUND, player);
        //player.setVisualFire(true);
    }

    @Override
    public void ultimateRun() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        if (player.getWorld().rayTraceBlocks(player.getLocation(), new Vector(0, -1, 0), floatHeight, FluidCollisionMode.ALWAYS) != null) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 2, 1, true, false, false));
        }

    }

    @Override
    public void onUltimateEnd() {
        //player.setVisualFire(false);
    }

}
