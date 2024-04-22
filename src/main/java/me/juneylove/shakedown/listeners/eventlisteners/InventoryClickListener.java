package me.juneylove.shakedown.listeners.eventlisteners;

import me.juneylove.shakedown.control.MatchProgress;
import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.mechanics.KitSettings;
import me.juneylove.shakedown.ui.Models;
import me.juneylove.shakedown.ui.GUIFormat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class InventoryClickListener implements Listener {

    @EventHandler
    public void onInventoryEvent(InventoryClickEvent event) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        game.onPlayerInventoryClick(event);

        if (MatchProgress.kitSelectionIsActive()) {
            if (game.currentRound.kitSetting instanceof KitSettings.Selection selection) {

                ItemStack item = event.getCurrentItem();
                if (item != null
                    && item.getType() == GUIFormat.menuSelectItem
                    && item.hasItemMeta()
                    && item.getItemMeta().getCustomModelData() == Models.KIT_SELECT.num) {

                    selection.openKitSelect((Player) event.getWhoClicked());

                } else {

                    if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.CHEST) {
                        selection.onKitSelectClick((Player) event.getWhoClicked(), event.getSlot());
                    }

                }

                event.setCancelled(true);

            }
            return;
        }

        if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {

            for (ItemStack stack : Objects.requireNonNull(event.getClickedInventory()).getContents()) {
                if (Objects.requireNonNull(event.getCurrentItem()).getType() == stack.getType()) {
                    Objects.requireNonNull(stack.getItemMeta()).setAttributeModifiers(null);
                }
            }

        }

        ItemStack stack = event.getCurrentItem();
        if (stack != null && Objects.requireNonNull(event.getClickedInventory()).getType() == InventoryType.CHEST) {
            stack.setItemMeta(null);
        }

        if (!game.moveItemsEnabled) {
            event.setCancelled(true);
            return;
        }

        InventoryAction type = event.getAction();

        switch (type) {

            case DROP_ALL_CURSOR:
            case DROP_ALL_SLOT:
            case DROP_ONE_CURSOR:
            case DROP_ONE_SLOT:

                if (!game.itemDropsEnabled) {
                    event.setCancelled(true);
                }

        }

    }

}
