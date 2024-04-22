package me.juneylove.shakedown.games.mobsmash.kits;

import me.juneylove.shakedown.games.mobsmash.BowKit;
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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Objects;

public class PillagerKit extends BowKit {

    boolean crossbowEquipped = false;
    ItemStack switchToReload = GUIFormat.customItemName(GUIFormat.getMenuIcon(Models.ARROW0), Component.text("Equip crossbow to reload").color(NamedTextColor.GOLD));

    Material meleeWeapon = Material.IRON_AXE;
    ItemStack axe = GUIFormat.unbreakable(meleeWeapon);
    ItemStack crossbow = GUIFormat.unbreakable(Material.CROSSBOW);
    ItemStack ultimateCrossbow = GUIFormat.unbreakable(Material.CROSSBOW);
    ItemStack switchingItem = GUIFormat.customItemName(GUIFormat.getMenuIcon(Models.BLANK), Component.text("Switching..."));

    final Sound ABILITY_SOUND = Sound.sound(Key.key("entity.pillager.ambient"), Sound.Source.WEATHER, 1.0f, 1.0f);
    final Sound ULTIMATE_SOUND = Sound.sound(Key.key("entity.pillager.celebrate"), Sound.Source.WEATHER, 5.0f, 1.0f);

    {
        abilityDurationSeconds = 1.5;
        abilityCooldownSeconds = 4;
        ultimateDurationSeconds = 6;
        ultPointsRequired = 5;

        displayName = Component.text("Pillager").color(TextColor.color(0x335466)).decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false);

        description = new ArrayList<>();
        description.add(Component.text("Primary weapon: Iron Axe").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.text("Secondary weapon: Crossbow").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(abilityDivider);
        description.add(Component.text("Switch between weapons, using one at a time").color(abilityColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(ultimateDivider);
        description.add(Component.text("Add Quick Charge and Piercing to your crossbow").color(ultimateColor)
                .decoration(TextDecoration.ITALIC, false));

        details = new ArrayList<>();
        details.add(detailsDivider);
        details.add(detailsHeader);
        details.add(Component.text("Your ")

                .append(Component.text("ability").color(abilityColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.swapOffhand"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" takes " + TextFormat.formatNumber(abilityDurationSeconds) +
                        " seconds to switch, and has a cooldown of " + TextFormat.formatNumber(abilityCooldownSeconds) + " seconds. Your "))

                .append(Component.text("ultimate").color(ultimateColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.drop"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" gives an enchanted crossbow for " + TextFormat.formatNumber(ultimateDurationSeconds) +
                        " seconds, but cannot be activated while switching weapons.")));
        details.add(detailsDivider);

        kit = new ItemStack[]{axe, new ItemStack(Material.ARROW, maxArrowAmount)};

        ultimateCrossbow.addEnchantment(Enchantment.QUICK_CHARGE, 2);
        ultimateCrossbow.addEnchantment(Enchantment.PIERCING, 2);

    }

    @Override
    public void onDeath() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        abilityDurationRemainingTicks = 0;
        ultimateDurationRemainingTicks = 0;

    }

    @Override
    public void applyKit() {
        crossbowEquipped = false;
    }

    @Override
    public void onAbilityStart() {

        // prevent activating ability if ultimate is active for this kit
        if (ultimateDurationRemainingTicks > 0) {
            abilityDurationRemainingTicks = 0; // will have already been set to full in AbilityManager, so reset here
            return;
        }

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        int weaponSlot = findWeaponSlot();

        crossbowEquipped = !crossbowEquipped;

        player.getInventory().setItem(weaponSlot, switchingItem);
        player.setCooldown(GUIFormat.menuSelectItem, (int) abilityDurationSeconds*20);

        if (!crossbowEquipped) {
            if (player.getInventory().contains(GUIFormat.getMenuIcon(Models.ARROW0))) {
                int slot = player.getInventory().first(GUIFormat.getMenuIcon(Models.ARROW0));
                player.getInventory().setItem(slot, switchToReload);
            }
        }

        player.getWorld().playSound(ABILITY_SOUND, player);

    }

    @Override
    public void abilityRun() {}

    @Override
    public void onAbilityEnd() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        int weaponSlot = findWeaponSlot();

        if (crossbowEquipped) {

            player.getInventory().setItem(weaponSlot, crossbow);
            if (player.getInventory().contains(switchToReload)) {
                int slot = player.getInventory().first(switchToReload);
                player.getInventory().setItem(slot, GUIFormat.getMenuIcon(Models.ARROW0));
            }

        } else {
            player.getInventory().setItem(weaponSlot, axe);
        }

    }

