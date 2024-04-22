package me.juneylove.shakedown.games.mobsmash;

import me.juneylove.shakedown.control.MatchProgress;
import me.juneylove.shakedown.mechanics.worlds.StructureWorld;
import me.juneylove.shakedown.mechanics.worlds.WorldSetting;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;

public class MobSmashControl {

    MobSmashSetting setting;
    WorldSetting worldSetting;

    HashMap<MobSmashPhases, Integer> phaseDurations = new HashMap<>();
    MobSmashPhases phase = MobSmashPhases.NONE;
    Instant phaseEnd;
    Duration pauseRemainingTime;

    MobSmashScore score;

    protected MobSmashControl(MobSmashSetting setting, WorldSetting worldSetting) {

        this.setting = setting;
        this.worldSetting = worldSetting;

        phaseDurations.put(MobSmashPhases.KIT_SELECTION, 20);
        phaseDurations.put(MobSmashPhases.START_COUNTDOWN, setting.currentRound.preRoundCountdownSeconds);
        phaseDurations.put(MobSmashPhases.PLAY, setting.currentRound.roundDurationSeconds);
        phaseDurations.put(MobSmashPhases.END_COUNTDOWN, setting.currentRound.postRoundCountdownSeconds);

    }

    public void assignScore(MobSmashScore score) {
        this.score = score;
    }

    private enum MobSmashPhases {
        NONE,
        KIT_SELECTION,
        START_COUNTDOWN,
        PLAY,
        END_COUNTDOWN
    }

    // ==========

    protected void ticker() {

        if (phase == MobSmashPhases.NONE) return;

        if (phaseEnd != null && Instant.now().isAfter(phaseEnd)) {

            switch (phase) {

                case KIT_SELECTION:
                    endKitSelect();
                    MatchProgress.startPreRoundCountdown();
                    break;
                case START_COUNTDOWN:
                    MatchProgress.endPreRoundCountdown();
                    onRoundStart();
                    break;
                case PLAY:
                    score.timeLimitReached(); // calls onRoundFinish
                    break;
                case END_COUNTDOWN:
                    endRound();
                    MatchProgress.incrementRound();
                    if (setting.currentRound != null) beginRound();

            }

        }

    }

    public void pause() {

        pauseRemainingTime = Duration.between(Instant.now(), phaseEnd);
        MatchProgress.pause();
        CapturePointManager.pause();

    }

    public void resume() {

        phaseEnd = Instant.now().plus(pauseRemainingTime);
        pauseRemainingTime = null;
        MatchProgress.resume();
        CapturePointManager.resume();

    }

    // ==========

    protected void onGameStart() {
        beginRound();
    }

    protected void onGameEnd() {
        MatchProgress.endGame();
        phase = MobSmashPhases.NONE;
    }

    // ==========

    protected void beginRound() {

        MatchProgress.beginRound();
        phase = MobSmashPhases.KIT_SELECTION;
        onWorldChange(worldSetting);
        MatchProgress.startKitSelection(Duration.ofSeconds(phaseDurations.get(MobSmashPhases.KIT_SELECTION)));
        phaseEnd = Instant.now().plusSeconds((long) phaseDurations.get(MobSmashPhases.KIT_SELECTION));

    }

    protected void onRoundStart() {

        MatchProgress.onRoundStart();
        AbilityManager.onRoundStart();
        CapturePointManager.onRoundStart();
        score.onRoundStart();

        phase = MobSmashPhases.PLAY;
        phaseEnd = Instant.now().plusSeconds((long) phaseDurations.get(MobSmashPhases.PLAY));

    }

    protected void onRoundFinish() {

        MatchProgress.onRoundFinish();
        AbilityManager.onRoundFinish();
        CapturePointManager.onRoundFinish();

        for (WorldSetting worldSetting : setting.currentRound.worldSettings) {
            if (worldSetting instanceof StructureWorld structureWorld) {
                structureWorld.clearEntities();
            }
        }

        phase = MobSmashPhases.END_COUNTDOWN;
        phaseEnd = Instant.now().plusSeconds((long) phaseDurations.get(MobSmashPhases.END_COUNTDOWN));

    }

    protected void endRound() {
        MatchProgress.endRound();
    }

    // ==========

    public void endKitSelect() {

        MatchProgress.endKitSelection();

        if (setting.currentRound.kitSetting instanceof MobSmashKitSelect mobSmashKitSelect) {
            mobSmashKitSelect.endKitSelection(worldSetting.getWorld());
        }

        AbilityManager.onEndKitSelect();

        phase = MobSmashPhases.START_COUNTDOWN;
        phaseEnd = Instant.now().plusSeconds((long) phaseDurations.get(MobSmashPhases.START_COUNTDOWN));

    }

    public void onWorldChange(WorldSetting worldSetting) {
        CapturePointManager.initialize(worldSetting);
        score.onWorldChange();
    }

}
