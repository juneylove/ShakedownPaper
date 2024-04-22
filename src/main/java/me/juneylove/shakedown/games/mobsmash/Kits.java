package me.juneylove.shakedown.games.mobsmash;

import me.juneylove.shakedown.games.mobsmash.kits.*;

import java.util.List;

public enum Kits {

    // Model numbers 200-229 for unselected kit icon
    // Model numbers 230-259 for kits selected by teammates
    // Model numbers 260-289 for kits selected by the user themself
    // Model numbers 290-299 for special kit items
    BLAZE          ( 0, new BlazeKit()),
    SILVERFISH     ( 1, new SilverfishKit()),
    PILLAGER       ( 2, new PillagerKit()),
    ENDERMITE      ( 3, new EndermiteKit()),
    PUFFERFISH     ( 4, new PufferfishKit()),
    PIGLIN         ( 5, new PiglinKit()),
    VEX            ( 6, new VexKit()),
    WITHERSKELETON ( 7, new WitherSkeletonKit()),
    ZOMBIE         ( 8, new ZombieKit()),
    STRAY          ( 9, new StrayKit()),
    BAT            (10, new BatKit()),
    SHULKER        (11, new ShulkerKit()),
    PHANTOM        (12, new PhantomKit()),
    SLIME          (13, new SlimeKit()),
    RABBIT         (14, new RabbitKit()),
    AXOLOTL        (15, new AxolotlKit()),
    ALLAY          (16, new AllayKit()),
    EVOKER         (17, new EvokerKit()),
    POLARBEAR      (18, new PolarBearKit()),
    GHAST          (19, new GhastKit()),
    DROWNED        (20, new DrownedKit());

    final int baseModelNumber = 200;
    final int teamSelectionIncrement = 30;
    final int userSelectionIncrement = 60;

    public final int unselectedModel;
    public final int teamSelectedModel;
    public final int userSelectedModel;

    public final int index;
    public final MobKit kit;

    static final List<Kits> kitsList;

    static {
        kitsList = List.of(Kits.values());
    }

    Kits(int modelIndex, MobKit kit) {
        this.index = modelIndex;
        this.unselectedModel   = baseModelNumber + modelIndex;
        this.teamSelectedModel = baseModelNumber + modelIndex + teamSelectionIncrement;
        this.userSelectedModel = baseModelNumber + modelIndex + userSelectionIncrement;
        this.kit = kit;
        this.kit.kitID = modelIndex;
    }

    public static Kits getByIndex(int index) {
        return kitsList.get(index);
    }

}
