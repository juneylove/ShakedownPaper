package me.juneylove.shakedown.ui;

import me.juneylove.shakedown.scoring.TeamManager;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.Set;

public class TitleManager {

    final static int defaultDurationTicks = 60;
    final static int defaultFadeInTicks = 0;
    final static int defaultFadeOutTicks = 10;

    public static void showToTeam(String team, TextComponent title, TextComponent subtitle) {
        showToTeam(team, title, subtitle, defaultFadeInTicks, defaultDurationTicks, defaultFadeOutTicks);
    }

    public static void showToTeam(String team, TextComponent title, TextComponent subtitle, int fadeInTicks, int durationTicks, int fadeOutTicks) {

        Duration fadeIn  = Duration.ofMillis(fadeInTicks * 50L);
        Duration stay    = Duration.ofMillis(durationTicks * 50L);
        Duration fadeOut = Duration.ofMillis(fadeOutTicks * 50L);
        Title.Times times = Title.Times.times(fadeIn, stay, fadeOut);
        Title fullTitle = Title.title(title, subtitle, times);

        Set<String> players = TeamManager.getMembers(team);
        for (String ign : players) {
            Player player = Bukkit.getPlayer(ign);
            if (player == null) continue;
            player.showTitle(fullTitle);
        }

    }

}
