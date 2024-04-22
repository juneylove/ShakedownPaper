package me.juneylove.shakedown.games.mobsmash.kits;

import me.juneylove.shakedown.Main;
import me.juneylove.shakedown.games.mobsmash.MobKit;
import me.juneylove.shakedown.mechanics.Respawn;
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
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vex;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.*;

public class EvokerKit extends MobKit {

    Random random = new Random();

    final Sound ABILITY_SOUND = Sound.sound(Key.key("entity.evoker.cast_spell"), Sound.Source.WEATHER, 1.0f, 1.0f);
    final Sound ULTIMATE_SOUND = Sound.sound(Key.key("item.totem.use"), Sound.Source.WEATHER, 5.0f, 1.0f);

    {
        abilityDurationSeconds = 10;
        abilityCooldownSeconds = 15;
        ultimateDurationSeconds = 0;
        ultPointsRequired = 5;

        displayName = Component.text("Evoker").color(TextColor.color(0x32663c)).decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false);

        description = new ArrayList<>();
        description.add(Component.text("Primary weapon: Stone Axe").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(abilityDivider);
        description.add(Component.text("Summon Vexes").color(abilityColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(ultimateDivider);
        description.add(Component.text("Resurrect a dead teammate").color(ultimateColor)
                .decoration(TextDecoration.ITALIC, false));

        details = new ArrayList<>();
        details.add(detailsDivider);
        details.add(detailsHeader);
        details.add(Component.text("Your ")

                .append(Component.text("ability").color(abilityColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.swapOffhand"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" summons two vexes to attack enemies. Your "))

                .append(Component.text("ultimate").color(ultimateColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.drop"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" lets you resurrect a teammate who is waiting to respawn. If multiple teammates are dead, one will be selected randomly.")));
        details.add(detailsDivider);

        kit = new ItemStack[]{GUIFormat.unbreakable(Material.STONE_AXE)};

    }

    @Override
    public void onAbilityStart() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        Vector direction1 = player.getEyeLocation().getDirection().rotateAroundY(0.5).setY(0.1);
        Vex vex1 = (Vex) player.getWorld().spawnEntity(player.getEyeLocation().add(direction1), EntityType.VEX);
        vex1.setMetadata("source", new FixedMetadataValue(Main.getInstance(), ign));
        vex1.setMetadata("mobSmashKit", new FixedMetadataValue(Main.getInstance(), "evoker"));
        vex1.setLimitedLifetime(true);
        vex1.setLimitedLifetimeTicks((int) (abilityDurationSeconds*20));

        Vector direction2 = player.getEyeLocation().getDirection().rotateAroundY(-0.5).setY(0.1);
        Vex vex2 = (Vex) player.getWorld().spawnEntity(player.getEyeLocation().add(direction2), EntityType.VEX);
        vex2.setMetadata("source", new FixedMetadataValue(Main.getInstance(), ign));
        vex2.setMetadata("mobSmashKit", new FixedMetadataValue(Main.getInstance(), "evoker"));
        vex2.setLimitedLifetime(true);
        vex2.setLimitedLifetimeTicks((int) (abilityDurationSeconds*20));

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

        if (Respawn.TempSpecCount(TeamManager.getTeam(ign)) == 0) {

            // this should not happen! defensive
            // reject ultimate trigger and leave it still ready to use
            currentUltPoints = ultPointsRequired;
            ultimateDurationRemainingTicks = 0;
            return;

        }

        // alright it's resurrect time bois - TODO: TEST
        List<String> tempSpecTeammates = Respawn.TempSpecMembers(TeamManager.getTeam(ign));
        int resurrectIndex = random.nextInt(0, tempSpecTeammates.size());
        String resurrectIgn = tempSpecTeammates.get(resurrectIndex);

        Player toResurrect = Bukkit.getPlayer(resurrectIgn);
        Location priorSpawn = Objects.requireNonNull(toResurrect).getBedSpawnLocation();
        toResurrect.setBedSpawnLocation(player.getLocation());
        Respawn.RespawnPlayer(toResurrect);
        toResurrect.setBedSpawnLocation(priorSpawn);

        toResurrect.getWorld().playSound(ULTIMATE_SOUND, toResurrect);

    }

    @Override
    public void ultimateRun() {}

    @Override
    public void onUltimateEnd() {}

}
