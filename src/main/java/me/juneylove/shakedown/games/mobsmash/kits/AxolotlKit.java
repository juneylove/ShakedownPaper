package me.juneylove.shakedown.games.mobsmash.kits;

import me.juneylove.shakedown.games.mobsmash.MobKit;
import me.juneylove.shakedown.ui.GUIFormat;
import me.juneylove.shakedown.ui.TextFormat;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.type.BubbleColumn;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

public class AxolotlKit extends MobKit {

    Location bubbleColumnLoc;
    public static double regenDuration = 4.0;
    public static double regenRadius = 4.0;

    final Sound ABILITY_SOUND = Sound.sound(Key.key("entity.axolotl.idle_water"), Sound.Source.WEATHER, 1.0f, 1.0f);
    final Sound ULTIMATE_SOUND = Sound.sound(Key.key("entity.axolotl.attack"), Sound.Source.WEATHER, 5.0f, 1.0f);

    {
        abilityDurationSeconds = 5;
        abilityCooldownSeconds = 10;
        ultimateDurationSeconds = 10;
        ultPointsRequired = 5;

        displayName = Component.text("Axolotl").color(TextColor.color(0xf27093)).decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false);

        description = new ArrayList<>();
        description.add(Component.text("Primary weapon: Stone Sword").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.text("Passive: Dolphin's Grace").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(abilityDivider);
        description.add(Component.text("Launch into the air with a bubble column").color(abilityColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(ultimateDivider);
        description.add(Component.text("Regenerate your team's health with kills").color(ultimateColor)
                .decoration(TextDecoration.ITALIC, false));

        details = new ArrayList<>();
        details.add(detailsDivider);
        details.add(detailsHeader);
        details.add(Component.text("Your ")

                .append(Component.text("ability").color(abilityColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.swapOffhand"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" places a bubble column in front of you (disappears after " + TextFormat.formatNumber(abilityDurationSeconds)
                        + " seconds) that can launch yourself and/or enemies. Your "))

                .append(Component.text("ultimate").color(ultimateColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.drop"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" lasts "
                        + TextFormat.formatNumber(ultimateDurationSeconds) + " seconds, applying " + TextFormat.formatNumber(regenDuration)
                        + " seconds of Regeneration II to you and nearby teammates for each killed enemy.")));
        details.add(detailsDivider);

        kit = new ItemStack[]{GUIFormat.unbreakable(Material.STONE_SWORD)};

    }

    @Override
    public void onDeath() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        if (abilityDurationRemainingTicks > 0) {
            abilityDurationRemainingTicks = 0;
            onAbilityEnd();
        }
        ultimateDurationRemainingTicks = 0;

    }

    @Override
    public void applyKit() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        player.addPotionEffect(new PotionEffect(PotionEffectType.DOLPHINS_GRACE, PotionEffect.INFINITE_DURATION, 0, true));

    }

    @Override
    public void onAbilityStart() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        bubbleColumnLoc = player.getEyeLocation().add(player.getEyeLocation().getDirection());

        if (bubbleColumnLoc.getBlock().getType() != Material.AIR) {

            // place at player's current location instead if block in front isn't air
            bubbleColumnLoc = player.getEyeLocation();
            if (bubbleColumnLoc.getBlock().getType() != Material.AIR) {

                // player must be underwater or something i dunno, but cancel ability
                abilityCooldownRemainingTicks = 0;
                abilityDurationRemainingTicks = 0;

            }

        }

        BubbleColumn bubbleColumn = (BubbleColumn) Bukkit.createBlockData(Material.BUBBLE_COLUMN);
        bubbleColumn.setDrag(false);
        player.getWorld().setBlockData(bubbleColumnLoc, bubbleColumn);

        player.getWorld().playSound(ABILITY_SOUND, player);

    }

    @Override
    public void abilityRun() {}

    @Override
    public void onAbilityEnd() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        if (bubbleColumnLoc != null) {
            player.getWorld().setBlockData(bubbleColumnLoc, Bukkit.createBlockData(Material.AIR));
        }

    }

    @Override
    public void onUltimateStart() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        player.getWorld().playSound(ULTIMATE_SOUND, player);

    }

    @Override
    public void ultimateRun() {}

    @Override
    public void onUltimateEnd() {}

}
