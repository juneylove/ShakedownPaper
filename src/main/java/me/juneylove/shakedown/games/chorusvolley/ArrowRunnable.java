package me.juneylove.shakedown.games.chorusvolley;

import org.bukkit.entity.Arrow;
import org.bukkit.scheduler.BukkitRunnable;

public class ArrowRunnable extends BukkitRunnable {

    Arrow arrow;
    BallHandler ballHandler;

    ArrowRunnable(Arrow arrow, BallHandler ballHandler) {
        this.arrow = arrow;
        this.ballHandler = ballHandler;
    }

    @Override
    public void run() {

        if (arrow.isDead()) {
            this.cancel();
            return;
        }

        ballHandler.arrowTick(arrow);

    }

}
