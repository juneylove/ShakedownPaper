package me.juneylove.shakedown.scoring;

import me.juneylove.shakedown.Main;

import java.io.Serializable;
import java.util.HashMap;

public class TeamScore implements Serializable {

    public int[] totals = new int[Main.NUM_OF_GAMES+1];

    // Games are 1-indexed so 0 slot in int[] is used for total for each player across all games
    public HashMap<String, int[]> players = new HashMap<>(ScoreManager.PLAYERS_PER_TEAM);

}
