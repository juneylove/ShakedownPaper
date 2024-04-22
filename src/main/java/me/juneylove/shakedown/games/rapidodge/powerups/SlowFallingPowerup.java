package me.juneylove.shakedown.games.rapidodge.powerups;

import me.juneylove.shakedown.games.rapidodge.AbstractPowerup;
import me.juneylove.shakedown.games.rapidodge.PowerupManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SlowFallingPowerup extends AbstractPowerup {

    @Override
    protected void start() {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, PowerupManager.slowFallingDuration, 0));
    }

    @Override
    protected void end() {}

}
