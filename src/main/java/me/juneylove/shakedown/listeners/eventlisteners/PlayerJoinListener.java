package me.juneylove.shakedown.listeners.eventlisteners;

import me.juneylove.shakedown.Main;
import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.mechanics.GlowManager;
import me.juneylove.shakedown.ui.LabelBarManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoinListener extends BukkitRunnable implements Listener {

    Player player;

    public PlayerJoinListener() {}

    PlayerJoinListener(Player player) {
        this.player = player;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        GlowManager.onPlayerRejoin(event.getPlayer());

        new PlayerJoinListener(event.getPlayer()).runTask(Main.getInstance());

    }

    @Override
    public void run() {
        LabelBarManager.reAddPlayer(player);
    }
}
