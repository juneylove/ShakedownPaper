package me.juneylove.shakedown.games.chorusvolley;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;

public class PowerupPadManager {

    static HashMap<String, PowerupPad> pads = new HashMap<>();

    static World world;

    static final int speedReEnableTicks = 120;
    static final int arrowReEnableTicks = 180;
    static final int fishingRodReEnableTicks = 240;

    protected static void assignWorld(World world1) {

        world = world1;

        pads.put("speed1", new SpeedPad(Material.DEAD_BUBBLE_CORAL_FAN, new Location(world,  11, 129, -10), speedReEnableTicks, "speed1"));
        pads.put("speed2", new SpeedPad(Material.DEAD_BUBBLE_CORAL_FAN, new Location(world,   0, 129, -13), speedReEnableTicks, "speed2"));
        pads.put("speed3", new SpeedPad(Material.DEAD_BUBBLE_CORAL_FAN, new Location(world, -11, 129, -10), speedReEnableTicks, "speed3"));
        pads.put("speed4", new SpeedPad(Material.DEAD_BUBBLE_CORAL_FAN, new Location(world, -11, 129,  10), speedReEnableTicks, "speed4"));
        pads.put("speed5", new SpeedPad(Material.DEAD_BUBBLE_CORAL_FAN, new Location(world,   0, 129,  13), speedReEnableTicks, "speed5"));
        pads.put("speed6", new SpeedPad(Material.DEAD_BUBBLE_CORAL_FAN, new Location(world,  11, 129,  10), speedReEnableTicks, "speed6"));

        pads.put("arrow1", new ArrowPad(Material.DEAD_TUBE_CORAL_FAN, new Location(world,  16, 129,  15), arrowReEnableTicks, "arrow1"));
        pads.put("arrow2", new ArrowPad(Material.DEAD_TUBE_CORAL_FAN, new Location(world,  16, 129, -15), arrowReEnableTicks, "arrow2"));
        pads.put("arrow3", new ArrowPad(Material.DEAD_TUBE_CORAL_FAN, new Location(world, -16, 129, -15), arrowReEnableTicks, "arrow3"));
        pads.put("arrow4", new ArrowPad(Material.DEAD_TUBE_CORAL_FAN, new Location(world, -16, 129,  15), arrowReEnableTicks, "arrow4"));

        pads.put("fishingRod1", new FishingRodPad(Material.DEAD_BRAIN_CORAL_FAN, new Location(world, 0, 129, 0), fishingRodReEnableTicks, "fishingRod1"));

    }

    static protected void ticker() {

        for (PowerupPad pad : pads.values()) {
            pad.tick();
        }

    }

    static protected void onPlayerMove(PlayerMoveEvent event) {

        for (String name : pads.keySet()) {
            if (pads.get(name).region.justEntered(event.getPlayer())) {
                pads.get(name).onPlayerEnter(event.getPlayer());
            }
        }

    }

    public static void enableAll() {

        for (PowerupPad pad : pads.values()) {
            pad.enable();
        }

    }
}
