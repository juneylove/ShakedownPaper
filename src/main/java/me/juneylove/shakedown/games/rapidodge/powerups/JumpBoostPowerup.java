package me.juneylove.shakedown.games.rapidodge.powerups;

import me.juneylove.shakedown.games.rapidodge.AbstractPowerup;
import me.juneylove.shakedown.games.rapidodge.PowerupManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class JumpBoostPowerup extends AbstractPowerup {

    @Override
    protected void start() {
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, PowerupManager.jumpBoostDuration, 1));
    }

    @Override
    protected void end() {}

}
