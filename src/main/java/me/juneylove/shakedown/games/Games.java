package me.juneylove.shakedown.games;

import me.juneylove.shakedown.data.BackupToFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Games {

    public static int CURRENT_GAME_NUM = 0; // 0 is global lobby (pre-match, post-match, and between games), 1 is first game
    public static GameSetting CURRENT_GAME;
    public static List<String> COMPLETED_GAMES = new ArrayList<>();

    public static HashMap<String, GameSetting> GAMES = new HashMap<>();

    public static final String LOBBY = "Lobby";
    public static final int gameCountdownSeconds = 5;

    //=========================================================================

    public static void LoadGameOptions() {

        GAMES.clear();
        //GAMES = LoadGames.LoadFromFolder();

        GameSetting lobby = new Lobby();

        GAMES.put(LOBBY, lobby);

    }

    public static void SetCurrentGame(String name) {

        CURRENT_GAME = GAMES.get(name);
        CURRENT_GAME_NUM = COMPLETED_GAMES.size() + 1;

    }

    //=========================================================================

    public static boolean SavePlayedGames() {

        return BackupToFile.SaveObject(COMPLETED_GAMES, "CompletedGames.dat");

    }

    public static boolean LoadPlayedGames() {

        Object obj = BackupToFile.LoadObject("CompletedGames.dat");
        if (obj == null) {
            return false;
        } else {
            //noinspection unchecked
            COMPLETED_GAMES = (List<String>) obj;
            return true;
        }

    }

    //=========================================================================

}
