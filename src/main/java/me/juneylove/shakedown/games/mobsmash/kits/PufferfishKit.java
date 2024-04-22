package me.juneylove.shakedown.games.mobsmash.kits;

import me.juneylove.shakedown.Main;
import me.juneylove.shakedown.games.mobsmash.BowKit;
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
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class PufferfishKit extends BowKit {

    final Sound ABILITY_SOUND = Sound.sound(Key.key("entity.puffer_fish.hurt"), Sound.Source.WEATHER, 1.0f, 1.0f);
    final Sound ULTIMATE_SOUND_1 = Sound.sound(Key.key("entity.puffer_fish.blow_up"), Sound.Source.WEATHER, 5.0f, 0.5f);
    final Sound ULTIMATE_SOUND_2 = Sound.sound(Key.key("entity.puffer_fish.blow_up"), Sound.Source.WEATHER, 1.0f, 0.75f);
    final Sound ULTIMATE_SOUND_3 = Sound.sound(Key.key("entity.puffer_fish.blow_up"), Sound.Source.WEATHER, 1.0f, 1.0f);
    final Sound ULTIMATE_SOUND_4 = Sound.sound(Key.key("entity.puffer_fish.blow_up"), Sound.Source.WEATHER, 1.0f, 1.25f);
    final Sound ULTIMATE_SOUND_5 = Sound.sound(Key.key("entity.puffer_fish.blow_up"), Sound.Source.WEATHER, 1.0f, 1.5f);
    final Sound ULTIMATE_SOUND_6 = Sound.sound(Key.key("entity.puffer_fish.blow_up"), Sound.Source.WEATHER, 1.0f, 1.75f);
    final Sound ULTIMATE_SOUND_7 = Sound.sound(Key.key("entity.puffer_fish.blow_up"), Sound.Source.WEATHER, 1.0f, 2.0f);
    final Sound ULTIMATE_SOUND_FINAL = Sound.sound(Key.key("entity.puffer_fish.sting"), Sound.Source.WEATHER, 1.0f, 1.5f);

    PufferFish ultimateFish;
    TextDisplay dangerIcon;
    int ultimateSoundInterval;
    double ultimateDamageRadius = 5.0;
    double ultimateMaxDamage = 6.0;
    double ultimateMaxPoisonDuration = 6.0;
    double firstUltimateDetonateTime = 3.0;

    boolean secondUltimateActive = false;
    double reducedHealth = 6.0;

    public static final ItemStack sword = GUIFormat.customItemName(GUIFormat.unbreakable(Material.WOODEN_SWORD),
            Component.text("Poison Sword").color(TextColor.color(0xd2a54d)).decoration(TextDecoration.ITALIC, false));
    public static double poisonSwordInflictDuration = 1.0;

    {
        abilityDurationSeconds = 0;
        abilityCooldownSeconds = 7;
        ultimateDurationSeconds = firstUltimateDetonateTime;
        ultimateSoundInterval = (int) ultimateDurationSeconds*20/7;
        ultPointsRequired = 5;

        displayName = Component.text("Pufferfish").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false);

        description = new ArrayList<>();
        description.add(Component.text("Primary weapon: Poison Wooden Sword").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.text("Secondary weapon: Bow").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(abilityDivider);
        description.add(Component.text("Throw a pufferfish that can attack a nearby enemy").color(abilityColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(ultimateDivider);
        description.add(Component.text("Spawn a bomb that damages and poisons nearby").color(ultimateColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.text("enemies, but reduces your health to 3 hearts").color(ultimateColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.text("Second ultimate:").decorate(TextDecoration.BOLD).color(ultimateColor)
                        .append(Component.text(" regain full health")).color(ultimateColor)
                .decoration(TextDecoration.ITALIC, false));

        details = new ArrayList<>();
        details.add(detailsDivider);
        details.add(detailsHeader);
        details.add(Component.text("Your ")

                .append(Component.text("ability").color(abilityColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.swapOffhand"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" throws a standard pufferfish that can attack an enemy. Your "))

                .append(Component.text("ultimate").color(ultimateColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.drop"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" will detonate at the activation location after " + TextFormat.formatNumber(ultimateDurationSeconds) + " seconds.")));
        details.add(detailsDivider);

        kit = new ItemStack[]{sword, GUIFormat.unbreakable(Material.BOW), new ItemStack(Material.ARROW, maxArrowAmount)};

    }

    @Override
    public void onDeath() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        secondUltimateActive = false;
        ultimateDurationSeconds = 3.0;

    }

    @Override
    public void onAbilityStart() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        PufferFish fish = (PufferFish) player.getWorld().spawnEntity(player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(0.5)), EntityType.PUFFERFISH);
        fish.setVelocity(player.getEyeLocation().getDirection().multiply(0.5));
        fish.setMetadata("mobSmashKit", new FixedMetadataValue(Main.getInstance(), "pufferfish"));
        fish.setMetadata("source", new FixedMetadataValue(Main.getInstance(), player.getName()));

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

        if (secondUltimateActive) {

            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
            player.setHealth(maxHealth);

            secondUltimateActive = false;
            ultimateDurationSeconds = firstUltimateDetonateTime;

        } else {

            ultimateFish = (PufferFish) player.getWorld().spawnEntity(player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(0.5)), EntityType.PUFFERFISH);
            ultimateFish.setGravity(false);
            ultimateFish.setAI(false);
            ultimateFish.setPuffState(2);
            //ultimateFish.setGlowing(true);
            ultimateFish.setMetadata("mobSmashKit", new FixedMetadataValue(Main.getInstance(), "pufferfish_ultimate"));
            ultimateFish.setMetadata("source", new FixedMetadataValue(Main.getInstance(), ign));

            dangerIcon = (TextDisplay) ultimateFish.getWorld().spawnEntity(ultimateFish.getLocation().clone().add(0, 1, 0), EntityType.TEXT_DISPLAY);
            dangerIcon.text(GUIFormat.DANGER_ICON);
            dangerIcon.setBillboard(Display.Billboard.CENTER);
            dangerIcon.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));

            // only show danger icon to enemy team
            String team = TeamManager.getTeam(player.getName());
            Set<String> teammates = TeamManager.getMembers(team);
            for (String member : teammates) {
                Player teammate = Bukkit.getPlayer(member);
                if (teammate == null) continue;
                teammate.hideEntity(Main.getInstance(), dangerIcon);
            }

            ultimateFish.getWorld().playSound(ULTIMATE_SOUND_1, ultimateFish);

            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(reducedHealth);
            player.setHealth(reducedHealth);

        }

    }

    @Override
    public void ultimateRun() {

        if (!secondUltimateActive) {

            int elapsedTicks = (int) ultimateDurationSeconds * 20 - ultimateDurationRemainingTicks;

            if (elapsedTicks ==     ultimateSoundInterval) ultimateFish.getWorld().playSound(ULTIMATE_SOUND_2, ultimateFish);
            if (elapsedTicks == 2 * ultimateSoundInterval) ultimateFish.getWorld().playSound(ULTIMATE_SOUND_3, ultimateFish);
            if (elapsedTicks == 3 * ultimateSoundInterval) ultimateFish.getWorld().playSound(ULTIMATE_SOUND_4, ultimateFish);
            if (elapsedTicks == 4 * ultimateSoundInterval) ultimateFish.getWorld().playSound(ULTIMATE_SOUND_5, ultimateFish);
            if (elapsedTicks == 5 * ultimateSoundInterval) ultimateFish.getWorld().playSound(ULTIMATE_SOUND_6, ultimateFish);
            if (elapsedTicks == 6 * ultimateSoundInterval) ultimateFish.getWorld().playSound(ULTIMATE_SOUND_7, ultimateFish);

        }

    }

    @Override
    public void onUltimateEnd() {

        if (!secondUltimateActive) {

            ultimateFish.getWorld().playSound(ULTIMATE_SOUND_FINAL, ultimateFish);

            // damage and poison nearby enemies
            Collection<Player> nearbyPlayers = ultimateFish.getLocation().getNearbyPlayers(ultimateDamageRadius);
            for (Player nearbyPlayer : nearbyPlayers) {

                if (!TeamManager.isGamePlayer(nearbyPlayer.getName()) || TeamManager.sameTeam(ign, nearbyPlayer.getName())) continue;

                double distance = ultimateFish.getLocation().distance(nearbyPlayer.getEyeLocation());
                double damage = ultimateMaxDamage / ultimateDamageRadius * Math.max(0, ultimateDamageRadius - distance);
                double duration = ultimateMaxPoisonDuration / ultimateDamageRadius * Math.max(0, ultimateDamageRadius - distance); //seconds

                nearbyPlayer.damage(damage);
                nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.POISON, (int) duration * 20, 0));
                nearbyPlayer.removeMetadata("lastDamager", Main.getInstance());
                nearbyPlayer.setMetadata("lastDamager", new FixedMetadataValue(Main.getInstance(), ign + "'s Pufferfish Bomb"));

            }

            dangerIcon.remove();
            ultimateFish.remove();

            secondUltimateActive = true;
            ultimateDurationSeconds = 0.0;

        }

        if (ultimateDurationRemainingTicks == 0) {

            // a little janky but this will reset health at the end of the round if second ult is active
            Player player = Bukkit.getPlayer(ign);
            if (player == null) return;

            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
            player.setHealth(maxHealth);

            secondUltimateActive = false;
            ultimateDurationSeconds = 3.0;

        }

    }

}
