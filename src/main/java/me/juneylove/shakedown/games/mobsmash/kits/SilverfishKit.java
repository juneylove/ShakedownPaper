package me.juneylove.shakedown.games.mobsmash.kits;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import me.juneylove.shakedown.Main;
import me.juneylove.shakedown.games.mobsmash.MobKit;
import me.juneylove.shakedown.mechanics.Respawn;
import me.juneylove.shakedown.ui.GUIFormat;
import me.juneylove.shakedown.ui.TextFormat;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;

public class SilverfishKit extends MobKit {

    final Sound ABILITY_SOUND = Sound.sound(Key.key("entity.silverfish.ambient"), Sound.Source.WEATHER, 1.0f, 1.0f);
    final Sound STONE_PLACE = Sound.sound(Key.key("block.stone.place"), Sound.Source.WEATHER, 1.0f, 1.0f);
    final Sound STONE_BREAK = Sound.sound(Key.key("block.stone.break"), Sound.Source.WEATHER, 1.0f, 1.0f);
    final Sound ULTIMATE_SOUND = Sound.sound(Key.key("entity.silverfish.hurt"), Sound.Source.WEATHER, 5.0f, 1.0f);

    public static int bombSilverfish = 5;

    BlockDisplay topStoneBlock = null;
    BlockDisplay bottomStoneBlock = null;

    {
        abilityDurationSeconds = 4;
        abilityCooldownSeconds = 15;
        ultimateDurationSeconds = 0;
        ultPointsRequired = 5;

        displayName = Component.text("Silverfish").color(TextColor.color(0x828f92)).decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false);

        description = new ArrayList<>();
        description.add(Component.text("Primary weapon: Stone Sword").color(weaponColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(abilityDivider);
        description.add(Component.text("Heal inside a stone block, invulnerable but immobile").color(abilityColor)
                .decoration(TextDecoration.ITALIC, false));
        description.add(Component.empty());
        description.add(ultimateDivider);
        description.add(Component.text("Throw a bomb that explodes into several silverfish on impact").color(ultimateColor)
                .decoration(TextDecoration.ITALIC, false));

        details = new ArrayList<>();
        details.add(detailsDivider);
        details.add(detailsHeader);
        details.add(Component.text("Your ")

                .append(Component.text("ability").color(abilityColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.swapOffhand"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" briefly turns you to stone, so you can heal for " + TextFormat.formatNumber(abilityDurationSeconds) + " seconds. Your "))

                .append(Component.text("ultimate").color(ultimateColor))
                .append(Component.text(" [" + TextFormat.PLUS_ONE_PX))
                .append(Component.keybind("key.drop"))
                .append(Component.text(TextFormat.PLUS_ONE_PX + "] "))

                .append(Component.text(" instantly throws a bomb that explodes into " + bombSilverfish + " silverfish.")));
        details.add(detailsDivider);

        ItemStack sword = GUIFormat.unbreakable(Material.STONE_SWORD);
        sword.addEnchantment(Enchantment.KNOCKBACK, 1);
        kit = new ItemStack[]{sword};

    }

    @Override
    public void onDeath() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        player.removePotionEffect(PotionEffectType.REGENERATION);
        player.removePotionEffect(PotionEffectType.SLOW);
        player.removePotionEffect(PotionEffectType.INVISIBILITY);
        player.removePotionEffect(PotionEffectType.WEAKNESS);
        abilityDurationRemainingTicks = 0;

    }

    @Override
    public void onJump(PlayerJumpEvent event) {

        // do not allow player to jump while healing because they aren't supposed to be able to move
        if (abilityDurationRemainingTicks > 0) event.setCancelled(true);


    }

    @Override
    public void onDamageByEntity(EntityDamageByEntityEvent event) {

        // cancel damage from entities while healing/invulnerable
        if (abilityDurationRemainingTicks > 0) event.setCancelled(true);

    }

    @Override
    public void onAbilityStart() {

        // stone heal: becomes a stone brick block for brief period, providing healing and invulnerability but cannot move

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, (int) abilityDurationSeconds*20, 3, true, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW,         (int) abilityDurationSeconds*20, 7, true, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, (int) abilityDurationSeconds*20, 0, true, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS,     (int) abilityDurationSeconds*20, 1, true, false, false));

        Location stoneLocation = player.getLocation().add(-0.5, 0, -0.5);
        stoneLocation.setPitch(0.0f);
        stoneLocation.setYaw(0.0f);
        bottomStoneBlock = (BlockDisplay) player.getWorld().spawnEntity(stoneLocation, EntityType.BLOCK_DISPLAY);
        bottomStoneBlock.setBlock(Bukkit.createBlockData(Material.STONE_BRICKS));
        topStoneBlock = (BlockDisplay) player.getWorld().spawnEntity(stoneLocation.add(0, 1, 0), EntityType.BLOCK_DISPLAY);
        topStoneBlock.setBlock(Bukkit.createBlockData(Material.STONE_BRICKS));

        player.getWorld().playSound(ABILITY_SOUND, player);
        player.getWorld().playSound(STONE_PLACE, player);

    }

    @Override
    public void abilityRun() {

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        // get rid of stone block displays if player gets knocked into the void
        // while healing ability is active
        if (Respawn.IsTempSpec(ign)) {
            abilityDurationRemainingTicks = 0;
            onAbilityEnd();
            return;
        }

        Location stoneLocation = player.getLocation().add(-0.5, 0, -0.5);
        stoneLocation.setPitch(0.0f);
        stoneLocation.setYaw(0.0f);
        bottomStoneBlock.teleport(stoneLocation);
        bottomStoneBlock.setVelocity(player.getVelocity());
        topStoneBlock.teleport(stoneLocation.add(0, 1, 0));
        topStoneBlock.setVelocity(player.getVelocity());

    }

    @Override
    public void onAbilityEnd() {

        if (bottomStoneBlock != null) bottomStoneBlock.remove();
        if (topStoneBlock != null) topStoneBlock.remove();

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        // this if statement should always be true in a normal case, but prevents
        // sound from being played at the end of the round if the ability wasn't active
        if (abilityDurationRemainingTicks > 0) {
            player.getWorld().playSound(ABILITY_SOUND, player);
            player.getWorld().playSound(STONE_BREAK, player);
        }

    }

    @Override
    public void onUltimateStart() {

        // silverfish bomb: throws a bomb (snowball) which hatches silverfish on impact

        Player player = Bukkit.getPlayer(ign);
        if (player == null) return;

        Snowball bomb = (Snowball) player.getWorld().spawnEntity(player.getEyeLocation().add(player.getEyeLocation().getDirection().multiply(0.5)), EntityType.SNOWBALL);
        bomb.setVelocity(player.getEyeLocation().getDirection().multiply(1.15));
        bomb.setItem(new ItemStack(Material.SILVERFISH_SPAWN_EGG));
        bomb.setMetadata("mobSmashKit", new FixedMetadataValue(Main.getInstance(), "silverfish"));
        bomb.setMetadata("source", new FixedMetadataValue(Main.getInstance(), player.getName()));

        player.getWorld().playSound(ULTIMATE_SOUND, player);

    }

    @Override
    public void ultimateRun() {}

    @Override
    public void onUltimateEnd() {}

}
