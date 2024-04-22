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
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class VexKit extends MobKit {

    double reducedHealth = 8.0;
    double lungeSpeed = 1.5;

    final Sound ABILITY_SOUND = Sound.sound(Key.key("entity.vex.charge"), Sound.Source.WEATHER, 1.0f, 1.0f);
    final Sound ULTIMATE_SOUND = Sound.sound(Key.key("entity.vex.ambient"), Sound.Source.WEATHER, 5.0f, 1.0f);

    {
        abilityDurationSeconds = 2;
        abilityCooldownSeconds = 8;
        ultimateDurationSeconds = 8;
        ultPointsRequired = 5;

        displayName = Component.text("Vex").color(TextColor.color(0x617383)).decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false);

        description = new ArrayList<>();
        description.add(Component.text("Primary weapon: Stone Sword").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(abilityDivider);
        description.add(Component.text("Lunge forward and knock back enemies").color(abilityColor)
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

                .append(Component.text(" lunges forward and gives your sword Knockback for " + TextFormat.formatNumber(abilityDurationSeconds) +
                        " seconds. Your "))

                .append(Component.text("ultimate").color(ultimateColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.drop"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" lets you fly for " + TextFormat.formatNumber(ultimateDurationSeconds)
                        + " seconds, but with a reduced health of " + TextFormat.formatNumber(reducedHealth/2.0) + " hearts.")));
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

        Vector newVelocity = player.getVelocity().add(player.getLocation().getDirection().multiply(lungeSpeed));
        player.setVelocity(newVelocity.setY(0.25)); // set y constant so looking up/down doesn't really affect range

        int slot = player.getInventory().getHeldItemSlot();
        ItemStack sword = player.getInventory().getItem(slot);
        if (sword != null) {
            sword.addEnchantment(Enchantment.KNOCKBACK, 1);
        }

        player.getWorld().playSound(ABILITY_SOUND, player);

    }

    @Override
    public void abilityRun() {}

    @Override
    public void onAbilityEnd() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        int slot = player.getInventory().first(Material.STONE_SWORD);
        ItemStack sword = player.getInventory().getItem(slot);
        if (sword != null) {
            sword.removeEnchantment(Enchantment.KNOCKBACK);
        }
    }

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
