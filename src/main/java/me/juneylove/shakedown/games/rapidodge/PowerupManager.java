package me.juneylove.shakedown.games.rapidodge;

import me.juneylove.shakedown.control.Controller;
import me.juneylove.shakedown.ui.GUIFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class PowerupManager {

    // ===== POWERUP BALANCING =====

    // all in ticks
    public static int levitationDuration = 40; // level 6
    public static int swiftSneakDuration = 250; // level 3
    public static int speedDuration = 300; // level 2
    public static int slowFallingDuration = 200; // level 1 obvs
    public static int jumpBoostDuration = 200; // level 2

    public static double forwardBoostMultiplier = 1.5;

    // ==========

    private static final float interactionSize = 0.7f;
    private static final int spawnY = 141;
    private static final int maxY = 150;
    private static final Vector vel = new Vector(0, 0.05, 0);

    private static final Random random = new Random();

    private final HashMap<Interaction, Item> spawnedPowerups = new HashMap<>();
    private final List<AbstractPowerup> activePowerups = new ArrayList<>();

    World world;
    BarManager barManager;
    Location side1;
    Location side2;
    int sideLength = 12;
    BlockFace direction1 = BlockFace.EAST;
    BlockFace direction2 = BlockFace.NORTH;

    protected PowerupManager(World world1, BarManager barManager1) {
        world = world1;
        barManager = barManager1;
        side1 = new Location(world, -9 ,spawnY, -4);
        side2 = new Location(world, 4, spawnY, 9);
    }

    protected void onPlayerInteractEntity(PlayerInteractEntityEvent event) {

        if (Controller.isPaused()) return;

        if (event.getRightClicked() instanceof Interaction interaction) {

            Item powerupItem = spawnedPowerups.get(interaction);
            event.getPlayer().getInventory().addItem(powerupItem.getItemStack());
            interaction.remove();
            powerupItem.remove();
            spawnedPowerups.remove(interaction);

        }

    }

    protected void onPlayerInteract(PlayerInteractEvent event) {

        if (Controller.isPaused()) return;

        ItemStack itemInHand = event.getItem();

        if (itemInHand != null && itemInHand.getType() == GUIFormat.menuSelectItem
                && itemInHand.getItemMeta().hasCustomModelData()){

            Powerups powerupType = Powerups.getByModelNum(itemInHand.getItemMeta().getCustomModelData());
            AbstractPowerup powerup = powerupType.powerup;
            powerup.assignPlayer(event.getPlayer());
            powerup.start();
            if (powerup.durationTicks != 0) {
                powerup.remainingTicks = powerup.durationTicks;
                activePowerups.add(powerup);
            }
            removeOnePowerup(event, powerupType);

        }

    }

    private void removeOnePowerup(PlayerInteractEvent event, Powerups powerup) {

        ItemStack heldItem = event.getPlayer().getInventory().getItem(event.getPlayer().getInventory().getHeldItemSlot());

        int slot;
        if (heldItem != null
            && heldItem.getType() == GUIFormat.menuSelectItem
            && heldItem.getItemMeta().hasCustomModelData()
            && heldItem.getItemMeta().getCustomModelData() == powerup.modelNum) {
            slot = event.getPlayer().getInventory().getHeldItemSlot();
        } else if (event.getItem() != null) {
            slot = event.getPlayer().getInventory().first(event.getItem());
        } else {
            return;
        }

        ItemStack stack = event.getPlayer().getInventory().getItem(slot);
        if (stack == null) return;
        stack.setAmount(stack.getAmount()-1);
        event.getPlayer().getInventory().setItem(slot, stack);

    }

    protected void ticker() {

        if (Controller.isPaused()) return;

        for (Interaction interaction : List.copyOf(spawnedPowerups.keySet())) {

            Item item = spawnedPowerups.get(interaction);
            item.setVelocity(vel);
            interaction.teleport(item);

            if (interaction.getLocation().getBlockY() >= maxY) {
                interaction.remove();
                item.remove();
                spawnedPowerups.remove(interaction);
            }

        }

        for (AbstractPowerup powerup : List.copyOf(activePowerups)) {

            if (powerup.remainingTicks > 0) {
                if (powerup.remainingTicks == 1) powerup.end();
                powerup.remainingTicks--;
                if (powerup.remainingTicks == 0) activePowerups.remove(powerup);
            }


        }

        if (random.nextInt(0, 201) < 1) {
            spawnPowerup();
        }

    }

    private void spawnPowerup() {

        Location location = randomSpawnLocation();
        int powerupId = random.nextInt(0, Powerups.values().length);
        Powerups powerup = Powerups.getById(powerupId);

        Item item = (Item) world.spawnEntity(location, EntityType.DROPPED_ITEM);
        ItemStack stack = new ItemStack(GUIFormat.menuSelectItem);
        ItemMeta meta = Bukkit.getItemFactory().getItemMeta(GUIFormat.menuSelectItem);
        TextComponent name = Component.empty()
                .append(Component.text(powerup.name).color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD))
                .append(Component.text(" (Right Click)")).color(NamedTextColor.GRAY);
        meta.displayName(name);
        meta.setCustomModelData(powerup.modelNum);
        stack.setItemMeta(meta);
        item.setItemStack(stack);
        item.setGravity(false);
        item.setCanPlayerPickup(false);
        item.setVelocity(vel);
        item.customName(name);
        item.setCustomNameVisible(true);
        item.setGlowing(true);

        Interaction interaction = (Interaction) world.spawnEntity(location, EntityType.INTERACTION);
        interaction.setInteractionHeight(interactionSize);
        interaction.setInteractionWidth(interactionSize);
        interaction.setGravity(false);
        interaction.setVelocity(vel);

        spawnedPowerups.put(interaction, item);

    }

    private Location randomSpawnLocation() {

        Location side;
        BlockFace direction;
        BlockFace stepOut;
        if (random.nextBoolean()) {
            side = side1;
            direction = direction1;
            stepOut = direction2;
        } else {
            side = side2;
            direction = direction2;
            stepOut = direction1;
        }

        int offset = random.nextInt(0, sideLength);
        Block result = side.getBlock().getRelative(direction, offset).getRelative(stepOut, barManager.getPowerupDistance());
        return result.getLocation().add(0.5, 0.0, 0.5);

    }

    protected void onPause() {

        for (Interaction interaction : spawnedPowerups.keySet()) {
            interaction.setVelocity(new Vector());
            spawnedPowerups.get(interaction).setVelocity(new Vector());
        }

    }

    public void onResume() {

        for (Interaction interaction : spawnedPowerups.keySet()) {
            interaction.setVelocity(vel);
            spawnedPowerups.get(interaction).setVelocity(vel);
        }

    }

    public void onRoundFinish() {

        for (AbstractPowerup powerup : activePowerups) {
            powerup.remainingTicks = 0;
            powerup.end();
        }
        activePowerups.clear();

        for (Player player : world.getPlayers()) {
            player.getInventory().clear();
        }

    }
}
