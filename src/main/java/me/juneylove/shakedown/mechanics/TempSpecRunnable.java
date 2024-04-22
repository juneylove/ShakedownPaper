package me.juneylove.shakedown.mechanics;

import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.mechanics.worlds.WorldSetting;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class TempSpecRunnable extends BukkitRunnable {

    Player player;

    public TempSpecRunnable(Player player) {
        this.player = player;
    }

    @Override
    public void run() {

        Location deathLocation = player.getLastDeathLocation();
        player.spigot().respawn();

        boolean teleported = false;
        for (WorldSetting worldSetting : Games.CURRENT_GAME.currentRound.worldSettings) {
            if (worldSetting.getWorld().equals(player.getWorld())) {
                if (worldSetting.spectatorSpawn != null) {
                    player.teleport(worldSetting.spectatorSpawn);
                    teleported = true;
                }
                break;
            }
        }
        if (!teleported) {
            //noinspection ConstantConditions
            player.teleport(deathLocation.add(0, 1, 0));
        }

        if (Games.CURRENT_GAME.spectatorsAreInvisible) {
            player.setVisibleByDefault(false);
        }

        if (Games.CURRENT_GAME.spectatorsCanFly) {
            player.setAllowFlight(true);
            player.setFlying(true);
        }

        player.getInventory().clear();
        player.setCollidable(false);
        player.setFlySpeed(0.1f);

        // hide hunger bar
        player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, PotionEffect.INFINITE_DURATION, 0, true, false, false));
        player.setFoodLevel(20);

        // Reset health to 10 hearts while temp spec
        double defaultHealth = Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getDefaultValue();
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(defaultHealth);
        player.setHealth(defaultHealth);

    }

}
