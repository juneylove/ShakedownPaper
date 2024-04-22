package me.juneylove.shakedown.games.fenceduel;

import me.juneylove.shakedown.games.GameSetting;
import me.juneylove.shakedown.mechanics.KitSettings;
import me.juneylove.shakedown.mechanics.MovementDetectRegion;
import me.juneylove.shakedown.ui.GUIFormat;
import me.juneylove.shakedown.ui.LabelBar;
import me.juneylove.shakedown.worlddefinitions.FenceDuelWorlds;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class FenceDuelSetting extends GameSetting {

    TeleportManager teleportManager;
    FenceDuelScore score;

    private final FenceDuelControl control;

    {

        name = "Fence Duels";

        pvpEnabled = true;
        predeterminedDamageHp = 2; // one heart per hit

        respawnTimeSeconds = 0;
        maxHealthHearts = 4;

        moveItemsEnabled = true;

        allowFireExtinguish = false;

        // ==========

        Round round1 = new Round();
        round1.name = "Round 1";
        round1.preRoundCountdownSeconds = 15;
        round1.roundDurationSeconds = 20;
        round1.postRoundCountdownSeconds = 5;

        round1.teamsPerWorld = 0;
        round1.numberOfLives = 0;

        FenceDuelWorlds.FenceDuelWorld worldSetting = new FenceDuelWorlds().fenceDuel1();
        World world = worldSetting.getWorld();
        round1.worldSettings.add(worldSetting);

        world.setGameRule(GameRule.FIRE_DAMAGE, false);

        ItemStack sword = GUIFormat.unbreakable(Material.IRON_SWORD);
        round1.kitSetting = new KitSettings().new Uniform(new ItemStack[]{sword});

        rounds.add(round1);
        currentRound = round1;

        for (int i=2; i<FenceDuelScore.roundsNeededToWin*2; i++) {

            Round round = round1.copy();
            round.name = "Round " + i;
            rounds.add(round);

        }

        // ==========

        teleportManager = new TeleportManager(worldSetting);
        score = new FenceDuelScore(teleportManager);
        teleportManager.assignScoreManager(score);
        control = new FenceDuelControl(this, score);
        score.assignControl(control);

        // ==========

        labelBarFormats.put(LabelBar.Side.CENTER, new LabelBar.Timer(currentRound.roundDurationSeconds));
        labelBarFormats.put(LabelBar.Side.LEFT, score.new LeftFenceDuelBar());
        labelBarFormats.put(LabelBar.Side.RIGHT, score.new RightFenceDuelBar());

    }

    @Override
    public void onGameStart() {
        control.startGame();
    }

    @Override
    public void onGameEnd() {
        control.endGame();
    }

    @Override
    public void onGamePause() {
        control.pause();
    }

    @Override
    public void onGameResume() {
        control.resume();
    }

    @Override
    public void tickerConstant() {
        control.ticker();
    }

    @Override
    public void onPlayerDeath(String lastDamagerIgn, Player target) {
        teleportManager.onPlayerDeath(target);
    }

    @Override
    public void onPlayerRespawn(Player player) {
        teleportManager.onPlayerRespawn(player);
    }

    @Override
    public void onPlayerEnterRegion(Player player, MovementDetectRegion region) {
        teleportManager.onPlayerEnterRegion(player, region);
    }

}
