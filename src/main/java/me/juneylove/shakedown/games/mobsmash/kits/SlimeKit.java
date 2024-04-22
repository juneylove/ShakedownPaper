package me.juneylove.shakedown.games.mobsmash.kits;

import me.juneylove.shakedown.Main;
import me.juneylove.shakedown.games.mobsmash.MobKit;
import me.juneylove.shakedown.mechanics.NPC;
import me.juneylove.shakedown.ui.GUIFormat;
import me.juneylove.shakedown.ui.TextFormat;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class SlimeKit extends MobKit {

    NPC replica1;
    NPC replica2;
    NPC replica3;

    public static final ItemStack sword = GUIFormat.customItemName(GUIFormat.unbreakable(Material.WOODEN_SWORD),
            Component.text("Poison Sword").color(NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
    public static double poisonSwordInflictDuration = 1.0;

    final Sound ABILITY_SOUND = Sound.sound(Key.key("entity.slime.squish"), Sound.Source.WEATHER, 1.0f, 1.0f);
    final Sound ULTIMATE_SOUND = Sound.sound(Key.key("entity.slime.attack"), Sound.Source.WEATHER, 5.0f, 1.0f);

    {
        abilityDurationSeconds = 0.05;
        abilityCooldownSeconds = 10;
        ultimateDurationSeconds = 4;
        ultPointsRequired = 5;

        displayName = Component.text("Slime").color(TextColor.color(0x69b956)).decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false);

        description = new ArrayList<>();
        description.add(Component.text("Primary weapon: Wooden Poison Sword").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.text("Passive: Jump Boost II").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(abilityDivider);
        description.add(Component.text("Spawn a slime that targets enemies").color(abilityColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(ultimateDivider);
        description.add(Component.text("Replicate yourself to confuse the enemy").color(ultimateColor)
                .decoration(TextDecoration.ITALIC, false));

        details = new ArrayList<>();
        details.add(detailsDivider);
        details.add(detailsHeader);
        details.add(Component.text("Your ")

                .append(Component.text("ability").color(abilityColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.swapOffhand"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" spawns a medium slime that will split into small slimes when killed. Your "))

                .append(Component.text("ultimate").color(ultimateColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.drop"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" spawns replicas of yourself for "
                        + TextFormat.formatNumber(ultimateDurationSeconds) + " seconds as a distraction.")));
        details.add(detailsDivider);

        kit = new ItemStack[]{sword};

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

        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, PotionEffect.INFINITE_DURATION, 1, true));

        NPC.loadSkin(ign);

    }

    @Override
    public void onAbilityStart() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        Slime slime = (Slime) player.getWorld().spawnEntity(player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(0.5)), EntityType.SLIME);
        slime.setSize(2);
        slime.setVelocity(player.getEyeLocation().getDirection().multiply(0.5));
        slime.setMetadata("mobSmashKit", new FixedMetadataValue(Main.getInstance(), "slime"));
        slime.setMetadata("source", new FixedMetadataValue(Main.getInstance(), player.getName()));

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

        List<Player> visibleTo = player.getWorld().getPlayers();

        Vector direction1 = player.getEyeLocation().getDirection().setY(0).normalize();
        replica1 = new NPC(ign, player.getLocation().add(direction1), visibleTo);
        Vector direction2 = player.getEyeLocation().getDirection().setY(0).normalize().rotateAroundY(1.0);
        replica2 = new NPC(ign, player.getLocation().add(direction2), visibleTo);
        Vector direction3 = player.getEyeLocation().getDirection().setY(0).normalize().rotateAroundY(-1.0);
        replica3 = new NPC(ign, player.getLocation().add(direction3), visibleTo);

        player.getWorld().playSound(ULTIMATE_SOUND, player);

    }

    @Override
    public void ultimateRun() {}

    @Override
    public void onUltimateEnd() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        replica1.remove();
        replica2.remove();
        replica3.remove();

    }

}