    @Override
    public void onUltimateStart() {

        // prevent activating ultimate if ability is active (currently switching weapons)
        if (abilityDurationRemainingTicks > 0) {
            currentUltPoints = ultPointsRequired; // this will have already been the case before triggering
            ultimateDurationRemainingTicks = 0; // will have already been set to full in AbilityManager, so reset here
            return;
        }

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        // use held slot instead of "weapon slot" because a dropped item is not technically in
        // the inventory (ultimate is triggered by dropping an item), even if dropping the item
        // is cancelled; so instead this will tell us whether the dropped item was stackable
        // i.e. a weapon)
        int heldSlot = player.getInventory().getHeldItemSlot();

        if (player.getInventory().getItem(heldSlot) != null) {

            // still holding item, which means the item dropped to trigger ultimate was stackable (arrows) -
            // find main weapon and replace that
            int weaponSlot = findWeaponSlot();
            player.getInventory().setItem(weaponSlot, ultimateCrossbow);

        } else {

            // dropped item was not stackable (weapon) - normally, replace it with ultimate crossbow -
            // but there's one other edge case: ctrl-dropping the full stack of arrows would also come here;
            // so instead of just replacing the held item with the ultimate crossbow, we'll clear the held
            // item then check the inventory to see what to do
            player.getInventory().setItem(heldSlot, null);

            if (player.getInventory().contains(meleeWeapon) || player.getInventory().contains(Material.CROSSBOW)) {

                // player still has weapon (this is the ctrl-drop arrows edge case)
                int weaponSlot = findWeaponSlot();
                player.getInventory().setItem(weaponSlot, ultimateCrossbow);
                // don't need to replace arrows (like the next case), because the player will
                // pick them back up because the drop event was cancelled

            } else {

                // player does not have weapon - the held slot (which we cleared) should be the weapon slot
                player.getInventory().setItem(heldSlot, ultimateCrossbow);

            }

        }

        // reload arrows on ultimate start but with no cooldown
        super.startReload();
        player.setCooldown(Material.CROSSBOW, 0);
        player.clearTitle(); // clear "reloading..."

        player.getWorld().playSound(ULTIMATE_SOUND, player);

    }

    @Override
    public void ultimateRun() {

        // this is necessary because, after dropping an item to trigger the ultimate, the
        // player will pick that item back up one tick later, so we have to get rid of it
        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        if (player.getInventory().contains(axe)) {
            player.getInventory().remove(axe);
        }
        if (player.getInventory().contains(crossbow)) {
            player.getInventory().remove(crossbow);
        }

    }

    @Override
    public void onUltimateEnd() {

        // same function as ability end - go back to whatever weapon was equipped
        onAbilityEnd();

    }

    // ==========

    @Override // overrides RangeKit
    public void onUseItem(PlayerInteractEvent event) {

        if (!crossbowEquipped) return;
        super.onUseItem(event);

    }

    @Override // overrides RangeKit
    public void onFireArrow() {

        super.onFireArrow();

        if (!crossbowEquipped) {

            Player player = Bukkit.getPlayer(ign);
            if (player == null) return;

            if (player.getInventory().contains(GUIFormat.getMenuIcon(Models.ARROW0))) {
                int slot = player.getInventory().first(GUIFormat.getMenuIcon(Models.ARROW0));
                player.getInventory().setItem(slot, switchToReload);
            }

        }

    }

    // ==========

    public int findWeaponSlot() {

        int slot;

        if (crossbowEquipped ||
            Objects.requireNonNull(Bukkit.getPlayer(ign)).getInventory().contains(Material.CROSSBOW)) {
            slot = Objects.requireNonNull(Bukkit.getPlayer(ign)).getInventory().first(Material.CROSSBOW);
        } else {
            slot = Objects.requireNonNull(Bukkit.getPlayer(ign)).getInventory().first(meleeWeapon);
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
