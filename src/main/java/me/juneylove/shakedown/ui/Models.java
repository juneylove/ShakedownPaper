package me.juneylove.shakedown.ui;

import me.juneylove.shakedown.scoring.TeamManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public enum Models {

    KIT_SELECT    (1,  Component.text("Select Kit").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD)),
    UNCLAIMED_KIT (2,  Component.text("Click to select").color(TextFormat.HIGHLIGHT_COLOR).decorate(TextDecoration.ITALIC)),
    SELECTED_KIT  (3, Component.text("Selected").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD)),

    TEAM_1_MEMBER  (4,  Component.text(TeamManager.getTeamByNumber( 1)).color(TextFormat.GetTextColor(TeamManager.getTeamByNumber( 1))).decorate(TextDecoration.BOLD)),
    TEAM_2_MEMBER  (5,  Component.text(TeamManager.getTeamByNumber( 2)).color(TextFormat.GetTextColor(TeamManager.getTeamByNumber( 2))).decorate(TextDecoration.BOLD)),
    TEAM_3_MEMBER  (6,  Component.text(TeamManager.getTeamByNumber( 3)).color(TextFormat.GetTextColor(TeamManager.getTeamByNumber( 3))).decorate(TextDecoration.BOLD)),
    TEAM_4_MEMBER  (7,  Component.text(TeamManager.getTeamByNumber( 4)).color(TextFormat.GetTextColor(TeamManager.getTeamByNumber( 4))).decorate(TextDecoration.BOLD)),
    TEAM_5_MEMBER  (8,  Component.text(TeamManager.getTeamByNumber( 5)).color(TextFormat.GetTextColor(TeamManager.getTeamByNumber( 5))).decorate(TextDecoration.BOLD)),
    TEAM_6_MEMBER  (9,  Component.text(TeamManager.getTeamByNumber( 6)).color(TextFormat.GetTextColor(TeamManager.getTeamByNumber( 6))).decorate(TextDecoration.BOLD)),
    TEAM_7_MEMBER  (10, Component.text(TeamManager.getTeamByNumber( 7)).color(TextFormat.GetTextColor(TeamManager.getTeamByNumber( 7))).decorate(TextDecoration.BOLD)),
    TEAM_8_MEMBER  (11, Component.text(TeamManager.getTeamByNumber( 8)).color(TextFormat.GetTextColor(TeamManager.getTeamByNumber( 8))).decorate(TextDecoration.BOLD)),
    TEAM_9_MEMBER  (12, Component.text(TeamManager.getTeamByNumber( 9)).color(TextFormat.GetTextColor(TeamManager.getTeamByNumber( 9))).decorate(TextDecoration.BOLD)),
    TEAM_10_MEMBER (13, Component.text(TeamManager.getTeamByNumber(10)).color(TextFormat.GetTextColor(TeamManager.getTeamByNumber(10))).decorate(TextDecoration.BOLD)),
    TEAM_11_MEMBER (14, Component.text(TeamManager.getTeamByNumber(11)).color(TextFormat.GetTextColor(TeamManager.getTeamByNumber(11))).decorate(TextDecoration.BOLD)),
    TEAM_12_MEMBER (15, Component.text(TeamManager.getTeamByNumber(12)).color(TextFormat.GetTextColor(TeamManager.getTeamByNumber(12))).decorate(TextDecoration.BOLD)),
    TEAM_13_MEMBER (16, Component.text(TeamManager.getTeamByNumber(13)).color(TextFormat.GetTextColor(TeamManager.getTeamByNumber(13))).decorate(TextDecoration.BOLD)),
    TEAM_14_MEMBER (17, Component.text(TeamManager.getTeamByNumber(14)).color(TextFormat.GetTextColor(TeamManager.getTeamByNumber(14))).decorate(TextDecoration.BOLD)),
    TEAM_15_MEMBER (18, Component.text(TeamManager.getTeamByNumber(15)).color(TextFormat.GetTextColor(TeamManager.getTeamByNumber(15))).decorate(TextDecoration.BOLD)),
    TEAM_16_MEMBER (19, Component.text(TeamManager.getTeamByNumber(16)).color(TextFormat.GetTextColor(TeamManager.getTeamByNumber(16))).decorate(TextDecoration.BOLD)),

    CAPTUREPOINT_WHITE  (20, Component.empty()),
    CAPTUREPOINT_GREEN  (21, Component.empty()),
    CAPTUREPOINT_RED    (22, Component.empty()),
    CAPTUREPOINT_ORANGE (23, Component.empty()),

    ELYTRA_BOOST    (24, Component.text("Elytra Boost").color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD)),
    INSTANT_RIPTIDE (25, Component.text("Instant Riptide").color(NamedTextColor.AQUA).decorate(TextDecoration.BOLD)),
    ARROW0          (26, Component.text("Out of arrows").color(NamedTextColor.RED)),

    BLANK (99, Component.empty());

    public final int num;
    public final Component name;

    Models(int num, Component name) {
        this.num = num;
        this.name = name;
    }

}
