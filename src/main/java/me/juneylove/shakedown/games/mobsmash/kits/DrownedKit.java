package me.juneylove.shakedown.games.mobsmash.kits;

import me.juneylove.shakedown.Main;
import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.games.mobsmash.MobKit;
import me.juneylove.shakedown.mechanics.worlds.StructureWorld;
import me.juneylove.shakedown.mechanics.worlds.WorldSetting;
import me.juneylove.shakedown.ui.GUIFormat;
import me.juneylove.shakedown.ui.TextFormat;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.LightningRod;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class DrownedKit extends MobKit {

    double riptideSpeed = 1.6;
    public static double lightningDamage = 6.0;

    public Location lightningRodLoc;

    public ItemStack trident = GUIFormat.unbreakable(Material.TRIDENT);

    WorldSetting worldSetting;

    final Sound ABILITY_SOUND = Sound.sound(Key.key("item.trident.riptide_3"), Sound.Source.WEATHER, 1.0f, 1.0f);
    final Sound ULTIMATE_PLACE_SOUND = Sound.sound(Key.key("block.copper.place"), Sound.Source.WEATHER, 1.0f, 1.0f);
    public static final Sound ULTIMATE_STRIKE_SOUND = Sound.sound(Key.key("item.trident.thunder"), Sound.Source.WEATHER, 5.0f, 1.0f);

    {
        abilityDurationSeconds = 0.05;
        abilityCooldownSeconds = 8;
        ultimateDurationSeconds = 10;
        ultPointsRequired = 5;

        displayName = Component.text("Drowned").color(TextColor.color(0x4ca682)).decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false);

        description = new ArrayList<>();
        description.add(Component.text("Primary weapon: Thrown Trident").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.text("Secondary weapon: Fishing Rod").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(abilityDivider);
        description.add(Component.text("Riptide boost (no water required)").color(abilityColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(ultimateDivider);
        description.add(Component.text("Lightning rod trap").color(ultimateColor)
                .decoration(TextDecoration.ITALIC, false));

        details = new ArrayList<>();
        details.add(detailsDivider);
        details.add(detailsHeader);
        details.add(Component.text("Your Weakness effect inhibits melee hits, but throwing the trident still deals full damage. Your ")

                .append(Component.text("ability").color(abilityColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.swapOffhand"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" instantly performs a riptide boost, even when not in water. Your "))

                .append(Component.text("ultimate").color(ultimateColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.drop"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" places a lightning rod on the ground, which disappears after "
                        + TextFormat.formatNumber(ultimateDurationSeconds) + " seconds. If hit with your thrown trident, lightning will strike.")));
        details.add(detailsDivider);

        trident.addEnchantment(Enchantment.LOYALTY, 3);

        kit = new ItemStack[]{trident, GUIFormat.unbreakable(Material.FISHING_ROD)};

    }

    @Override
    public void onDeath() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        if (ultimateDurationRemainingTicks > 0) {
            ultimateDurationRemainingTicks = 0;
            onUltimateEnd();
        }

    }

    @Override
    public void applyKit() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, PotionEffect.INFINITE_DURATION, 2, true, false, false));

        for (WorldSetting worldSetting1 : Games.CURRENT_GAME.currentRound.worldSettings) {
            if (worldSetting1.getWorld() == player.getWorld()) {
                worldSetting = worldSetting1;
            }
        }

    }

    @Override
    public void onAbilityStart() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        Vector newVelocity = player.getVelocity().add(player.getLocation().getDirection().multiply(riptideSpeed));
        player.setVelocity(newVelocity);

        player.getWorld().playSound(ABILITY_SOUND, player);

    }

    @Override
    public void abilityRun() {}

    @Override
    public void onAbilityEnd() {}

    @Override
    public void onUltimateStart() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        lightningRodLoc = player.getLocation().add(player.getEyeLocation().getDirection().setY(0.1));
        if (lightningRodLoc.getBlock().getType() != Material.AIR) {

            lightningRodLoc = player.getLocation();
            if (lightningRodLoc.getBlock().getType() != Material.AIR) {

                lightningRodLoc = player.getLocation().add(0.0, 1.0, 0.0);
                if (lightningRodLoc.getBlock().getType() != Material.AIR) {

                    // player must be underwater or something i dunno, but cancel ultimate
                    currentUltPoints = ultPointsRequired;
                    ultimateDurationRemainingTicks = 0;
                    return;

                }

            }

        }

        LightningRod rod = (LightningRod) Bukkit.createBlockData(Material.LIGHTNING_ROD);
        rod.setFacing(BlockFace.UP);
        player.getWorld().setBlockData(lightningRodLoc, rod);

        player.getWorld().playSound(ULTIMATE_PLACE_SOUND, player);

    }

    @Override
    public void ultimateRun() {}

    @Override
    public void onUltimateEnd() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        if (lightningRodLoc != null) {
            player.getWorld().setBlockData(lightningRodLoc, Bukkit.createBlockData(Material.AIR));
        }

    }

    // ==========

    public void onTridentThrow(Trident trident) {
        new DrownedRunnable(trident).runTaskTimer(Main.getInstance(),0, 1);
    }

    public class DrownedRunnable extends BukkitRunnable {

        Trident tridentEntity;
        int voidPlane;

        public DrownedRunnable(Trident trident) {

            this.tridentEntity = trident;

            if (worldSetting instanceof StructureWorld structureWorld) {
                this.voidPlane = structureWorld.voidPlane;
            } else {
                this.cancel();
            }

        }

        @Override
        public void run() {

            if (tridentEntity.getLocation().getY() < voidPlane) {

                Player player = Bukkit.getPlayer(ign);
                if (player == null) return;

                if (player.getInventory().getItem(player.getInventory().getHeldItemSlot()) == null) {
                    player.getInventory().setItem(player.getInventory().getHeldItemSlot(), trident);
                } else {
                    // holding an item (fishing rod) so just add the trident to the inventory
                    player.getInventory().addItem(trident);
                }
                tridentEntity.remove();
                this.cancel();

            }

        }

    }

}
