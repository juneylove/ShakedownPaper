package me.juneylove.shakedown.games.rapidodge;

import me.juneylove.shakedown.games.rapidodge.powerups.*;

import java.util.List;

public enum Powerups {

    LEVITATION    (1, "Levitation", new LevitationPowerup()),
    SWIFT_SNEAK   (2, "Swift Sneak", new SwiftSneakPowerup()),
    FORWARD_BOOST (3, "Forward Boost", new ForwardBoostPowerup()),
    SPEED         (4, "Speed", new SpeedPowerup()),
    SLOW_FALLING  (5, "Slow Falling", new SlowFallingPowerup()),
    TELEPORT      (6, "Teleport", new TeleportPowerup()),
    JUMP_BOOST    (7, "Jump Boost", new JumpBoostPowerup());

    static final int baseModelNum = 300;
    final int modelNum;
    final String name;
    final AbstractPowerup powerup;

    static final List<Powerups> powerupsList;

    Powerups(int num, String name1, AbstractPowerup powerup1) {
        modelNum = baseModelNum + num;
        name = name1;
        powerup = powerup1;
    }

    static {
        powerupsList = List.of(Powerups.values());
    }

    public static Powerups getById(int id) {
        return powerupsList.get(id);
    }

    public static Powerups getByModelNum(int modelNumber) {
        return powerupsList.get(modelNumber-baseModelNum-1);
    }

}
