package me.juneylove.shakedown.control;

import me.juneylove.shakedown.Main;
import me.juneylove.shakedown.data.LoadTeams;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.scoring.ScoreManager;
import me.juneylove.shakedown.scoring.TeamManager;
import me.juneylove.shakedown.ui.LabelBarManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static me.juneylove.shakedown.scoring.TeamManager.*;

public class SdCommand implements CommandExecutor {

    @SuppressWarnings("FieldCanBeLocal")
    private static final long CONFIRM_TIME_SECONDS = 5;
    protected static boolean ResetConfirmationInProgress = false;
    protected static boolean StartConfirmationInProgress = false;
    static BukkitTask StartConfirmationTask;
    static BukkitTask ResetConfirmationTask;

    static Player player;

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (commandSender instanceof Player) {

            player = (Player) commandSender;

            // First, if a reset confirmation is in progress and any other command is issued, cancel the reset confirmation before processing the new command
            if (ResetConfirmationInProgress) {

                if (!(args.length == 1 && args[0].equalsIgnoreCase("reset"))) {

                    // Cancel reset confirmation
                    ResetConfirmationInProgress = false;
                    ResetConfirmationTask.cancel();
                    player.sendMessage(Component.text("Game reset cancelled", NamedTextColor.YELLOW));
                    // The given command is then processed normally

                }

            }

            // Similarly cancel an uneven start confirmation if any other command is received
            if (StartConfirmationInProgress) {

                if (!(args.length == 1 && args[0].equalsIgnoreCase("start"))) {

                    // Cancel uneven start confirmation
                    StartConfirmationInProgress = false;
                    StartConfirmationTask.cancel();
                    player.sendMessage(Component.text("Game start cancelled", NamedTextColor.YELLOW));
                    // The given command is then processed normally

                }

            }

            // Check different arg number to determine how to process the command
            if (args.length == 1) {

                switch (args[0].toLowerCase()) {

                    //sd start: start match
                    case "start":
                        SdStart();
                        break;

                    //sd pause: pause match in progress
                    case "pause":
                        SdPause();
                        break;

                    //sd resume: resume paused match
                    case "resume":
                        SdResume();
                        break;

                    //sd restore: restore game state from backup
                    case "restore":
                        SdRestore();
                        break;

                    //sd reset: reset entire match state; must be confirmed by running twice within 5 seconds
                    case "reset":
                        SdReset();
                        break;

                    // Unsupported argument
                    default:
                        GiveCorrectUsage();

                }

            } else if (args.length == 2) {

                if (Objects.equals(args[0].toLowerCase(),"teams")) {

                    //noinspection SwitchStatementWithTooFewBranches
                    switch (args[1].toLowerCase()) {

                        //sd teams load: load all teams from file
                        case "load":
                            SdLoadTeamsFromFile();
                            break;

                        // Unsupported argument
                        default:
                            GiveCorrectUsage();

                    }

                }

            } else if (args.length == 3) {

                if (Objects.equals(args[0].toLowerCase(),"teams")) {

                    switch (args[1].toLowerCase()) {

                        //sd teams new: manually register new team
                        case "new":
                            SdNewTeam(args[2]);
                            break;

                        //sd teams remove: delete EMPTY team
                        case "remove":
                            SdRemoveTeam(args[2]);
                            break;

                        // Unsupported argument
                        default:
                            GiveCorrectUsage();

                }

                } else {

                    String team = TeamManager.fixTeamCase(args[2]);

                    // Valid team name
                    switch (args[0].toLowerCase()) {

                        //sd addtoteam <ign> <team>: add a player to specified team
                        case "addtoteam":
                            SdAddToTeam(args[1], team);
                            break;

                        //sd removefromteam <ign> <team>: remove player from specified team
                        case "removefromteam":
                            SdRemoveFromTeam(args[1], team);
                            break;

                        //sd changeteam <ign> <team>: change a player's team
                        case "changeteam":
                            SdChangeTeam(args[1], team);
                            break;

                        // Unsupported argument
                        default:
                            GiveCorrectUsage();

                    }

                }

            } else {

                // Wrong number of arguments
                GiveCorrectUsage();

            }
            return true;

        } else {

            // Non-player sender
            return false;

        }

    }

    //=========================================================================

    private static void SdLoadTeamsFromFile() {

        boolean success = LoadTeams.LoadFromFile();
        if (success) {
            player.sendMessage(Component.text("Teams loaded from file", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("Error populating teams, check server log", NamedTextColor.RED));
        }

    }

    private static void SdNewTeam(String teamName) {

        if (teamExists(teamName)) {
            player.sendMessage(Component.text("Team already exists", NamedTextColor.YELLOW));
        } else {

            boolean success = TeamManager.registerEmptyTeam(teamName);
            if (success) {
                player.sendMessage(Component.text("Team registered successfully", NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text("Error creating team", NamedTextColor.RED));
            }

        }

    }

    private void SdRemoveTeam(String teamName) {

        if (TeamManager.isGameTeam(teamName)) {

            boolean success = TeamManager.removeEmptyTeam(teamName);
            if (success) {
                player.sendMessage(Component.text("Team removed successfully", NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text("Error deleting team", NamedTextColor.RED));
            }

        } else {

            if (teamExists(teamName)) {
                player.sendMessage(Component.text("This team cannot be deleted", NamedTextColor.YELLOW));
            } else {
                player.sendMessage(Component.text("Team does not exist", NamedTextColor.RED));

            }

        }

    }

    private static void SdAddToTeam(String ign, String team) {

        if (TeamManager.teamExists(team)) {

            boolean Success = TeamManager.addPlayerToTeam(ign, team);
            if (Success) {
                TeamManager.updatePlayerCount();
                player.sendMessage(Component.text(ign + " added to team " + team, NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text(ign + " is already on a team", NamedTextColor.RED));
            }

        } else {
            player.sendMessage(Component.text("Team " + team + " does not exist", NamedTextColor.RED));
        }

    }

    private static void SdRemoveFromTeam(String ign, String team) {

        if (TeamManager.isPlayerOnThisTeam(ign, team)) {

            boolean Success = removePlayerFromTeam(ign, team);
            if (Success) {
                TeamManager.updatePlayerCount();
                player.sendMessage(Component.text(ign + " removed from team " + team, NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text("Failed to remove " + ign + " from team " + team, NamedTextColor.RED));
            }

        } else {
            player.sendMessage(Component.text(ign + " is not on team " + team, NamedTextColor.YELLOW));
        }

    }

    private static void SdChangeTeam(String ign, String newTeam) {

        if (teamExists(newTeam)) {

            boolean Success = TeamManager.changeTeam(ign, newTeam);
            if (Success) {
                player.sendMessage(Component.text(ign + " moved to team " + newTeam, NamedTextColor.GREEN));
            } else {
                player.sendMessage(Component.text("Player " + ign + " not found", NamedTextColor.RED));
            }

        } else {
            player.sendMessage(Component.text("Team " + newTeam + " not found", NamedTextColor.RED));
        }

    }

    //=========================================================================

    private void SdStart() {

        if (Main.GAME_IN_PROGRESS) {
            player.sendMessage(Component.text("Match already in progress", NamedTextColor.YELLOW));
            return;
        }

        boolean teamsAreEven = ScoreManager.verifyTeamSizes(Main.getInstance());
        ConfirmStart(teamsAreEven);

    }

    private void ConfirmStart(boolean teamsAreEven) {

        // Start command has already been issued once in the last 5 seconds - start match anyway
        if (StartConfirmationInProgress) {

            StartConfirmationInProgress = false;
            StartConfirmationTask.cancel();
            StartMatch();

            // Start command has not already been issued in the last 5 seconds, prime for confirmation
        } else {

            if (teamsAreEven) {
                player.sendMessage(Component.text("Run \"/sd start\" again within 5 seconds to confirm", NamedTextColor.YELLOW));
            } else {
                player.sendMessage(Component.text("Warning: teams are uneven! ", NamedTextColor.RED)
                        .append(Component.text("Run \"/sd start\" again within 5 seconds to confirm", NamedTextColor.YELLOW)));
            }
            StartConfirmationInProgress = true;

            // Resets above boolean to false after CONFIRM_TIME_SECONDS seconds
            StartConfirmationTask = new ConfirmStartRunnable(player).runTaskLater(Main.getInstance(), 20* CONFIRM_TIME_SECONDS);

        }

    }

    private void StartMatch() {

        boolean Success = Controller.StartMatch();
        if (Success) {
            player.sendMessage(Component.text("Match started!", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("Unable to start match, check server log", NamedTextColor.RED));
        }

    }

    private void SdPause() {

        boolean Success = Controller.PauseMatch();
        if (Success) {
            player.sendMessage(Component.text("Match paused", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("Match not in progress", NamedTextColor.RED));
        }

    }

    private static void SdResume() {

        boolean Success = Controller.ResumeMatch();
        if (Success) {
            player.sendMessage(Component.text("Match resumed", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("Match already in progress", NamedTextColor.RED));
        }

    }

    private static void SdRestore() {

        boolean LoadSuccess = Controller.RestoreMatchState();
        if (LoadSuccess) {
            player.sendMessage(Component.text("Match restored successfully", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("Error(s) during match restore, check server log", NamedTextColor.RED));
        }

    }

    //=========================================================================

    private static void SdReset() {

        if (!Main.GAME_IN_PROGRESS) {
            ConfirmReset();
        } else {
            player.sendMessage(Component.text("Game is in progress, cannot reset", NamedTextColor.YELLOW));
        }

    }

    private static void ConfirmReset() {

        // Reset command has already been issued once in the last 5 seconds - reset everything monkaS
        if (ResetConfirmationInProgress) {

            player.sendMessage(Component.text("Resetting game", NamedTextColor.GREEN));
            ResetConfirmationInProgress = false;
            ResetConfirmationTask.cancel();
            ResetEverything();

        // Reset command has not already been issued in the last 5 seconds, prime for confirmation
        } else {

            player.sendMessage(Component.text("Run \"/sd reset\" again within 5 seconds to confirm", NamedTextColor.YELLOW));
            ResetConfirmationInProgress = true;

            // Resets above boolean to false after CONFIRM_TIME_SECONDS seconds
            ResetConfirmationTask = new ConfirmResetRunnable(player).runTaskLater(Main.getInstance(), 20* CONFIRM_TIME_SECONDS);

        }

    }

    private static void ResetEverything() {

        boolean SaveSuccess = Controller.SaveMatchState();

        if (SaveSuccess) {

            player.sendMessage(Component.text("Team and score data saved, resetting match", NamedTextColor.GREEN));

            TeamManager.clearPlacementsThenTeamsThenScores();
            MatchProgress.BackToLobby();
            Games.COMPLETED_GAMES.clear();
            LabelBarManager.deleteAll();

            // TODO: Will need to reset all games as well, once those are implemented

        } else {

            player.sendMessage(Component.text("Error saving team and score data, match not reset", NamedTextColor.RED));

        }

    }

    //=========================================================================

    private static void GiveCorrectUsage() {

        player.sendMessage(Component.text("Invalid command. Proper usage:", NamedTextColor.RED));
        player.sendMessage(Component.text("    /sd <start | pause | resume>", NamedTextColor.RED));
        player.sendMessage(Component.text("    /sd <restore | reset>", NamedTextColor.RED));
        player.sendMessage(Component.text("    /sd teams load", NamedTextColor.RED));
        player.sendMessage(Component.text("    /sd teams <new | remove> <name>", NamedTextColor.RED));
        player.sendMessage(Component.text("    /sd <addtoteam | removefromteam> <ign> <team>", NamedTextColor.RED));
        player.sendMessage(Component.text("    /sd changeteam <ign> <newteam>", NamedTextColor.RED));
        player.sendMessage(Component.text("When adding a new player, make sure case is correct.", NamedTextColor.RED));

    }

    //=========================================================================

}
