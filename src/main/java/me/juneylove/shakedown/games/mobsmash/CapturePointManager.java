package me.juneylove.shakedown.games.mobsmash;

import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.mechanics.CapturePoint;
import me.juneylove.shakedown.mechanics.worlds.WorldSetting;
import org.bukkit.World;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;

public class CapturePointManager {

    static HashMap<World, CapturePoint> activePoint = new HashMap<>();
    static HashMap<World, List<CapturePoint>> allPoints = new HashMap<>();
    static HashMap<World, Integer> activePointIndex = new HashMap<>();
    static HashMap<World, Double> cycleTimeSeconds = new HashMap<>();

    static HashMap<World, Instant> nextCapturePointCycle = new HashMap<>();
    static HashMap<World, Duration> pauseRemainingDurations = new HashMap<>();

    static String controllingTeam = null;

    public static void initialize(WorldSetting worldSetting) {

        World world = worldSetting.getWorld();
        int roundDurationSeconds = Games.CURRENT_GAME.currentRound.roundDurationSeconds;
        int numberOfPoints = worldSetting.capturePoints.size();

        allPoints.put(world, worldSetting.capturePoints);
        activePoint.put(world, worldSetting.capturePoints.get(0));
        activePoint.get(world).initialize();
        activePoint.get(world).update();
        activePointIndex.put(world, 0);
        cycleTimeSeconds.put(world, roundDurationSeconds / (double) numberOfPoints);

    }

    protected static void pause() {

        for (World world : nextCapturePointCycle.keySet()) {
            pauseRemainingDurations.put(world, Duration.between(Instant.now(), nextCapturePointCycle.get(world)));
        }

    }

    protected static void resume() {

        for (World world : pauseRemainingDurations.keySet()) {
            nextCapturePointCycle.put(world, Instant.now().plus(pauseRemainingDurations.get(world)));
        }
        pauseRemainingDurations.clear();

    }

    public static void onRoundStart() {

        for (WorldSetting worldSetting : Games.CURRENT_GAME.currentRound.worldSettings) {
            World world = worldSetting.getWorld();
            nextCapturePointCycle.put(world, Instant.now().plusMillis((long) (cycleTimeSeconds.get(world) * 1000)));
        }

    }

    public static void onRoundFinish() {

        for (World world : activePoint.keySet()) {
            nextCapturePointCycle.remove(world);
            activePoint.get(world).remove();
            activePointIndex.put(world, 0);
        }

    }

    public static void capturePointTicker() {

        for (World world : activePoint.keySet()) {

            if (nextCapturePointCycle.get(world) == null) continue;

            if (Instant.now().isAfter(nextCapturePointCycle.get(world))) {
                cyclePoint(world);
            }

            activePoint.get(world).update();
            controllingTeam = activePoint.get(world).controllingTeam();

        }

    }

    private static void cyclePoint(World world) {

        int newIndex = activePointIndex.get(world) + 1;
        if (newIndex >= allPoints.get(world).size()) return;

        activePoint.get(world).remove();
        activePointIndex.put(world, newIndex);
        activePoint.put(world, allPoints.get(world).get(newIndex));
        activePoint.get(world).initialize();
        nextCapturePointCycle.put(world, Instant.now().plusMillis((long) (cycleTimeSeconds.get(world) * 1000)));

    }


}
