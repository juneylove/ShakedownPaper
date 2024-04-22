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
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class GhastKit extends MobKit {

    final double fireballSpeed = 0.5;
    int fireballCooldownTicks = 50;
    float flySpeed = 0.03f; // default = 0.1

    ItemStack fireball = GUIFormat.customItemName(Material.FIRE_CHARGE, Component.text("Fireball").color(NamedTextColor.RED));

    final Sound SHOOT_SOUND = Sound.sound(Key.key("entity.ghast.shoot"), Sound.Source.WEATHER, 1.0f, 1.0f);
    final Sound ABILITY_SOUND = Sound.sound(Key.key("entity.ghast.warn"), Sound.Source.WEATHER, 1.0f, 1.0f);
    final Sound ULTIMATE_SOUND = Sound.sound(Key.key("entity.ghast.hurt"), Sound.Source.WEATHER, 5.0f, 1.0f);

    {
        abilityDurationSeconds = 0.05;
        abilityCooldownSeconds = 8;
        ultimateDurationSeconds = 8;
        ultPointsRequired = 5;

        displayName = Component.text("Ghast").color(TextColor.color(0xd8cccd)).decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false);

        description = new ArrayList<>();
        description.add(Component.text("Primary weapon: Fireball").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.text("Passive: Slow Flight").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(abilityDivider);
        description.add(Component.text("Multishot Fireball").color(abilityColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(ultimateDivider);
        description.add(Component.text("Fast Flight").color(ultimateColor)
                .decoration(TextDecoration.ITALIC, false));

        details = new ArrayList<>();
        details.add(detailsDivider);
        details.add(detailsHeader);
        details.add(Component.text("Your ")

                .append(Component.text("ability").color(abilityColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.swapOffhand"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" fires three parallel fireballs to cover a larger area. Your "))

                .append(Component.text("ultimate").color(ultimateColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.drop"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" grants you full flying speed for " + TextFormat.formatNumber(ultimateDurationSeconds) + " seconds.")));
        details.add(detailsDivider);

        fireball = GUIFormat.addLore(fireball, List.of(Component.text("Right click to shoot").color(NamedTextColor.YELLOW)));
        kit = new ItemStack[]{fireball};

    }

    @Override
    public void applyKit() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        player.setAllowFlight(true);
        player.setFlySpeed(flySpeed);

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
            player.setCooldown(Material.FIRE_CHARGE, fireballCooldownTicks);

            Vector direction = player.getEyeLocation().getDirection();
            shootFireball(player.getEyeLocation(), direction);

            player.getWorld().playSound(SHOOT_SOUND, player);

        }

    }

    @Override
    public void onAbilityStart() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        Vector direction = player.getEyeLocation().getDirection();
        // cross product with y axis will be perpendicular, i.e. pointing to the right
        Vector rightOffset = direction.clone().getCrossProduct(new Vector(0, 1, 0)).normalize();
        Vector leftOffset = rightOffset.clone().multiply(-1);

        shootFireball(player.getEyeLocation(), direction);
        shootFireball(player.getEyeLocation().add(rightOffset), direction);
        shootFireball(player.getEyeLocation().add(leftOffset), direction);

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

        player.setFlySpeed(0.1f); // default value

        player.getWorld().playSound(ULTIMATE_SOUND, player);

    }

    @Override
    public void ultimateRun() {}

    @Override
    public void onUltimateEnd() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        player.setFlySpeed(flySpeed);

    }

    // ==========

    private void shootFireball(Location location, Vector direction) {

        Vector velocity = direction.multiply(fireballSpeed);
        Location fireballSpawn = location.add(direction.multiply(1.0)); // spawn 1 block in front of player's looking direction
        Entity fireball = location.getWorld().spawnEntity(fireballSpawn, EntityType.FIREBALL);
        fireball.setMetadata("mobSmashKit", new FixedMetadataValue(Main.getInstance(), "ghast"));
        fireball.setMetadata("source", new FixedMetadataValue(Main.getInstance(), ign));
        fireball.setVelocity(velocity);

    }

}
