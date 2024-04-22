package me.juneylove.shakedown.games.fenceduel;

import io.papermc.paper.entity.LookAnchor;
import me.juneylove.shakedown.control.Controller;
import me.juneylove.shakedown.control.MatchProgress;
import me.juneylove.shakedown.mechanics.MovementDetectRegion;
import me.juneylove.shakedown.worlddefinitions.FenceDuelWorlds;
import org.bukkit.entity.Player;

public class TeleportManager {

    FenceDuelWorlds.FenceDuelWorld world;
    FenceDuelScore score;

    Player redPlayer;
    Player bluePlayer;

    int redCurrentIsland = -1;
    int blueCurrentIsland = 1;

    TeleportManager(FenceDuelWorlds.FenceDuelWorld world) {
        this.world = world;
    }

    public void assignPlayers(Player red, Player blue) {
        redPlayer = red;
        bluePlayer = blue;
    }

    protected void assignScoreManager(FenceDuelScore score) {
        this.score = score;
    }

    protected Player getRedPlayer() {
        return redPlayer;
    }

    protected Player getBluePlayer() {
        return bluePlayer;
    }

    public void onPlayerEnterRegion(Player entered, MovementDetectRegion region) {

        if (!MatchProgress.playIsActive()) return;
        if (Controller.isPaused()) return;

        boolean playerIsRed = entered.equals(redPlayer);

        if (region.name.startsWith("island")) {

            // player moved to a different island
            int islandNumber = Integer.parseInt(region.name.split("island")[1]);
            if (playerIsRed) {

                if (islandNumber != redCurrentIsland) {
                    redCurrentIsland = islandNumber;
                    if (blueCurrentIsland < islandNumber) { // player got past opponent
                        moveBluePlayer(islandNumber);
                    }
                }

            } else {

                if (islandNumber != blueCurrentIsland) {
                    blueCurrentIsland = islandNumber;
                    if (redCurrentIsland > islandNumber) { // player got past opponent
                        moveRedPlayer(islandNumber);
                    }
                }

            }

        } else if (region.name.startsWith("void")) {

            if (playerIsRed) {

                if (redCurrentIsland > -5) redCurrentIsland--;
                moveRedPlayer(redCurrentIsland);

            } else {

                if (blueCurrentIsland < 5) blueCurrentIsland++;
                moveBluePlayer(blueCurrentIsland);

            }

        } else if (region.name.startsWith("goal")) {

            if (playerIsRed && region.name.equals("goal2")) {
                score.winRound(true);
            } else if (!playerIsRed && region.name.equals("goal1")) {
                score.winRound(false);
            }

        }

    }

    private void moveRedPlayer(int islandNumber) {
        redPlayer.teleport(world.getRedSpawn(islandNumber));
        redPlayer.lookAt(bluePlayer, LookAnchor.EYES, LookAnchor.EYES);
    }

    private void moveBluePlayer(int islandNumber) {
        bluePlayer.teleport(world.getBlueSpawn(islandNumber));
        bluePlayer.lookAt(redPlayer, LookAnchor.EYES, LookAnchor.EYES);
    }

    public void onPlayerDeath(Player target) {

        if (target.equals(redPlayer)) {
            redCurrentIsland--;
            target.setBedSpawnLocation(world.getRedSpawn(redCurrentIsland));
        } else {
            blueCurrentIsland++;
            target.setBedSpawnLocation(world.getBlueSpawn(blueCurrentIsland));
        }

    }

    public void onPlayerRespawn(Player player) {

        if (player.equals(redPlayer)) {
            redPlayer.lookAt(bluePlayer, LookAnchor.EYES, LookAnchor.EYES);
        } else {
            bluePlayer.lookAt(redPlayer, LookAnchor.EYES, LookAnchor.EYES);
        }

    }
}
