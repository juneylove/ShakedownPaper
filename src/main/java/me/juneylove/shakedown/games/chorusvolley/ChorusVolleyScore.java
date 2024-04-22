package me.juneylove.shakedown.games.chorusvolley;

import me.juneylove.shakedown.mechanics.worlds.WorldSetting;
import me.juneylove.shakedown.ui.TextFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.ShulkerBullet;

import java.time.Duration;

public class ChorusVolleyScore {

    static ShulkerBullet bullet;

    static TextComponent redVictory = Component.text(TextFormat.smallText("red victory")).color(NamedTextColor.RED).decorate(TextDecoration.BOLD);
    static TextComponent draw = Component.text(TextFormat.smallText("draw")).color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD);
    static TextComponent blueVictory = Component.text(TextFormat.smallText("blue victory")).color(NamedTextColor.BLUE).decorate(TextDecoration.BOLD);

    static TextComponent redGoalTitle = Component.text(TextFormat.smallText("red goal")).color(NamedTextColor.RED).decorate(TextDecoration.BOLD);
    static TextComponent blueGoalTitle = Component.text(TextFormat.smallText("blue goal")).color(NamedTextColor.BLUE).decorate(TextDecoration.BOLD);
    static Title.Times titleTimes = Title.Times.times(Duration.ZERO, Duration.ofSeconds(4L), Duration.ofSeconds(1L));

    int redGoalsScored = 0;
    int blueGoalsScored = 0;

    int countdownTitleTicks = 0;

    static TextComponent countdown3 = Component.text(">   3   <").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD);
    static TextComponent countdown2 = Component.text(">  2  <").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD);
    static TextComponent countdown1 = Component.text("> 1 <").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD);
    static TextComponent countdown0 = Component.text("GO!").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD);
    static Title.Times countdownTimes = Title.Times.times(Duration.ZERO, Duration.ofSeconds(1L), Duration.ofSeconds(1L));

    protected void assignBall(ShulkerBullet bullet1) {
        bullet = bullet1;
    }

    WorldSetting world;
    ChorusVolleyControl control;

    protected ChorusVolleyScore(WorldSetting worldSetting, ChorusVolleyControl control) {
        world = worldSetting;
        this.control = control;
    }

    protected boolean gameIsTied() {
        return (redGoalsScored == blueGoalsScored);
    }

    static double redGoalPlane = -19;
    static double blueGoalPlane = 20;

    protected void ticker() {

        if (bullet == null) return;

        if (bullet.getLocation().getX() < redGoalPlane) {
            scoreBlueGoal();
        } else if (bullet.getLocation().getX() > blueGoalPlane) {
            scoreRedGoal();
        }

        if (countdownTitleTicks > 0) {

            if (countdownTitleTicks == 61) bullet.getWorld().showTitle(Title.title(countdown3, Component.empty(), countdownTimes));
            if (countdownTitleTicks == 41) bullet.getWorld().showTitle(Title.title(countdown2, Component.empty(), countdownTimes));
            if (countdownTitleTicks == 21) bullet.getWorld().showTitle(Title.title(countdown1, Component.empty(), countdownTimes));
            if (countdownTitleTicks ==  1) bullet.getWorld().showTitle(Title.title(countdown0, Component.empty(), countdownTimes));

            countdownTitleTicks--;

        }

    }

    private void scoreRedGoal() {

        redGoalsScored++;

        TextComponent subtitle = Component.empty()
                .append(Component.text(redGoalsScored).color(NamedTextColor.RED))
                .append(Component.text(" - ").color(NamedTextColor.WHITE))
                .append(Component.text(blueGoalsScored).color(NamedTextColor.BLUE));
        bullet.getWorld().showTitle(Title.title(redGoalTitle, subtitle, titleTimes));

        control.onGoalScore();

    }

    private void scoreBlueGoal() {

        blueGoalsScored++;

        TextComponent subtitle = Component.empty()
                .append(Component.text(redGoalsScored).color(NamedTextColor.RED))
                .append(Component.text(" - ").color(NamedTextColor.WHITE))
                .append(Component.text(blueGoalsScored).color(NamedTextColor.BLUE));
        bullet.getWorld().showTitle(Title.title(blueGoalTitle, subtitle, titleTimes));

        control.onGoalScore();

    }

    protected void onRoundFinish() {

        if (redGoalsScored > blueGoalsScored) {

            // red wins
            TextComponent subtitle = Component.empty()
                    .append(Component.text(redGoalsScored).color(NamedTextColor.RED))
                    .append(Component.text(" - ").color(NamedTextColor.WHITE))
                    .append(Component.text(blueGoalsScored).color(NamedTextColor.BLUE));
            bullet.getWorld().showTitle(Title.title(redVictory, subtitle, titleTimes));

        } else if (redGoalsScored == blueGoalsScored) {

            // draw
            TextComponent subtitle = Component.empty()
                    .append(Component.text(redGoalsScored).color(NamedTextColor.RED))
                    .append(Component.text(" - ").color(NamedTextColor.WHITE))
                    .append(Component.text(blueGoalsScored).color(NamedTextColor.BLUE));
            bullet.getWorld().showTitle(Title.title(draw, subtitle, titleTimes));

        } else {

            // blue wins
            TextComponent subtitle = Component.empty()
                    .append(Component.text(redGoalsScored).color(NamedTextColor.RED))
                    .append(Component.text(" - ").color(NamedTextColor.WHITE))
                    .append(Component.text(blueGoalsScored).color(NamedTextColor.BLUE));
            bullet.getWorld().showTitle(Title.title(blueVictory, subtitle, titleTimes));

        }

    }

}
