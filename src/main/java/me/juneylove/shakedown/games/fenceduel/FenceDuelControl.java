package me.juneylove.shakedown.games.fenceduel;

import me.juneylove.shakedown.control.MatchProgress;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;

public class FenceDuelControl {

    FenceDuelSetting setting;
    FenceDuelScore score;

    HashMap<FenceDuelPhases, Integer> phaseDurations = new HashMap<>();
    FenceDuelPhases phase = FenceDuelPhases.NONE;
    Instant phaseEnd;

    Duration pauseRemainingTime;

    protected FenceDuelControl(FenceDuelSetting setting, FenceDuelScore score) {

        this.setting = setting;
        this.score = score;

        phaseDurations.put(FenceDuelPhases.START_COUNTDOWN, setting.currentRound.preRoundCountdownSeconds);
        phaseDurations.put(FenceDuelPhases.PLAY, setting.currentRound.roundDurationSeconds);
        phaseDurations.put(FenceDuelPhases.POST_GOAL_PAUSE, 3);
        phaseDurations.put(FenceDuelPhases.RESUME_COUNTDOWN, 5);
        phaseDurations.put(FenceDuelPhases.END_COUNTDOWN, setting.currentRound.postRoundCountdownSeconds);

    }

    private enum FenceDuelPhases {
        NONE,
        START_COUNTDOWN,
        PLAY,
        POST_GOAL_PAUSE,
        RESUME_COUNTDOWN,
        END_COUNTDOWN
    }

    // =========

    protected void ticker() {

        if (phase == FenceDuelPhases.NONE) return;

        if (phaseEnd != null && Instant.now().isAfter(phaseEnd)) {

            switch (phase) {

                case START_COUNTDOWN:
                    MatchProgress.endPreRoundCountdown();
                    onRoundStart();
                    break;
                case PLAY:
                    score.endRoundDraw();
                    onRoundFinish();
                    break;
                case POST_GOAL_PAUSE:
                    endRound();
                    MatchProgress.incrementRound();
                    if (setting.currentRound != null) beginRound();
                    break;
                case RESUME_COUNTDOWN:
                    onRoundStart();
                    break;
                case END_COUNTDOWN:
                    endGame();

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

    // ==========

    protected void startGame() {

        MatchProgress.beginRound();
        phase = FenceDuelPhases.START_COUNTDOWN;
        phaseEnd = Instant.now().plusSeconds((long) phaseDurations.get(FenceDuelPhases.START_COUNTDOWN));

    }

    protected void endGame() {
        MatchProgress.endGame();
        phase = FenceDuelPhases.NONE;
    }

    // ==========

    private void beginRound() { // not called for first round

        MatchProgress.beginRound();
        phase = FenceDuelPhases.RESUME_COUNTDOWN;
        phaseEnd = Instant.now().plusSeconds((long) phaseDurations.get(FenceDuelPhases.RESUME_COUNTDOWN));

    }

    private void onRoundStart() {

        MatchProgress.onRoundStart();
        phase = FenceDuelPhases.PLAY;
        phaseEnd = Instant.now().plusSeconds((long)phaseDurations.get(FenceDuelPhases.PLAY));

    }

    protected void onRoundFinish() {

        MatchProgress.onRoundFinish();

        if (score.isScoreLimitReached()) {
            score.onScoreLimitReached();
            phase = FenceDuelPhases.END_COUNTDOWN;
            phaseEnd = Instant.now().plusSeconds((long)phaseDurations.get(FenceDuelPhases.END_COUNTDOWN));
        } else {
            phase = FenceDuelPhases.POST_GOAL_PAUSE;
            phaseEnd = Instant.now().plusSeconds((long)phaseDurations.get(FenceDuelPhases.POST_GOAL_PAUSE));
        }

    }

    private void endRound() {
        MatchProgress.endRound();
    }

}
