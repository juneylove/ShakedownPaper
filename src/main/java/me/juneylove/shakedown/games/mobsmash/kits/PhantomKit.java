package me.juneylove.shakedown.games.mobsmash.kits;

import me.juneylove.shakedown.games.mobsmash.BowKit;
import me.juneylove.shakedown.ui.GUIFormat;
import me.juneylove.shakedown.ui.TextFormat;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

public class PhantomKit extends BowKit {

    ItemStack priorChestItem = null;

    final Sound ABILITY_SOUND = Sound.sound(Key.key("entity.phantom.ambient"), Sound.Source.WEATHER, 1.0f, 1.0f);
    final Sound ULTIMATE_SOUND = Sound.sound(Key.key("entity.phantom.bite"), Sound.Source.WEATHER, 5.0f, 1.0f);

    {
        abilityDurationSeconds = 5;
        abilityCooldownSeconds = 10;
        ultimateDurationSeconds = 20;
        ultPointsRequired = 5;

        displayName = Component.text("Phantom").color(TextColor.color(0x5161a4)).decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false);

        description = new ArrayList<>();
        description.add(Component.text("Primary weapon: Bow").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.text("Secondary weapon: Wooden Sword").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(abilityDivider);
        description.add(Component.text("Slow Falling").color(abilityColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(ultimateDivider);
        description.add(Component.text("Elytra").color(ultimateColor)
                .decoration(TextDecoration.ITALIC, false));

        details = new ArrayList<>();
        details.add(detailsDivider);
        details.add(detailsHeader);
        details.add(Component.text("Your ")

                .append(Component.text("ability").color(abilityColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.swapOffhand"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" gives you slow falling for " + TextFormat.formatNumber(abilityDurationSeconds) + " seconds or until you touch the ground. Your "))

                .append(Component.text("ultimate").color(ultimateColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.drop"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" equips an Elytra for "
                        + TextFormat.formatNumber(ultimateDurationSeconds) + " seconds. ")));
        details.add(detailsDivider);

        kit = new ItemStack[]{GUIFormat.unbreakable(Material.WOODEN_SWORD), GUIFormat.unbreakable(Material.BOW), new ItemStack(Material.ARROW, maxArrowAmount)};

    }

    @Override
    public void onDeath() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        if (ultimateDurationRemainingTicks > 0) {
            ultimateDurationRemainingTicks = 0;
            onUltimateEnd();
        }

        player.removePotionEffect(PotionEffectType.SLOW_FALLING);

    }

    @Override
    public void onAbilityStart() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        player.getWorld().playSound(ABILITY_SOUND, player);

        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, (int) (abilityDurationSeconds*20), 0));

    }

    @Override
    public void abilityRun() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        if (((Entity) player).isOnGround()) abilityDurationRemainingTicks = 1; // calling onAbilityEnd will be taken care of by AbilityManager

    }

    @Override
    public void onAbilityEnd() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        player.removePotionEffect(PotionEffectType.SLOW_FALLING);

    }

    @Override
    public void onUltimateStart() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        player.getWorld().playSound(ULTIMATE_SOUND, player);

        priorChestItem = player.getInventory().getItem(EquipmentSlot.CHEST);
        player.getInventory().setItem(EquipmentSlot.CHEST, new ItemStack(Material.ELYTRA));

    }

    @Override
    public void ultimateRun() {}

    @Override
    public void onUltimateEnd() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        player.getInventory().setItem(EquipmentSlot.CHEST, priorChestItem);

    }

}
