package me.juneylove.shakedown.games.mobsmash.kits;

import me.juneylove.shakedown.Main;
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
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class ZombieKit extends MobKit {

    final Sound ABILITY_SOUND = Sound.sound(Key.key("entity.zombie.ambient"), Sound.Source.WEATHER, 1.0f, 1.0f);
    final Sound ULTIMATE_SOUND = Sound.sound(Key.key("entity.zombie.break_wooden_door"), Sound.Source.WEATHER, 5.0f, 1.0f);

    {
        abilityDurationSeconds = 0.05;
        abilityCooldownSeconds = 10;
        ultimateDurationSeconds = 8;
        ultPointsRequired = 5;

        displayName = Component.text("Zombie").color(TextColor.color(0x487134)).decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false);

        description = new ArrayList<>();
        description.add(Component.text("Primary weapon: Stone Sword").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(abilityDivider);
        description.add(Component.text("Spawn reinforcements").color(abilityColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(ultimateDivider);
        description.add(Component.text("Turn killed enemies into zombies").color(ultimateColor)
                .decoration(TextDecoration.ITALIC, false));

        details = new ArrayList<>();
        details.add(detailsDivider);
        details.add(detailsHeader);
        details.add(Component.text("Your ")

                .append(Component.text("ability").color(abilityColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.swapOffhand"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" will spawn 2 zombies in front of you that will attack enemies. Your "))

                .append(Component.text("ultimate").color(ultimateColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.drop"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" enables you to turn killed enemies into zombies for " + TextFormat.formatNumber(ultimateDurationSeconds) + " seconds.")));
        details.add(detailsDivider);

        kit = new ItemStack[]{GUIFormat.unbreakable(Material.STONE_SWORD)};

    }

    @Override
    public void onDeath() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        ultimateDurationRemainingTicks = 0;

    }

    @Override
    public void onAbilityStart() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        Vector direction1 = player.getEyeLocation().getDirection().rotateAroundY(0.5).setY(0.1);
        Zombie zombie1 = (Zombie) player.getWorld().spawnEntity(player.getLocation().add(direction1), EntityType.ZOMBIE);
        zombie1.setMetadata("source", new FixedMetadataValue(Main.getInstance(), ign));
        zombie1.setMetadata("mobSmashKit", new FixedMetadataValue(Main.getInstance(), "zombie"));
        zombie1.setShouldBurnInDay(false);
        zombie1.setHealth(10.0);

        Vector direction2 = player.getEyeLocation().getDirection().rotateAroundY(-0.5).setY(0.1);
        Zombie zombie2 = (Zombie) player.getWorld().spawnEntity(player.getLocation().add(direction2), EntityType.ZOMBIE);
        zombie2.setMetadata("source", new FixedMetadataValue(Main.getInstance(), ign));
        zombie2.setMetadata("mobSmashKit", new FixedMetadataValue(Main.getInstance(), "zombie"));
        zombie2.setShouldBurnInDay(false);
        zombie2.setHealth(10.0);

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
