package me.juneylove.shakedown.games.rapidodge;

import me.juneylove.shakedown.control.MatchProgress;
import me.juneylove.shakedown.mechanics.Respawn;
import me.juneylove.shakedown.mechanics.worlds.StructureWorld;
import me.juneylove.shakedown.mechanics.worlds.WorldSetting;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;

public class RapidodgeControl {

    RapidodgeSetting setting;
    RapidodgeScore score;
    BarManager barManager;
    PowerupManager powerupManager;
    WorldSetting worldSetting;

    HashMap<RapidodgePhases, Integer> phaseDurations = new HashMap<>();
    RapidodgePhases phase = RapidodgePhases.NONE;
    Instant phaseEnd;
    Duration pauseRemainingTime;

    RapidodgeControl(RapidodgeSetting setting, RapidodgeScore score, BarManager barManager, PowerupManager powerupManager, WorldSetting worldSetting) {

        this.setting = setting;
        this.score = score;
        this.barManager = barManager;
        this.powerupManager = powerupManager;
        this.worldSetting = worldSetting;

        phaseDurations.put(RapidodgePhases.START_COUNTDOWN, setting.currentRound.preRoundCountdownSeconds);
        phaseDurations.put(RapidodgePhases.PLAY, setting.currentRound.roundDurationSeconds);
        phaseDurations.put(RapidodgePhases.END_COUNTDOWN, setting.currentRound.postRoundCountdownSeconds);

    }

    private enum RapidodgePhases {
        NONE,
        START_COUNTDOWN,
        PLAY,
        END_COUNTDOWN
    }

    // ==========

    protected void ticker() {

        if (phase == RapidodgePhases.NONE) return;

        if (phaseEnd != null && Instant.now().isAfter(phaseEnd)) {

            switch (phase) {

                case START_COUNTDOWN:
                    MatchProgress.endPreRoundCountdown();
                    onRoundStart();
                    break;
                case PLAY:
                    onRoundFinish();
                    break;

                case END_COUNTDOWN:
                    endRound();

            }

        }

    }

    public void pause() {

        pauseRemainingTime = Duration.between(Instant.now(), phaseEnd);
        powerupManager.onPause();
        MatchProgress.pause();

    }

    public void resume() {

        phaseEnd = Instant.now().plus(pauseRemainingTime);
        pauseRemainingTime = null;
        powerupManager.onResume();
        MatchProgress.resume();

    }

    // ==========

    protected void onGameStart() {
        beginRound();
    }

    protected void onGameEnd() {
        MatchProgress.endGame();
        phase = RapidodgePhases.NONE;
    }

    // ==========

    private void beginRound() {

        MatchProgress.beginRound();
        MatchProgress.startPreRoundCountdown();
        phase = RapidodgePhases.START_COUNTDOWN;
        phaseEnd = Instant.now().plusSeconds((long)phaseDurations.get(RapidodgePhases.START_COUNTDOWN));
        onWorldChange(worldSetting);

    }

    private void onRoundStart() {

        MatchProgress.onRoundStart();
        barManager.onRoundStart();
        score.onRoundStart();

        phase = RapidodgePhases.PLAY;
        phaseEnd = Instant.now().plusSeconds((long)phaseDurations.get(RapidodgePhases.PLAY));

    }

    public void onRoundFinish() {

        MatchProgress.onRoundFinish();
        barManager.onRoundFinish();
        score.onRoundFinish();
        powerupManager.onRoundFinish();

        for (WorldSetting worldSetting : setting.currentRound.worldSettings) {

            if (worldSetting instanceof StructureWorld structureWorld) {
                structureWorld.clearEntities();
                setting.clearBlocks(worldSetting.getWorld()); // temp (?)
            }

            for (Player player : worldSetting.getWorld().getPlayers()) {

                if (!Respawn.IsTempSpec(player.getName())) {
                    player.setAllowFlight(true);
                    player.setFlying(true);
                    player.setVelocity(new Vector());
                }

            }

        }

        phase = RapidodgePhases.END_COUNTDOWN;
        phaseEnd = Instant.now().plusSeconds((long)phaseDurations.get(RapidodgePhases.END_COUNTDOWN));

    }

    private void endRound() {
        MatchProgress.endRound();
    }

    public void onWorldChange(WorldSetting worldSetting) {

        for (Player player : worldSetting.getWorld().getPlayers()) {
            player.setGravity(true);
        }

    }

}
