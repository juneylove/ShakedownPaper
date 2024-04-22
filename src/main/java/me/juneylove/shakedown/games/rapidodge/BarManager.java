package me.juneylove.shakedown.games.rapidodge;

import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.ui.TextFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class BarManager {

    static Random random = new Random();

    static List<AbstractBar> activeBars = new ArrayList<>();

    // ==========

    World world;
    Location side1;
    BlockFace direction1;
    Location side2;
    BlockFace direction2;
    int sideLength = 12;
    int ticksSinceBarSpawn = 1000; // set high so bars start spawning immediately

    List<difficultyStage> difficultyStages = new ArrayList<>();
    difficultyStage currentStage;
    int currentStageNum = 0;
    Instant stageStart;
    Duration stageDuration;

    private static class difficultyStage {

        int barMinSpawnAmount;
        int barMaxSpawnAmount;
        int barSpawnIntervalTicks;
        int launchDelayTicks;
        int powerupDistance;

        difficultyStage(int minAmount, int maxAmount, int spawnInterval, int delay, int powerupDistance) {
            barMinSpawnAmount = minAmount;
            barMaxSpawnAmount = maxAmount;
            barSpawnIntervalTicks = spawnInterval;
            launchDelayTicks = delay;
            this.powerupDistance = powerupDistance;
        }

    }

    {

        difficultyStages.add(new difficultyStage(3, 5, 100, 45, 0));
        difficultyStages.add(new difficultyStage(4, 6, 80, 35, 1));
        difficultyStages.add(new difficultyStage(5, 7, 60, 25, 1));
        difficultyStages.add(new difficultyStage(6, 8, 50, 20, 2));
        difficultyStages.add(new difficultyStage(7, 9, 40, 10, 2));

        currentStage = difficultyStages.get(0);

    }

    protected int getPowerupDistance() {
        return currentStage.powerupDistance;
    }

    // ==========

    private void spawnBars() {

        Location side;
        BlockFace direction;
        BlockFace facing;
        if (random.nextBoolean()) {
            side = side1;
            direction = direction1;
            facing = direction2;
        } else {
            side = side2;
            direction = direction2;
            facing = direction1;
        }

        int barAmount = random.nextInt(currentStage.barMinSpawnAmount, currentStage.barMaxSpawnAmount+1);
        Set<Integer> barLocations = new HashSet<>();
        while (barLocations.size() < barAmount) {
            barLocations.add(random.nextInt(0, sideLength));
        }

        for (int offset : barLocations) {
            Location location = side.getBlock().getRelative(direction, offset).getLocation();
            activeBars.add(new VerticalBar(location, facing, currentStage.launchDelayTicks));
        }

        ticksSinceBarSpawn = 0;

    }

    // ==========

    protected void ticker() {

        for (AbstractBar bar : List.copyOf(activeBars)) {
            bar.run();
        }

        if (Instant.now().isAfter(stageStart.plus(stageDuration)) && currentStageNum < difficultyStages.size()-1) {
            nextDifficultyStage();
        }

        ticksSinceBarSpawn++;
        if (ticksSinceBarSpawn > currentStage.barSpawnIntervalTicks) {
            spawnBars();
        }

    }

    protected void onRoundStart() {
        stageStart = Instant.now();
        stageDuration = Duration.ofSeconds(Games.CURRENT_GAME.currentRound.roundDurationSeconds / difficultyStages.size());
    }

    protected void onRoundFinish() {
        for (AbstractBar bar : List.copyOf(activeBars)) {
            bar.remove();
        }
        activeBars.clear();
    }

    // ==========

    protected BarManager(World world1) {
        world = world1;
        side1 = new Location(world, -11, 145, 19);
        direction1 = BlockFace.EAST;
        side2 = new Location(world, -20, 145, 10);
        direction2 = BlockFace.NORTH;
    }

    private void nextDifficultyStage() {

        currentStageNum++;
        currentStage = difficultyStages.get(currentStageNum);
        stageStart = Instant.now();

        for (Player player : world.getPlayers()) {
            player.sendMessage(Component.text("[" + TextFormat.PLUS_ONE_PX + TextFormat.PLUS_ONE_PX
                    + "!" + TextFormat.PLUS_ONE_PX + TextFormat.PLUS_ONE_PX + "] Difficulty increasing").color(NamedTextColor.RED));
        }

    }

}
