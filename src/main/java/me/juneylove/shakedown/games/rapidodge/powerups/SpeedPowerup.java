package me.juneylove.shakedown.games.rapidodge.powerups;

import me.juneylove.shakedown.games.rapidodge.AbstractPowerup;
import me.juneylove.shakedown.games.rapidodge.PowerupManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpeedPowerup extends AbstractPowerup {

    @Override
    protected void start() {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, PowerupManager.speedDuration, 1));
    }

    @Override
    protected void end() {}

}
