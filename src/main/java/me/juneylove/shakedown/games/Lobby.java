package me.juneylove.shakedown.games;

import me.juneylove.shakedown.mechanics.*;
import me.juneylove.shakedown.mechanics.abilities.HealBow;
import me.juneylove.shakedown.mechanics.worlds.StandardWorld;
import me.juneylove.shakedown.mechanics.worlds.StructureWorld;
import me.juneylove.shakedown.mechanics.worlds.WorldSetting;
import me.juneylove.shakedown.mechanics.worlds.WorldSettings;
import me.juneylove.shakedown.ui.LabelBar;
import me.juneylove.shakedown.ui.Models;
import me.juneylove.shakedown.ui.GUIFormat;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Lobby extends GameSetting {

    {

        name = Games.LOBBY;

        // temp settings for testing
        moveItemsEnabled = true;
        allowConcretePlace = true;
        allowOtherBlockPlace = true;
        concreteInstabreak = true;
        chestInteractsEnabled = true;

        currentRound = new Round();
        currentRound.worldSettings = new ArrayList<>();

        WorldSetting worldSetting = new StructureWorld("test.nbt");
        currentRound.worldSettings.add(worldSetting);

        currentRound.kitSetting = new KitSettings().new Uniform(new ItemStack[]{new ItemStack(Material.DEBUG_STICK)});

        currentRound.name = "Round 1";
        currentRound.teamsPerWorld = 0;
        currentRound.preRoundCountdownSeconds = 20;
        currentRound.roundDurationSeconds = 10;
        currentRound.postRoundCountdownSeconds = 5;
        rounds.add(currentRound);

        //labelBarFormats.put(LabelBar.Side.LEFT, new LabelBar.AllTeamsLifeStatus());
        //labelBarFormats.put(LabelBar.Side.CENTER, new LabelBar.Timer(currentRound.roundDurationSeconds, LabelBar.ROUND_NAME));
        //labelBarFormats.put(LabelBar.Side.RIGHT, new LabelBar.TeamLifeStatus(LabelBar.OWN_TEAM));

    }

    @Override
    public boolean shouldLoadLootTables() {
        return false;
    }

}
