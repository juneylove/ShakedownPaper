package me.juneylove.shakedown.mechanics;

import me.juneylove.shakedown.Main;
import me.juneylove.shakedown.scoring.TeamManager;
import me.juneylove.shakedown.ui.GUIFormat;
import me.juneylove.shakedown.ui.Models;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Transformation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class CapturePoint {

    private enum Mode {

        UNCAPTURED (255, 255, 255, Models.CAPTUREPOINT_WHITE,  "\uE047"),
        FRIENDLY   (133, 255,  99, Models.CAPTUREPOINT_GREEN,  "\uE048"),
        ENEMY      (255,  99,  99, Models.CAPTUREPOINT_RED,    "\uE049"),
        CONTESTED  (255, 184,  97, Models.CAPTUREPOINT_ORANGE, "\uE04A");

        final Color glowColor;
        final ItemStack overlay;
        final TextComponent icon;

        Mode(int r, int g, int b, Models model, String icon) {

            this.glowColor = Color.fromRGB(r, g, b);
            this.overlay = GUIFormat.getMenuIcon(model);
            this.icon = Component.text(icon);

        }

    }

    Location center;
    boolean initialized = false;

    HashMap<Mode, ItemDisplay> overlays = new HashMap<>();
    HashMap<Mode, TextDisplay> icons = new HashMap<>();

    public CapturePoint(Location center) {

        this.center = center;
        // Center on block
        this.center.setX(center.getBlockX() + 0.5);
        this.center.setY(center.getBlockY());
        this.center.setZ(center.getBlockZ() + 0.5);

    }

    public void initialize() {

        Transformation transformation;

        ItemDisplay uncapturedOverlay = (ItemDisplay) center.getWorld().spawnEntity(center, EntityType.ITEM_DISPLAY);
        uncapturedOverlay.setItemStack(Mode.UNCAPTURED.overlay);
        uncapturedOverlay.setGlowColorOverride(Mode.UNCAPTURED.glowColor);
        uncapturedOverlay.setRotation(0.0f, 90.0f);
        transformation = uncapturedOverlay.getTransformation();
        transformation.getScale().set(5.0, 5.0, 0.05);
        uncapturedOverlay.setTransformation(transformation);
        uncapturedOverlay.setShadowStrength(0.0f);
        uncapturedOverlay.setVisibleByDefault(false);
        uncapturedOverlay.setGlowing(true);
        overlays.put(Mode.UNCAPTURED, uncapturedOverlay);

        ItemDisplay friendlyOverlay = (ItemDisplay) center.getWorld().spawnEntity(center, EntityType.ITEM_DISPLAY);
        friendlyOverlay.setItemStack(Mode.FRIENDLY.overlay);
        friendlyOverlay.setGlowColorOverride(Mode.FRIENDLY.glowColor);
        friendlyOverlay.setRotation(0.0f, 90.0f);
        transformation = friendlyOverlay.getTransformation();
        transformation.getScale().set(5.0, 5.0, 0.05);
        friendlyOverlay.setTransformation(transformation);
        friendlyOverlay.setShadowStrength(0.0f);
        friendlyOverlay.setVisibleByDefault(false);
        friendlyOverlay.setGlowing(true);
        overlays.put(Mode.FRIENDLY, friendlyOverlay);

        ItemDisplay enemyOverlay = (ItemDisplay) center.getWorld().spawnEntity(center, EntityType.ITEM_DISPLAY);
        enemyOverlay.setItemStack(Mode.ENEMY.overlay);
        enemyOverlay.setGlowColorOverride(Mode.ENEMY.glowColor);
        enemyOverlay.setRotation(0.0f, 90.0f);
        transformation = enemyOverlay.getTransformation();
        transformation.getScale().set(5.0, 5.0, 0.05);
        enemyOverlay.setTransformation(transformation);
        enemyOverlay.setShadowStrength(0.0f);
        enemyOverlay.setVisibleByDefault(false);
        enemyOverlay.setGlowing(true);
        overlays.put(Mode.ENEMY, enemyOverlay);

        ItemDisplay contestedOverlay = (ItemDisplay) center.getWorld().spawnEntity(center, EntityType.ITEM_DISPLAY);
        contestedOverlay.setItemStack(Mode.CONTESTED.overlay);
        contestedOverlay.setGlowColorOverride(Mode.CONTESTED.glowColor);
        contestedOverlay.setRotation(0.0f, 90.0f);
        transformation = contestedOverlay.getTransformation();
        transformation.getScale().set(5.0, 5.0, 0.05);
        contestedOverlay.setTransformation(transformation);
        contestedOverlay.setShadowStrength(0.0f);
        contestedOverlay.setVisibleByDefault(false);
        contestedOverlay.setGlowing(true);
        overlays.put(Mode.CONTESTED, contestedOverlay);

        Location iconCenter = center.clone().add(0.0, 1.0, 0.0);

        TextDisplay uncapturedIcon = (TextDisplay) center.getWorld().spawnEntity(iconCenter, EntityType.TEXT_DISPLAY);
        uncapturedIcon.setBillboard(Display.Billboard.CENTER);
        uncapturedIcon.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
        uncapturedIcon.text(Mode.UNCAPTURED.icon);
        uncapturedIcon.setShadowStrength(0.0f);
        uncapturedIcon.setSeeThrough(true);
        uncapturedIcon.setVisibleByDefault(false);
        uncapturedIcon.setGlowing(true);
        icons.put(Mode.UNCAPTURED, uncapturedIcon);

        TextDisplay friendlyIcon = (TextDisplay) center.getWorld().spawnEntity(iconCenter, EntityType.TEXT_DISPLAY);
        friendlyIcon.setBillboard(Display.Billboard.CENTER);
        friendlyIcon.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
        friendlyIcon.text(Mode.FRIENDLY.icon);
        friendlyIcon.setShadowStrength(0.0f);
        friendlyIcon.setSeeThrough(true);
        friendlyIcon.setVisibleByDefault(false);
        friendlyIcon.setGlowing(true);
        icons.put(Mode.FRIENDLY, friendlyIcon);

        TextDisplay enemyIcon = (TextDisplay) center.getWorld().spawnEntity(iconCenter, EntityType.TEXT_DISPLAY);
        enemyIcon.setBillboard(Display.Billboard.CENTER);
        enemyIcon.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
        enemyIcon.text(Mode.ENEMY.icon);
        enemyIcon.setShadowStrength(0.0f);
        enemyIcon.setSeeThrough(true);
        enemyIcon.setVisibleByDefault(false);
        enemyIcon.setGlowing(true);
        icons.put(Mode.ENEMY, enemyIcon);

        TextDisplay contestedIcon = (TextDisplay) center.getWorld().spawnEntity(iconCenter, EntityType.TEXT_DISPLAY);
        contestedIcon.setBillboard(Display.Billboard.CENTER);
        contestedIcon.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
        contestedIcon.text(Mode.CONTESTED.icon);
        contestedIcon.setShadowStrength(0.0f);
        contestedIcon.setVisibleByDefault(false);
        contestedIcon.setGlowing(true);
        icons.put(Mode.CONTESTED, contestedIcon);

        initialized = true;

    }

    public void remove() {

        for (Mode mode : Mode.values()) {
            overlays.get(mode).remove();
            icons.get(mode).remove();
        }
        initialized = false;

    }

    public List<Player> presentPlayers() {

        List<Player> players = new ArrayList<>();

        Collection<Player> nearbyPlayers = center.clone().add(0.0, 1.5, 0.0).getNearbyPlayers(2.5);

        for (Player player : nearbyPlayers) {
            if (TeamManager.isGamePlayer(player.getName())) {
                players.add(player);
            }
        }

        return players;

    }

    public boolean isContested() {

        if (presentPlayers().size() == 0) return false;

        String firstTeam = TeamManager.getTeam(presentPlayers().get(0).getName());

        for (Player player : presentPlayers()) {

            String team = TeamManager.getTeam(player.getName());
            if (!team.equals(firstTeam)) return true;

        }

        return false;

    }

    public String controllingTeam() {

        if (isContested()) return null;
        if (presentPlayers().size() == 0) return null;

        return TeamManager.getTeam(presentPlayers().get(0).getName());

    }

    public void update() {

        if (!initialized) return;

        for (Player player : center.getWorld().getPlayers()) {

            Mode mode;

            if (controllingTeam() == null) {

                if (isContested()) {
                    mode = Mode.CONTESTED;
                } else {
                    mode = Mode.UNCAPTURED;
                }

            } else { // Capture point is controlled by one team

                if (TeamManager.isPlayerOnThisTeam(player.getName(), controllingTeam())) {
                    mode = Mode.FRIENDLY;
                } else {
                    mode = Mode.ENEMY;
                }

            }

            showToPlayer(player, mode);

        }

    }

    private void showToPlayer(Player player, Mode mode) {

        Plugin plugin = Main.getInstance();

        for (Mode modeValue : Mode.values()) {

            if (modeValue == mode) {
                player.showEntity(plugin, overlays.get(modeValue));
                player.showEntity(plugin, icons.get(modeValue));
            } else {
                player.hideEntity(plugin, overlays.get(modeValue));
                player.hideEntity(plugin, icons.get(modeValue));
            }

        }

    }

}
