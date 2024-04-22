package me.juneylove.shakedown.games.mobsmash.kits;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import me.juneylove.shakedown.games.mobsmash.BowKit;
import me.juneylove.shakedown.scoring.TeamManager;
import me.juneylove.shakedown.ui.GUIFormat;
import me.juneylove.shakedown.ui.Models;
import me.juneylove.shakedown.ui.TextFormat;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

public class ShulkerKit extends BowKit {

    boolean bowEquipped = false;

    float defaultWalkSpeed = 0.2f;

    ItemStack blankItem = GUIFormat.customItemName(new ItemStack(Material.BARRIER), Component.text("Use your ability to shoot"));
    ItemStack switchingItem = GUIFormat.customItemName(GUIFormat.getMenuIcon(Models.BLANK), Component.text("Switching..."));
    ItemStack bow = GUIFormat.unbreakable(Material.BOW);

    double levitateRadius = 4.0;
    double levitateDuration = 5.0;

    final Sound ABILITY_SOUND_1 = Sound.sound(Key.key("entity.shulker.open"), Sound.Source.WEATHER, 1.0f, 1.0f);
    final Sound ABILITY_SOUND_2 = Sound.sound(Key.key("entity.shulker.close"), Sound.Source.WEATHER, 1.0f, 1.0f);
    final Sound ULTIMATE_SOUND = Sound.sound(Key.key("entity.shulker.shoot"), Sound.Source.WEATHER, 5.0f, 1.0f);

    {
        abilityDurationSeconds = 0.4;
        abilityCooldownSeconds = 2;
        ultimateDurationSeconds = 0;
        ultPointsRequired = 5;

        displayName = Component.text("Shulker").color(TextColor.color(0x966996)).decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false);

        description = new ArrayList<>();
        description.add(Component.text("Primary weapon: Power Bow").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.text("Passive: Can only shoot while stationary").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(abilityDivider);
        description.add(Component.text("Switch between moving and shooting").color(abilityColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(ultimateDivider);
        description.add(Component.text("Levitate surrounding enemies").color(ultimateColor)
                .decoration(TextDecoration.ITALIC, false));

        details = new ArrayList<>();
        details.add(detailsDivider);
        details.add(detailsHeader);
        details.add(Component.text("Your ")

                .append(Component.text("ability").color(abilityColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.swapOffhand"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" lets you switch between moving and shooting, and takes "
                        + TextFormat.formatNumber(abilityDurationSeconds) + " seconds to switch. Your "))

                .append(Component.text("ultimate").color(ultimateColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.drop"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" levitates nearby enemies for " + TextFormat.formatNumber(levitateDuration) + " seconds.")));
        details.add(detailsDivider);

        bow.addEnchantment(Enchantment.ARROW_DAMAGE, 1);

        kit = new ItemStack[]{blankItem, new ItemStack(Material.ARROW, maxArrowAmount)};

    }

    @Override
    public void onDeath() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        bowEquipped = false;
        abilityDurationRemainingTicks = 0;

    }

    @Override
    public void applyKit() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        bowEquipped = false;

    }

    @Override
    public void onJump(PlayerJumpEvent event) {

        // do not allow player to jump while bow is equipped, as long as we've finished switching to bow
        if (bowEquipped && abilityDurationRemainingTicks == 0) event.setCancelled(true);


    }

    @Override
    public void onAbilityStart() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        int slot = findWeaponSlot();

        bowEquipped = !bowEquipped;

        player.getInventory().setItem(slot, switchingItem);
        player.setCooldown(GUIFormat.menuSelectItem, (int) abilityDurationSeconds*20);

        // play close sound when bow is unequipped
        if (!bowEquipped) {
            player.getWorld().playSound(ABILITY_SOUND_2, player);
        }

    }

    @Override
    public void abilityRun() {}

    @Override
    public void onAbilityEnd() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        int slot = findWeaponSlot();

        if (bowEquipped) {

            player.getInventory().setItem(slot, bow);
            player.setWalkSpeed(0.0f);

        } else {
            player.getInventory().setItem(slot, blankItem);
            player.setWalkSpeed(defaultWalkSpeed);
        }

        // play open sound when bow is equipped
        if (bowEquipped) {
            player.getWorld().playSound(ABILITY_SOUND_1, player);
        }

        // triggers at end of round
        if (abilityDurationRemainingTicks == 0) {

            player.getInventory().setItem(slot, blankItem);
            player.setWalkSpeed(defaultWalkSpeed);
            bowEquipped = false;

        }

    }

    @Override
    public void onUltimateStart() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        // levitate nearby enemies
        Collection<Player> nearbyPlayers = player.getLocation().getNearbyPlayers(levitateRadius);
        for (Player nearbyPlayer : nearbyPlayers) {

            if (!TeamManager.isGamePlayer(nearbyPlayer.getName()) || TeamManager.sameTeam(ign, nearbyPlayer.getName())) continue;

            double distance = player.getLocation().distance(nearbyPlayer.getEyeLocation());
            if (distance > levitateRadius) continue; // creates a spherical effect instead of cube

            nearbyPlayer.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, (int) levitateDuration * 20, 0));

        }

        player.getWorld().playSound(ULTIMATE_SOUND, player);

    }

    @Override
    public void ultimateRun() {}

    @Override
    public void onUltimateEnd() {}

    // ==========

    public int findWeaponSlot() {

        int slot;

        slot = Objects.requireNonNull(Bukkit.getPlayer(ign)).getInventory().first(Material.BOW);

        if (slot == -1) {
            slot = Objects.requireNonNull(Bukkit.getPlayer(ign)).getInventory().first(Material.BARRIER);
        }

        if (slot == -1) {
            slot = Objects.requireNonNull(Bukkit.getPlayer(ign)).getInventory().first(GUIFormat.menuSelectItem);
        }

        if (slot == -1) {

            // first hotbar slot if not found for whatever reason, but don't replace existing item (i.e. arrows) if they're in that slot
            if (Objects.requireNonNull(Bukkit.getPlayer(ign)).getInventory().getItem(36) != null) {
                slot = 36;
            } else {
                slot = 37;
            }

        }

        return slot;

    }

}
