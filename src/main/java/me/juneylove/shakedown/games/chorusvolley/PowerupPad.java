package me.juneylove.shakedown.games.chorusvolley;

import me.juneylove.shakedown.mechanics.MovementDetectRegion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static me.juneylove.shakedown.games.chorusvolley.PowerupPadManager.world;

public abstract class PowerupPad {

    final int reEnableDelay;
    final Material disabledMaterial = Material.DEAD_HORN_CORAL_FAN;
    final Material enabledMaterial;
    int disabledRemainingTicks;
    final Location fanLocation;

    ItemDisplay display;

    final MovementDetectRegion region;

    PowerupPad(Material enabledMaterial, Location fanLocation, int reEnableDelay, String name, Material displayMaterial) {
        this.enabledMaterial = enabledMaterial;
        this.fanLocation = fanLocation;
        this.reEnableDelay = reEnableDelay;

        this.region = new MovementDetectRegion(name, fanLocation.clone().add(-1, -1, -1), fanLocation.clone().add(1, 0, 1));

        Location displayLoc = fanLocation.clone().add(0.5, 0.75, 0.5);
        display = (ItemDisplay) world.spawnEntity(displayLoc, EntityType.ITEM_DISPLAY);
        display.setItemStack(new ItemStack(displayMaterial));
        display.setBillboard(Display.Billboard.CENTER);

        enable();
    }

    protected void onPlayerEnter(Player player) {

        if (shouldApplyEffect(player)) {
            applyEffect(player);
            disable();
        }

    }

    protected void tick() {
        if (disabledRemainingTicks > 0) {
            if (disabledRemainingTicks == 1) enable();
            disabledRemainingTicks--;
        }
    }

    protected abstract boolean shouldApplyEffect(Player player);
    protected abstract void applyEffect(Player player);

    protected void disable() {
        fanLocation.getBlock().setType(disabledMaterial);
        Waterlogged blockData = ((Waterlogged)fanLocation.getBlock().getBlockData());
        blockData.setWaterlogged(false);
        fanLocation.getBlock().setBlockData(blockData);
        disabledRemainingTicks = reEnableDelay;
        display.setVisibleByDefault(false);
    }

    protected void enable() {
        fanLocation.getBlock().setType(enabledMaterial);
        Waterlogged blockData = ((Waterlogged)fanLocation.getBlock().getBlockData());
        blockData.setWaterlogged(false);
        fanLocation.getBlock().setBlockData(blockData);
        display.setVisibleByDefault(true);
    }

}
