package me.juneylove.shakedown.games.mobsmash.kits;

import me.juneylove.shakedown.games.mobsmash.BowKit;
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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

public class PiglinKit extends BowKit {

    int swordSlot;
    int crossbowSlot;

    Material meleeWeapon = Material.GOLDEN_SWORD;
    ItemStack sword = GUIFormat.unbreakable(meleeWeapon);
    ItemStack crossbow = GUIFormat.unbreakable(Material.CROSSBOW);
    ItemStack ultimateAxe = GUIFormat.unbreakable(Material.GOLDEN_AXE);

    final Sound ABILITY_SOUND = Sound.sound(Key.key("entity.piglin.angry"), Sound.Source.WEATHER, 1.0f, 1.0f);
    final Sound ULTIMATE_SOUND = Sound.sound(Key.key("entity.piglin_brute.angry"), Sound.Source.WEATHER, 5.0f, 1.0f);

    {
        abilityDurationSeconds = 4;
        abilityCooldownSeconds = 12;
        ultimateDurationSeconds = 8;
        ultPointsRequired = 5;

        maxArrowAmount = 2;

        displayName = Component.text("Piglin").color(TextColor.color(0xe9a075)).decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false);

        description = new ArrayList<>();
        description.add(Component.text("Primary weapon: Gold Sword").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.text("Secondary weapon: Crossbow").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(abilityDivider);
        description.add(Component.text("Gain a brief speed boost").color(abilityColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(ultimateDivider);
        description.add(Component.text("Become a Brute with a sharpened axe and Absorption").color(ultimateColor)
                .decoration(TextDecoration.ITALIC, false));

        details = new ArrayList<>();
        details.add(detailsDivider);
        details.add(detailsHeader);
        details.add(Component.text("Your ")

                .append(Component.text("ability").color(abilityColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.swapOffhand"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" grants speed for " + TextFormat.formatNumber(abilityDurationSeconds) +
                        " seconds. Your "))

                .append(Component.text("ultimate").color(ultimateColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.drop"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" gives Absorption and replaces both weapons with a gold axe for "
                        + TextFormat.formatNumber(ultimateDurationSeconds) + " seconds.")));
        details.add(detailsDivider);

        kit = new ItemStack[]{sword, crossbow, new ItemStack(Material.ARROW, maxArrowAmount)};

        ultimateAxe.addEnchantment(Enchantment.DAMAGE_ALL, 2);

    }

    @Override
    public void onDeath() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        player.removePotionEffect(PotionEffectType.SPEED);
        abilityDurationRemainingTicks = 0;
        ultimateDurationRemainingTicks = 0;

    }

    @Override
    public void onAbilityStart() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        player.getWorld().playSound(ABILITY_SOUND, player);

        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (int) abilityDurationSeconds*20, 1));

    }

    @Override
    public void abilityRun() {}

    @Override
    public void onAbilityEnd() {}

    @Override
    public void onUltimateStart() {

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
            swordSlot = player.getInventory().first(sword);
            crossbowSlot = player.getInventory().first(crossbow);
            player.getInventory().setItem(swordSlot, ultimateAxe);

        } else {

            // dropped item was not stackable (weapon) - normally, replace it with ultimate axe -
            // but there's one other edge case: ctrl-dropping the full stack of arrows would also come here;
            // so instead of just replacing the held item with the ultimate crossbow, we'll clear the held
            // item then check the inventory to see what to do
            player.getInventory().setItem(heldSlot, null);

            if (player.getInventory().contains(meleeWeapon) && player.getInventory().contains(Material.CROSSBOW)) {

                // player still has weapon (this is the ctrl-drop arrows edge case)
                swordSlot = player.getInventory().first(sword);
                crossbowSlot = player.getInventory().first(crossbow);

                player.getInventory().setItem(swordSlot, ultimateAxe);
                player.getInventory().setItem(crossbowSlot, null);
                // don't need to replace arrows (like the next case), because the player will
                // pick them back up because the drop event was cancelled

            } else if (player.getInventory().contains(meleeWeapon)) {

                // dropped crossbow - give ultimate axe in held slot, clear sword
                swordSlot = player.getInventory().first(sword);
                crossbowSlot = player.getInventory().getHeldItemSlot();

                player.getInventory().setItem(swordSlot, null);
                player.getInventory().setItem(crossbowSlot, ultimateAxe);

            } else {

                // dropped sword - give ultimate axe in held slot, clear crossbow
                swordSlot = player.getInventory().getHeldItemSlot();
                crossbowSlot = player.getInventory().first(crossbow);

                player.getInventory().setItem(swordSlot, ultimateAxe);
                player.getInventory().setItem(crossbowSlot, null);

            }

        }

        player.getWorld().playSound(ULTIMATE_SOUND, player);

        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, (int) ultimateDurationSeconds*20, 0));

    }

    @Override
    public void ultimateRun() {

        // this is necessary because, after dropping an item to trigger the ultimate, the
        // player will pick that item back up one tick later, so we have to get rid of it
        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        if (player.getInventory().contains(sword)) {
            player.getInventory().remove(sword);
        }
        if (player.getInventory().contains(crossbow)) {
            player.getInventory().remove(crossbow);
        }

    }

    @Override
    public void onUltimateEnd() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        player.getInventory().remove(Material.GOLDEN_AXE);
        player.getInventory().setItem(swordSlot, sword);
        player.getInventory().setItem(crossbowSlot, crossbow);

    }

}
