package me.juneylove.shakedown.listeners.eventlisteners;

import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.mechanics.*;
import me.juneylove.shakedown.mechanics.worlds.StructureWorld;
import me.juneylove.shakedown.mechanics.worlds.WorldSetting;
import me.juneylove.shakedown.scoring.TeamManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.util.BoundingBox;

public class PlayerMoveListener implements Listener {



    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        GameSetting game = Games.CURRENT_GAME;
        if (game == null) return;

        Player player = event.getPlayer();

        if (Respawn.IsTempSpec(player.getName()) || !TeamManager.isGamePlayer(player.getName())) {
            return;
        }

        for (WorldSetting worldSetting : game.currentRound.worldSettings) {

            if (worldSetting.getWorld() == player.getWorld()) {

                if (worldSetting instanceof StructureWorld structureWorld) {

                    if (player.getLocation().getY() < structureWorld.voidPlane) {
                        game.onPlayerEnterVoid(player);
                    }

                }

                for (MovementDetectRegion region : worldSetting.movementDetectRegions) {

                    if (region.justEntered(player)) {
                        game.onPlayerEnterRegion(player, region);
                    }

                }

            }

        }

        game.onPlayerMove(event);

    }

    // attempt at creating a faux collision box by straight up teleporting the player out lmao
    // leaving because idk might be useful at some point
    private void expel(Player player, BoundingBox playerBox, BoundingBox intersection) {

        double dx = intersection.getWidthX();
        double dy = intersection.getHeight();
        double dz = intersection.getWidthZ();

        Location newLoc;

        if (dx<dy && dx<dz) { // x is minimum

            if (playerBox.getCenterX() < intersection.getCenterX()) {
                // Move player in -x direction by dx
                newLoc = player.getLocation().add(-dx, 0 ,0);
            } else {
                // Move player in +x direction by dx
                newLoc = player.getLocation().add(dx, 0 ,0);
            }

        } else if (dy<dx && dy<dz) { // y is minimum

            if (playerBox.getCenterY() < intersection.getCenterY()) {
                // Move player in -y direction by dy
                newLoc = player.getLocation().add(0, -dy ,0);
            } else {
                // Move player in +y direction by dy
                newLoc = player.getLocation().add(0, dy ,0);
            }

        } else { // z is minimum

            if (playerBox.getCenterZ() < intersection.getCenterZ()) {
                // Move player in -z direction by dz
                newLoc = player.getLocation().add(0, 0 ,-dz);
            } else {
                // Move player in +z direction by dz
                newLoc = player.getLocation().add(0, 0 ,dz);
            }

        }

        player.teleport(newLoc);

    }

}
