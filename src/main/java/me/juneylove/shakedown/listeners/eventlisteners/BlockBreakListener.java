package me.juneylove.shakedown.listeners.eventlisteners;

import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.mechanics.LootChests;
import me.juneylove.shakedown.mechanics.Respawn;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockBreakListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        if (Respawn.IsTempSpec(event.getPlayer().getName())) {
            event.setCancelled(true);
            return;
        }

        Material type = event.getBlock().getType();

        if (CheckBlockTypeForCancel(game, type)) {
            event.setCancelled(true);
            return;
        }

        if (!game.blockDropsEnabled) {
            event.setDropItems(false);
            return;
        }

        // Disable block drops for concrete specifically if infinite concrete is enabled
        if (game.infiniteConcrete) {
            if (type.name().contains("CONCRETE") && !type.name().contains("POWDER")) {
                event.setDropItems(false);
            }
        }

        if (!event.isCancelled()) {
            Games.CURRENT_GAME.onBlockBreak(event.getPlayer(), event.getBlock());
        }

    }

    protected static boolean CheckBlockTypeForCancel(GameSetting game, Material type) {

        if (game.shouldLoadLootTables() && LootChests.IsLootChest(type)) {
            return true;
        }

        switch (type) {

            case LILY_PAD:

                if (game.shouldLoadLootTables()) {
                    return true;
                }
                break;

            case TNT:

                if (!game.allowTntPlace) {
                    return true;
                }
                break;

            case BLACK_CONCRETE:
            case BLUE_CONCRETE:
            case BROWN_CONCRETE:
            case CYAN_CONCRETE:
            case GRAY_CONCRETE:
            case GREEN_CONCRETE:
            case LIGHT_BLUE_CONCRETE:
            case LIGHT_GRAY_CONCRETE:
            case LIME_CONCRETE:
            case MAGENTA_CONCRETE:
            case ORANGE_CONCRETE:
            case PINK_CONCRETE:
            case PURPLE_CONCRETE:
            case RED_CONCRETE:
            case WHITE_CONCRETE:
            case YELLOW_CONCRETE:

                if (!game.allowConcretePlace) {
                    return true;
                }
                break;

            case COBWEB:

                if (!game.allowCobwebPlace) {
                    return true;
                }
                break;

            default:

                if (!game.allowOtherBlockPlace) {
                    return true;
                }

        }
        return false;

    }

}
