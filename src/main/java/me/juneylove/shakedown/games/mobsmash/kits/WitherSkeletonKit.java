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
import org.bukkit.entity.WitherSkull;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;

public class WitherSkeletonKit extends MobKit {

    final Sound ABILITY_SOUND = Sound.sound(Key.key("entity.wither_skeleton.ambient"), Sound.Source.WEATHER, 1.0f, 1.0f);
    final Sound ULTIMATE_SOUND = Sound.sound(Key.key("entity.wither.shoot"), Sound.Source.WEATHER, 5.0f, 1.0f);

    public static ItemStack witherSword = GUIFormat.unbreakable(Material.STONE_SWORD);
    public static double witherDuration = 4;

    public static double ultimateKnockbackRadius = 4.0;
    public static double ultimateMaxKnockbackMultiplier = 0.25;

    {
        abilityDurationSeconds = 6;
        abilityCooldownSeconds = 10;
        ultimateDurationSeconds = 0;
        ultPointsRequired = 5;

        displayName = Component.text("Wither Skeleton").color(TextColor.color(0x353535)).decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false);

        description = new ArrayList<>();
        description.add(Component.text("Primary weapon: Stone Sword").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.text("Passive: Immune to Wither effects").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(abilityDivider);
        description.add(Component.text("Charge your sword with the Wither effect").color(abilityColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(ultimateDivider);
        description.add(Component.text("Launch a Blue Skull with explosive knockback").color(ultimateColor)
                .decoration(TextDecoration.ITALIC, false));

        details = new ArrayList<>();
        details.add(detailsDivider);
        details.add(detailsHeader);
        details.add(Component.text("Your ")

                .append(Component.text("ability").color(abilityColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.swapOffhand"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" applies Wither to attacked enemies for " + TextFormat.formatNumber(abilityDurationSeconds) +
                        " seconds. Your "))

                .append(Component.text("ultimate").color(ultimateColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.drop"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" instantly launches a Blue Skull which flies in a straight line and knocks back enemies.")));
        details.add(detailsDivider);

        kit = new ItemStack[]{GUIFormat.unbreakable(Material.STONE_SWORD)};

        ItemMeta meta = Bukkit.getServer().getItemFactory().getItemMeta(Material.STONE_SWORD);
        meta.displayName(Component.text("Wither Sword").color(NamedTextColor.DARK_GRAY));
        meta.setCustomModelData(1);
        witherSword.setItemMeta(meta);

    }

    @Override
    public void onDeath() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        abilityDurationRemainingTicks = 0;

    }

    @Override
    public void onAbilityStart() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        int slot = player.getInventory().first(Material.STONE_SWORD);
        if (slot == -1) slot = player.getInventory().getHeldItemSlot();

        player.getInventory().setItem(slot, witherSword);

        player.getWorld().playSound(ABILITY_SOUND, player);

    }

    @Override
    public void abilityRun() {}

    @Override
    public void onAbilityEnd() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        int slot = player.getInventory().first(Material.STONE_SWORD);
        if (slot == -1) slot = player.getInventory().getHeldItemSlot();

        player.getInventory().setItem(slot, GUIFormat.unbreakable(Material.STONE_SWORD));

    }

    @Override
    public void onUltimateStart() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        WitherSkull skull = (WitherSkull) player.getWorld().spawnEntity(player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(0.5)), EntityType.WITHER_SKULL);
        skull.setCharged(true);
        skull.setDirection(player.getEyeLocation().getDirection());
        skull.setVelocity(player.getEyeLocation().getDirection().multiply(0.8));
        skull.setMetadata("mobSmashKit", new FixedMetadataValue(Main.getInstance(), "witherskeleton"));
        skull.setMetadata("source", new FixedMetadataValue(Main.getInstance(), ign));

        player.getWorld().playSound(ULTIMATE_SOUND, player);

    }

    @Override
    public void ultimateRun() {}

    @Override
    public void onUltimateEnd() {}

}
