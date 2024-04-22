package me.juneylove.shakedown.games.chorusvolley;

import me.juneylove.shakedown.control.MatchProgress;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.mechanics.worlds.WorldSetting;
import me.juneylove.shakedown.mechanics.worlds.WorldSettings;
import me.juneylove.shakedown.ui.LabelBar;
import me.juneylove.shakedown.ui.LabelBarManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;

public class ChorusVolleyControl {

    ChorusVolleySetting setting;
    WorldSetting worldSetting;
    ChorusVolleyScore score;
    BallHandler ballHandler;

    HashMap<ChorusVolleyPhases, Integer> phaseDurations = new HashMap<>();
    ChorusVolleyPhases phase = ChorusVolleyPhases.NONE;
    Instant phaseEnd;

    Duration pauseRemainingTime;

    HashMap<String, Location> spawns = new HashMap<>();

    protected ChorusVolleyControl(ChorusVolleySetting setting, WorldSetting worldSetting) {

        this.setting = setting;
        this.worldSetting = worldSetting;

        phaseDurations.put(ChorusVolleyPhases.START_COUNTDOWN, setting.currentRound.preRoundCountdownSeconds);
        phaseDurations.put(ChorusVolleyPhases.PLAY, setting.currentRound.roundDurationSeconds);
        phaseDurations.put(ChorusVolleyPhases.POST_GOAL_PAUSE, 4);
        phaseDurations.put(ChorusVolleyPhases.RESUME_COUNTDOWN, 6);
        phaseDurations.put(ChorusVolleyPhases.OVERTIME, 120);
        phaseDurations.put(ChorusVolleyPhases.END_COUNTDOWN, setting.currentRound.postRoundCountdownSeconds);

    }

    public void assignClasses(ChorusVolleyScore score, BallHandler ballHandler) {
        this.score = score;
        this.ballHandler = ballHandler;
    }

    private enum ChorusVolleyPhases {
        NONE,
        START_COUNTDOWN,
        PLAY,
        POST_GOAL_PAUSE,
        RESUME_COUNTDOWN,
        OVERTIME,
        END_COUNTDOWN
    }

    // =========

    protected void ticker() {

        if (phase == ChorusVolleyPhases.NONE) return;

        if (phase == ChorusVolleyPhases.PLAY
            || phase == ChorusVolleyPhases.OVERTIME) score.ticker();

        if (phaseEnd != null && Instant.now().isAfter(phaseEnd)) {

            switch (phase) {

                case START_COUNTDOWN:
                    onRoundStart();
                    break;
                case PLAY:
                    if (score.gameIsTied()) beginOvertime();
                    else onRoundFinish();
                    break;
                case POST_GOAL_PAUSE:
                    postGoalReset();
                    break;
                case RESUME_COUNTDOWN:
                    postGoalResume();
                    break;
                case OVERTIME:
                    overtimeTimeLimitReached();
                    break;
                case END_COUNTDOWN:
                    endRound();

            }

        }

    }

    public void pause() {

        pauseRemainingTime = Duration.between(Instant.now(), phaseEnd);
        MatchProgress.pause();

    }

    public void resume() {

        phaseEnd = Instant.now().plus(pauseRemainingTime);
        pauseRemainingTime = null;
        MatchProgress.resume();

    }

    // =========

    protected void startGame() {

        beginRound(); // players are teleported to spawns here
        phase = ChorusVolleyPhases.START_COUNTDOWN;
        phaseEnd = Instant.now().plusSeconds((long)phaseDurations.get(ChorusVolleyPhases.START_COUNTDOWN));

        score.countdownTitleTicks = 20 * phaseDurations.get(ChorusVolleyPhases.START_COUNTDOWN);

        for (Player player : worldSetting.getWorld().getPlayers()) {
            spawns.put(player.getName(), player.getLocation());
        }

    }

    protected void endGame() {
        // MatchProgress.endGame is called in endRound
        phase = ChorusVolleyPhases.NONE;
    }

    // =========

    protected void beginRound() {
        MatchProgress.beginRound();
        MatchProgress.startPreRoundCountdown();

        ballHandler.deleteBall();
        ballHandler.spawnBall();
        ballHandler.pauseBall();
    }

    protected void onRoundStart() {

        MatchProgress.endPreRoundCountdown();
        MatchProgress.onRoundStart();
        phase = ChorusVolleyPhases.PLAY;
        phaseEnd = Instant.now().plusSeconds((long)phaseDurations.get(ChorusVolleyPhases.PLAY));

        ballHandler.resumeBall();
        ballHandler.resetVelocity();

    }

    protected void onRoundFinish() {

        MatchProgress.onRoundFinish();
        score.onRoundFinish();
        phase = ChorusVolleyPhases.END_COUNTDOWN;
        phaseEnd = Instant.now().plusSeconds((long) phaseDurations.get(ChorusVolleyPhases.END_COUNTDOWN));

        ballHandler.pauseBall();

    }

    protected void endRound() {
        MatchProgress.endRound();
        endGame();
    }

    // =========

    protected void onGoalScore() {

        ballHandler.pauseBall();
        ballHandler.onGoalScore();

        if (phase == ChorusVolleyPhases.PLAY) {

            phase = ChorusVolleyPhases.POST_GOAL_PAUSE;
            phaseEnd = Instant.now().plusSeconds((long) phaseDurations.get(ChorusVolleyPhases.POST_GOAL_PAUSE));
            LabelBarManager.pauseActiveTimers();

        } else if (phase == ChorusVolleyPhases.OVERTIME) {
            onRoundFinish();
        }

    }

    private void postGoalReset() {

        ballHandler.deleteBall();
        ballHandler.spawnBall();
        ballHandler.pauseBall();
        ballHandler.resetVelocity();
        PowerupPadManager.enableAll();

        Collection<String> teams = MatchProgress.teamsInWorld(worldSetting);
        WorldSettings.addRoundBarriers(worldSetting, teams);

        for (String ign : spawns.keySet()) {
            Player player = Bukkit.getPlayer(ign);
            if (player == null) continue;
            player.teleport(spawns.get(ign));
        }

        phase = ChorusVolleyPhases.RESUME_COUNTDOWN;
        phaseEnd = Instant.now().plusSeconds((long)phaseDurations.get(ChorusVolleyPhases.RESUME_COUNTDOWN));

        score.countdownTitleTicks = 20 * phaseDurations.get(ChorusVolleyPhases.RESUME_COUNTDOWN);

    }

    private void postGoalResume() {

        ballHandler.resumeBall();

        WorldSettings.removeRoundBarriers(worldSetting);

        phase = ChorusVolleyPhases.PLAY;
        phaseEnd = Instant.now().plusSeconds((long)phaseDurations.get(ChorusVolleyPhases.PLAY));

        LabelBarManager.resumePausedTimers();

    }

    // =========

    private void beginOvertime() {

        phase = ChorusVolleyPhases.OVERTIME;
        phaseEnd = Instant.now().plusSeconds((long)phaseDurations.get(ChorusVolleyPhases.OVERTIME));

        LabelBar.Stopwatch stopwatch = new LabelBar.Stopwatch("Overtime: ");
        setting.labelBarFormats.put(LabelBar.Side.CENTER, stopwatch);
        LabelBarManager.setAllFormats(worldSetting.getWorld());
        stopwatch.start();

    }

    private void overtimeTimeLimitReached() {
        onRoundFinish();
    }

}
