package me.juneylove.shakedown.listeners.eventlisteners;

import com.google.common.collect.Multimap;
import me.juneylove.shakedown.control.MatchProgress;
import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.mechanics.KitSettings;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.Objects;

public class InventoryCloseListener implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        if (MatchProgress.kitSelectionIsActive()) {
            if (game.currentRound.kitSetting instanceof KitSettings.Selection selection) {
                if (event.getInventory().getType() != InventoryType.PLAYER) {
                    selection.onKitSelectClose((Player) event.getPlayer());
                }
            }
        }

        if (game.shouldLoadLootTables() && event.getInventory().getType() == InventoryType.CHEST) {

            ItemStack[] contents = event.getInventory().getContents();
            for (ItemStack stack : contents) {
                if (stack != null) {

                    World world = event.getPlayer().getWorld();
                    Location dropLocation = DecodeItemMeta(stack, world);

                    if (dropLocation.length() > 0.1) {

                        //noinspection ConstantConditions
                        ItemMeta meta = stack.getItemMeta().clone();
                        meta.setAttributeModifiers(null);
                        stack.setItemMeta(meta);
                        world.dropItem(dropLocation, stack);

                    }

                }
            }

        }

        game.onPlayerInventoryClose(event);

    }

    private Location DecodeItemMeta(ItemStack stack, World world) {

        Multimap<Attribute, AttributeModifier> modifiers = Objects.requireNonNull(stack.getItemMeta()).getAttributeModifiers();

        double x = 0, y = 0, z = 0;

        if (modifiers != null) {

            Collection<AttributeModifier> followRange = modifiers.get(Attribute.GENERIC_FOLLOW_RANGE);
            for (AttributeModifier modifier : followRange) {
                x = Double.parseDouble(modifier.getName());
            }

            Collection<AttributeModifier> movementSpeed = modifiers.get(Attribute.GENERIC_MOVEMENT_SPEED);
            for (AttributeModifier modifier : movementSpeed) {
                y = Double.parseDouble(modifier.getName());
            }

            Collection<AttributeModifier> attackDamage = modifiers.get(Attribute.GENERIC_ATTACK_DAMAGE);
            for (AttributeModifier modifier : attackDamage) {
                z = Double.parseDouble(modifier.getName());
            }

        }

        return new Location(world, x, y, z).add(0.5, 0.5, 0.5);

    }

}
