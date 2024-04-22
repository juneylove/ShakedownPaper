package me.juneylove.shakedown.listeners.eventlisteners;

import me.juneylove.shakedown.control.MatchProgress;
import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.mechanics.Respawn;
import me.juneylove.shakedown.mechanics.abilities.ElytraBoost;
import me.juneylove.shakedown.mechanics.abilities.InstantRiptide;
import me.juneylove.shakedown.mechanics.KitSettings;
import me.juneylove.shakedown.mechanics.LootChests;
import me.juneylove.shakedown.ui.Models;
import me.juneylove.shakedown.ui.GUIFormat;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class PlayerInteractListener implements Listener {

    @EventHandler
    public void onInteractEvent(PlayerInteractEvent event) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        ItemStack itemInHand = event.getItem();
        if (itemInHand != null
            && itemInHand.getType() == GUIFormat.menuSelectItem
            && itemInHand.getItemMeta() != null
            && itemInHand.getItemMeta().hasCustomModelData()) {

            if (itemInHand.getItemMeta().getCustomModelData() == Models.KIT_SELECT.num) {

                if (MatchProgress.kitSelectionIsActive() && game.currentRound.kitSetting instanceof KitSettings.Selection kitSelection) {
                    kitSelection.openKitSelect(event.getPlayer());
                }

            } else if (itemInHand.getItemMeta().getCustomModelData() == Models.ELYTRA_BOOST.num) {

                if (MatchProgress.playIsActive()) {
                    ElytraBoost.boost(event.getPlayer());
                }

            } else if (itemInHand.getItemMeta().getCustomModelData() == Models.INSTANT_RIPTIDE.num) {

                if (MatchProgress.playIsActive()) {
                    InstantRiptide.boost(event.getPlayer());
                }

            }

        } else if (event.getClickedBlock() != null) {

            if (Respawn.IsTempSpec(event.getPlayer().getName())) {
                event.setCancelled(true);
                return;
            }

            Material clicked = event.getClickedBlock().getType();

            if (clicked == Material.FIRE && event.getAction().isLeftClick()) {

                if (game.allowFireExtinguish) {
                    event.getClickedBlock().setType(Material.AIR);
                    Sound extinguish = Sound.sound(Key.key("block.fire.extinguish"), Sound.Source.BLOCK, 1.0f, 1.0f);
                    int x = event.getClickedBlock().getX();
                    int y = event.getClickedBlock().getY();
                    int z = event.getClickedBlock().getZ();
                    event.getPlayer().getWorld().playSound(extinguish, x, y, z);
                }

            } else if (clicked == Material.CHEST && !game.chestInteractsEnabled) {

                event.setCancelled(true);

            } else if (game.shouldLoadLootTables() && LootChests.IsLootChest(clicked)) {

                if (event.getAction() == Action.RIGHT_CLICK_BLOCK && !event.getPlayer().isSneaking()) {

                    event.setCancelled(true);

                    Optional<Location> chestLoc = LootChests.FindCustomChest(event.getPlayer());
                    if (chestLoc.isPresent()) {
                        Location loc = chestLoc.get();
                        LootChests.OpenChest(loc, event.getPlayer());
                    }

                }

            }

        }

        game.onPlayerInteract(event);

    }

}
