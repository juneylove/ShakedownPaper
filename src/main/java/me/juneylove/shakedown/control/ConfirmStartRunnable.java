package me.juneylove.shakedown.control;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class ConfirmStartRunnable extends BukkitRunnable {

    Player player;

    public ConfirmStartRunnable(Player player) {
        this.player = player;
    }

    @Override
    public void run() {

        player.sendMessage(ChatColor.YELLOW + "Game start attempt expired");
        SdCommand.StartConfirmationInProgress = false;

    }

}
