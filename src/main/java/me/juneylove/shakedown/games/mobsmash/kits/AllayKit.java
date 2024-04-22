package me.juneylove.shakedown.games.mobsmash.kits;

import com.destroystokyo.paper.ParticleBuilder;
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
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AllayKit extends MobKit {

    double reducedHealth = 8.0;

    AreaEffectCloud cloud;
    float cloudRadius = 3f;

    final Sound ABILITY_SOUND = Sound.sound(Key.key("entity.allay.ambient_with_item"), Sound.Source.WEATHER, 1.0f, 1.0f);
    final Sound ULTIMATE_SOUND = Sound.sound(Key.key("entity.allay.item_given"), Sound.Source.WEATHER, 5.0f, 1.0f);

    {
        abilityDurationSeconds = 7;
        abilityCooldownSeconds = 15;
        ultimateDurationSeconds = 8;
        ultPointsRequired = 5;

        displayName = Component.text("Allay").color(TextColor.color(0x3ce0f6)).decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false);

        description = new ArrayList<>();
        description.add(Component.text("Primary weapon: Stone Axe").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(abilityDivider);
        description.add(Component.text("Heal nearby teammates").color(abilityColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(ultimateDivider);
        description.add(Component.text("Gain flight, but with reduced health").color(ultimateColor)
                .decoration(TextDecoration.ITALIC, false));

        details = new ArrayList<>();
        details.add(detailsDivider);
        details.add(detailsHeader);
        details.add(Component.text("Your ")

                .append(Component.text("ability").color(abilityColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.swapOffhand"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" places down a lingering regeneration cloud for " + TextFormat.formatNumber(abilityDurationSeconds) +
                        " seconds. Your "))

                .append(Component.text("ultimate").color(ultimateColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.drop"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" lets you fly for " + TextFormat.formatNumber(ultimateDurationSeconds)
                        + " seconds, but with a reduced health of " + TextFormat.formatNumber(reducedHealth/2.0) + " hearts.")));
        details.add(detailsDivider);

        kit = new ItemStack[]{GUIFormat.unbreakable(Material.STONE_AXE)};

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

        cloud = (AreaEffectCloud) player.getWorld().spawnEntity(player.getLocation(), EntityType.AREA_EFFECT_CLOUD);
        cloud.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, 40, 1), true);
        cloud.setRadius(cloudRadius);
        cloud.setRadiusOnUse(0.0f);
        cloud.setRadiusPerTick(0.0f);
        cloud.setReapplicationDelay(40);
        cloud.setWaitTime(0);
        cloud.setSource(player);
        cloud.setDuration((int) (abilityDurationSeconds*20));

        // only show cloud to teammates
        String team = TeamManager.getTeam(ign);
        Set<String> members = TeamManager.getMembers(team);
        List<Player> showTo = new ArrayList<>();
        for (String member : members) {
            Player player1 = Bukkit.getPlayer(member);
            if (player1 != null) showTo.add(player1);
        }

        ParticleBuilder builder = new ParticleBuilder(cloud.getParticle());
        builder.receivers(showTo);
        cloud.setParticle(cloud.getParticle(), builder.data());

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

        player.setAllowFlight(true);
        player.setFlying(true);
        player.setVelocity(player.getVelocity().add(new Vector(0.0, 1.0, 0.0)));
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(reducedHealth);

        player.getWorld().playSound(ULTIMATE_SOUND, player);

    }

    @Override
    public void ultimateRun() {}

    @Override
    public void onUltimateEnd() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        player.setAllowFlight(false);
        player.setFlying(false);
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        player.setHealth(maxHealth-6.0);

        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 0));

    }

}
