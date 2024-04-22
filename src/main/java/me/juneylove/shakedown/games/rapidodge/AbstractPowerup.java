package me.juneylove.shakedown.games.rapidodge;

import org.bukkit.entity.Player;

public abstract class AbstractPowerup {

    public Player player;
    public int durationTicks;
    protected int remainingTicks;

    protected void assignPlayer(Player player1) {
        player = player1;
    }

    protected abstract void start();
    protected abstract void end();

}
