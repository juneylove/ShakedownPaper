package me.juneylove.shakedown.games.mobsmash.kits;

import me.juneylove.shakedown.Main;
import me.juneylove.shakedown.games.mobsmash.MobKit;
import me.juneylove.shakedown.mechanics.GlowManager;
import me.juneylove.shakedown.scoring.TeamManager;
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
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class BatKit extends MobKit {

    double glowRadius = 5.0;
    public static double healRadius = 5.0;

    final Sound ABILITY_SOUND = Sound.sound(Key.key("entity.bat.takeoff"), Sound.Source.WEATHER, 1.0f, 1.0f);
    final Sound ULTIMATE_SOUND = Sound.sound(Key.key("entity.bat.ambient"), Sound.Source.WEATHER, 5.0f, 1.0f);

    {
        abilityDurationSeconds = 6;
        abilityCooldownSeconds = 14;
        ultimateDurationSeconds = 6;
        ultPointsRequired = 5;

        displayName = Component.text("Bat").color(TextColor.color(0x664965)).decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false);

        description = new ArrayList<>();
        description.add(Component.text("Primary weapon: Stone Sword").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.text("Passive: Night Vision").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(abilityDivider);
        description.add(Component.text("Reveal nearby enemies").color(abilityColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(ultimateDivider);
        description.add(Component.text("Give health to your team with each critical hit").color(ultimateColor)
                .decoration(TextDecoration.ITALIC, false));

        details = new ArrayList<>();
        details.add(detailsDivider);
        details.add(detailsHeader);
        details.add(Component.text("Your ")

                .append(Component.text("ability").color(abilityColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.swapOffhand"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" makes all nearby enemies glow for " + TextFormat.formatNumber(abilityDurationSeconds) + " seconds. Your "))

                .append(Component.text("ultimate").color(ultimateColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.drop"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" heals half a heart to all nearby teammates for every critical hit on an enemy.")));
        details.add(detailsDivider);

        kit = new ItemStack[]{GUIFormat.unbreakable(Material.STONE_SWORD)};

    }

    @Override
    public void onDeath() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        if (abilityDurationRemainingTicks > 0) {
            abilityDurationRemainingTicks = 0;
            player.setGravity(true);
        }

    }

    @Override
    public void applyKit() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, PotionEffect.INFINITE_DURATION, 0, true, false, false));

    }

    @Override
    public void onAbilityStart() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        Collection<Player> nearbyPlayers = player.getLocation().getNearbyPlayers(glowRadius);
        Set<String> teammates = TeamManager.getMembers(TeamManager.getTeam(ign));
        for (Player nearbyPlayer : nearbyPlayers) {

            if (!TeamManager.isGamePlayer(nearbyPlayer.getName()) || TeamManager.sameTeam(ign, nearbyPlayer.getName())) continue;

            NamedTextColor namedColor = TextFormat.GetNamedColor(TeamManager.getTeam(nearbyPlayer.getName()));
            GlowManager.addGlow(nearbyPlayer, namedColor, teammates, (int) (abilityDurationSeconds*20));

        }

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
    public void ultimateRun() {}

    @Override
    public void onUltimateEnd() {}

}
