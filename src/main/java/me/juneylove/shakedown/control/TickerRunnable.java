package me.juneylove.shakedown.control;

import org.bukkit.scheduler.BukkitRunnable;

import java.time.Instant;

public class TickerRunnable extends BukkitRunnable {

    // Initialize these with subtracted values so all tasks run immediately
    Instant Next1SecondTick = Instant.now();
    Instant Next5SecondTick = Instant.now();
    Instant Next15SecondTick = Instant.now();

    @Override
    public void run() {

        Controller.TickerConstant();

        Instant now = Instant.now();

        if (now.isAfter(Next1SecondTick)) {
            Controller.Ticker1Second();
            Next1SecondTick = Next1SecondTick.plusSeconds(1);
        }

        if (now.isAfter(Next5SecondTick)) {
            Controller.Ticker5Second();
            Next5SecondTick = Next5SecondTick.plusSeconds(5);
        }

        if (now.isAfter(Next15SecondTick)) {
            Controller.Ticker15Second();
            Next15SecondTick = Next15SecondTick.plusSeconds(15);
        }

    }

}
