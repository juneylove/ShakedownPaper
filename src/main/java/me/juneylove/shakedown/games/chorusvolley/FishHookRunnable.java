package me.juneylove.shakedown.games.chorusvolley;

import org.bukkit.entity.FishHook;
import org.bukkit.scheduler.BukkitRunnable;

public class FishHookRunnable extends BukkitRunnable {

    FishHook fishHook;
    BallHandler ballHandler;

    protected FishHookRunnable(FishHook fishHook, BallHandler ballHandler) {
        this.fishHook = fishHook;
        this.ballHandler = ballHandler;
    }

    @Override
    public void run() {

        if (fishHook.isDead()) {
            this.cancel();
            return;
        }

        ballHandler.fishHookTick(fishHook);

        if (fishHook.getState() == FishHook.HookState.HOOKED_ENTITY) {
            this.cancel();
        }

    }

}
