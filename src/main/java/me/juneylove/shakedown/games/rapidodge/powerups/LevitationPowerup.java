package me.juneylove.shakedown.games.rapidodge.powerups;

import me.juneylove.shakedown.games.rapidodge.AbstractPowerup;
import me.juneylove.shakedown.games.rapidodge.PowerupManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class LevitationPowerup extends AbstractPowerup {

    @Override
    protected void start() {

        double additionalDuration = 145 - player.getLocation().getY(); // top surface of platform is y 145
        if (additionalDuration < 0.0) additionalDuration = 0.0;

        int duration = PowerupManager.levitationDuration + (int)(20 * additionalDuration/5); // add one second for every 5 blocks below platform

        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, duration, 5));

    }

    @Override
    protected void end() {}

}
