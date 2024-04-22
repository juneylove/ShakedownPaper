package me.juneylove.shakedown.games.rapidodge.powerups;

import me.juneylove.shakedown.games.rapidodge.AbstractPowerup;
import org.bukkit.Location;

import java.util.Random;

public class TeleportPowerup extends AbstractPowerup {

    Random random = new Random();

    @Override
    protected void start() {

        float pitch = player.getPitch();
        float yaw = player.getYaw();

        int newX = player.getLocation().getBlockX();
        int newY = 150;
        int newZ = player.getLocation().getBlockZ();
        boolean found = false;
        int count = 0;
        while (!found) {

            newX = random.nextInt(-9, 2+1);
            newZ = random.nextInt(-2, 9+1);

            newY = player.getWorld().getHighestBlockYAt(newX, newZ);

            if (newY > -60) found = true;

            count++;
            if (count > 30) return;

        }

        Location newLocation = new Location(player.getWorld(), newX+0.5, newY+1.1, newZ+0.5);
        newLocation.setPitch(pitch);
        newLocation.setYaw(yaw);

        player.teleport(newLocation);

    }

    @Override
    protected void end() {}

}
