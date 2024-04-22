package me.juneylove.shakedown.control;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ConfirmResetRunnable extends BukkitRunnable {

    Player player;

    public ConfirmResetRunnable(Player player) {
        this.player = player;
    }

    @Override
    public void run() {

        player.sendMessage(ChatColor.YELLOW + "Game reset expired");
        SdCommand.ResetConfirmationInProgress = false;

    }

}
