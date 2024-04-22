package me.juneylove.shakedown.control;

import me.juneylove.shakedown.scoring.TeamManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SdCommandTabCompleter implements TabCompleter {

    List<String> list = new ArrayList<>();

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String commandLabel, @NotNull String[] args) {

        if (commandSender instanceof Player && commandLabel.equalsIgnoreCase("sd")) {

            // Zero arguments - just "/sd"
            if (args.length == 0) {

                FirstArgList();
                Collections.sort(list);
                return list;

            // Started typing first argument - modify same list
            } else if (args.length == 1) {

                FirstArgList();
                list.removeIf(s -> !s.toLowerCase().startsWith(args[0].toLowerCase()));
                Collections.sort(list);
                return list;

            // Typing second argument - tab complete <load|new|remove> for first arg "teams"
            } else if (args.length == 2) {

                if (args[0].equalsIgnoreCase("teams")) {

                    list.add("load");
                    list.add("new");
                    list.add("remove");

                    list.removeIf(s -> !s.toLowerCase().startsWith(args[1].toLowerCase()));
                    Collections.sort(list);
                    return list;

                }

            // Typing third argument:
            // - Tab complete team names for first arg "teams" + second arg "remove"
            // - Tab complete team names for first arg "addtoteam," "removefromteam," or "changeteam"
            } else if (args.length == 3) {

                if ((args[0].equalsIgnoreCase("teams") &&
                     args[1].equalsIgnoreCase("remove"))
                        ||
                   (args[0].equalsIgnoreCase("addtoteam") ||
                    args[0].equalsIgnoreCase("removefromteam") ||
                    args[0].equalsIgnoreCase("changeteam"))) {

                    TeamList();
                    list.removeIf(s -> !s.toLowerCase().startsWith(args[2].toLowerCase()));
                    Collections.sort(list);
                    return list;

                }

            } // End argument number if-else

        } // End player and command label check

        return null;

    }

    //=========================================================================

    private void TeamList() {

        list.addAll(TeamManager.teams());

    }

    private void FirstArgList() {

        list.add("start");
        list.add("pause");
        list.add("resume");
        list.add("restore");
        list.add("reset");
        list.add("teams");
        list.add("addtoteam");
        list.add("removefromteam");
        list.add("changeteam");

    }

    //=========================================================================

}
