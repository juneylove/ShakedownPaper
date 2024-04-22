package me.juneylove.shakedown.listeners.eventlisteners;

import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.mechanics.LootChests;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;

public class StructureGrowListener implements Listener {

    @EventHandler
    public void onGrowEvent(StructureGrowEvent event) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        Material type = event.getWorld().getBlockAt(event.getLocation()).getType();

        if (LootChests.IsLootChest(type)) {
            event.setCancelled(true);
        }

    }

}
