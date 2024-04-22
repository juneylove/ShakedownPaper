package me.juneylove.shakedown.games.rapidodge;

import me.juneylove.shakedown.control.MatchProgress;
import me.juneylove.shakedown.mechanics.Respawn;
import me.juneylove.shakedown.mechanics.worlds.WorldSetting;
import me.juneylove.shakedown.scoring.ScoreManager;
import me.juneylove.shakedown.ui.TextFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.HashMap;

public class RapidodgeScore {

    private static final int scorePerPlayerOutlasted = 20;

    private static final TextComponent eliminated = Component.text(TextFormat.smallText("eliminated")).color(NamedTextColor.RED);
    private static final TextComponent gameOver = Component.text(TextFormat.smallText("game over")).color(NamedTextColor.AQUA);

    private final WorldSetting worldSetting;
    private RapidodgeControl control;

    private final HashMap<String, Integer> placements = new HashMap<>();

    protected RapidodgeScore(WorldSetting worldSetting1) {
        worldSetting = worldSetting1;
    }

    protected void assignControl(RapidodgeControl control) {
        this.control = control;
    }

    protected void onElimination(Player player) {

        int playersAlive = 0;
        for (Player player1 : worldSetting.getWorld().getPlayers()) {
            if (!Respawn.IsTempSpec(player1.getName())) {
                ScoreManager.addScore(player1.getName(), scorePerPlayerOutlasted);
                playersAlive++;
            }
        }

        placements.put(player.getName(), playersAlive+1);

        if (playersAlive <= 1) {
            control.onRoundFinish();
        } else {

            TextComponent subtitle = Component.text(TextFormat.smallText(TextFormat.formatPlace(playersAlive + 1) + " place")).color(NamedTextColor.YELLOW);
            Title.Times times = Title.Times.times(Duration.ZERO, Duration.ofSeconds(4L), Duration.ofSeconds(1L));
            player.showTitle(Title.title(eliminated, subtitle, times));

        }

    }

    protected void onRoundFinish() {

        for (Player player : worldSetting.getWorld().getPlayers()) {

            if (!Respawn.IsTempSpec(player.getName())) {

                TextComponent subtitle = Component.text(TextFormat.smallText("1st place")).color(NamedTextColor.YELLOW);
                Title.Times times = Title.Times.times(Duration.ZERO, Duration.ofSeconds(4L), Duration.ofSeconds(1L));
                player.showTitle(Title.title(gameOver, subtitle, times));

            } else {

                TextComponent subtitle;
                if (placements.get(player.getName()) == null) {
                    subtitle = Component.empty();
                } else {
                    int placement = placements.get(player.getName());
                    subtitle = Component.text(TextFormat.smallText(TextFormat.formatPlace(placement) + " place")).color(NamedTextColor.YELLOW);
                }
                Title.Times times = Title.Times.times(Duration.ZERO, Duration.ofSeconds(4L), Duration.ofSeconds(1L));
                player.showTitle(Title.title(gameOver, subtitle, times));

            }

        }

    }

    public void onRoundStart() {
        placements.clear();
    }
}
