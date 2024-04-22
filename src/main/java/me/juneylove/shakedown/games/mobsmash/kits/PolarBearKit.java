package me.juneylove.shakedown.games.mobsmash.kits;

import me.juneylove.shakedown.games.mobsmash.AbilityManager;
import me.juneylove.shakedown.games.mobsmash.Kits;
import me.juneylove.shakedown.games.mobsmash.MobKit;
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
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;

public class PolarBearKit extends MobKit {

    int healRange = 10;
    double absorptionRadius = 8.0;
    int absorptionDurationTicks = 600; // 30 sec

    final Sound ABILITY_SOUND = Sound.sound(Key.key("entity.polar_bear.ambient"), Sound.Source.WEATHER, 1.0f, 1.0f);
    final Sound ULTIMATE_SOUND = Sound.sound(Key.key("entity.polar_bear.warning"), Sound.Source.WEATHER, 5.0f, 1.0f);

    {
        abilityDurationSeconds = 0;
        abilityCooldownSeconds = 8;
        ultimateDurationSeconds = 0;
        ultPointsRequired = 5;
        maxHealth = 24.0;

        displayName = Component.text("Polar Bear").color(TextColor.color(0xefefde)).decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false);

        description = new ArrayList<>();
        description.add(Component.text("Primary weapon: Stone Axe").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.text("Passive: Increased Health").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(abilityDivider);
        description.add(Component.text("Heal a nearby teammate").color(abilityColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(ultimateDivider);
        description.add(Component.text("Give Absorption to nearby allies").color(ultimateColor)
                .decoration(TextDecoration.ITALIC, false));

        details = new ArrayList<>();
        details.add(detailsDivider);
        details.add(detailsHeader);
        details.add(Component.text("Your ")

                .append(Component.text("ability").color(abilityColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.swapOffhand"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" heals a teammate by looking at them and pressing the ability key. Your "))

                .append(Component.text("ultimate").color(ultimateColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.drop"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" grants Absorption to all nearby teammates.")));
        details.add(detailsDivider);

        kit = new ItemStack[]{GUIFormat.unbreakable(Material.STONE_AXE)};

    }

    @Override
    public void onAbilityStart() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        Player targeted = entityToHeal();

        if (targeted == null) {

            // this should not happen! defensive
            // reject ability trigger and leave it still ready to use
            abilityDurationRemainingTicks = 0;
            abilityCooldownRemainingTicks = 0;
            return;

        }

        // heal teammate - level 2 instant health, heals 4 hearts
        // TODO: TEST
        targeted.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 1, 1));

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

        Collection<Player> nearbyPlayers = player.getLocation().getNearbyPlayers(absorptionRadius);
        for (Player nearbyPlayer : nearbyPlayers) {

            if (!TeamManager.isGamePlayer(nearbyPlayer.getName()) || !TeamManager.sameTeam(ign, nearbyPlayer.getName())) continue;
            if (ign.equals(nearbyPlayer.getName())) continue; // do not give absorption to self because already increased health that would be so op

            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, absorptionDurationTicks, 0));

        }

        player.getWorld().playSound(ULTIMATE_SOUND, player);

    }

    @Override
    public void ultimateRun() {}

    @Override
    public void onUltimateEnd() {}

    // ==========

    @Nullable
    public Player entityToHeal() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return null;

        RayTraceResult result = player.rayTraceEntities(healRange, false);
        if (result == null) return null;
        Entity hitEntity = result.getHitEntity();
        if (hitEntity == null) return null;

        if (hitEntity instanceof Player target) {

            if (TeamManager.sameTeam(ign, target.getName())) {

                // player is looking at teammate - see if they can be healed
                Kits targetedKit = AbilityManager.getKitType(target.getName());
                if (targetedKit == null) return null;

                if (target.getHealth() < targetedKit.kit.maxHealth) {
                    return target;
                } else {
                    return null;
                }

            } else {
                return null;
            }

        } else {
            return null;
        }

    }

}
