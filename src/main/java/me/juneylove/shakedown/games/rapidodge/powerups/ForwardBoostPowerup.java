package me.juneylove.shakedown.games.rapidodge.powerups;

import me.juneylove.shakedown.games.rapidodge.AbstractPowerup;
import me.juneylove.shakedown.games.rapidodge.PowerupManager;
import org.bukkit.util.Vector;

public class ForwardBoostPowerup extends AbstractPowerup {

    @Override
    protected void start() {

        Vector addedVelocity;

        double additionalY = 145 - player.getLocation().getY(); // top surface of platform is y 145
        if (additionalY < 0.0) additionalY = 0.0;

        addedVelocity = player.getEyeLocation().getDirection().add(new Vector(0, additionalY/7.0, 0)).multiply(PowerupManager.forwardBoostMultiplier);

        player.setVelocity(player.getVelocity().add(addedVelocity));

    }

    @Override
    protected void end() {}

}
