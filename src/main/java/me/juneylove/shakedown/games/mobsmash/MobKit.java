package me.juneylove.shakedown.games.mobsmash;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import io.papermc.paper.event.entity.EntityLoadCrossbowEvent;
import me.juneylove.shakedown.ui.TextFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class MobKit {

    public static final TextColor weaponColor = NamedTextColor.YELLOW;
    public static final TextColor abilityColor = NamedTextColor.AQUA;
    public static final TextColor ultimateColor = NamedTextColor.LIGHT_PURPLE;

    public static final TextComponent abilityDivider = (Component.text(TextFormat.singleLine(25) + " [" + TextFormat.PLUS_ONE_PX)
            .append(Component.keybind("key.swapOffhand"))
            .append(Component.text(TextFormat.PLUS_ONE_PX + "] " + TextFormat.smallText("ability " + TextFormat.singleLine(25)))))
            .color(abilityColor)
            .decoration(TextDecoration.ITALIC, false);
    public static final TextComponent ultimateDivider = (Component.text(TextFormat.singleLine(25) + " [" + TextFormat.PLUS_ONE_PX)
            .append(Component.keybind("key.drop"))
            .append(Component.text(TextFormat.PLUS_ONE_PX + "] " + TextFormat.smallText("ultimate " + TextFormat.singleLine(25)))))
            .color(ultimateColor)
            .decoration(TextDecoration.ITALIC, false);
    public static final TextComponent detailsDivider = Component.text(TextFormat.singleLine(250)).color(weaponColor);
    public static final TextComponent detailsHeader = Component.text(TextFormat.smallText("kit details:")).color(NamedTextColor.GOLD);

    public String ign;
    public int kitID;

    public double maxHealth = 20.0;
    public ItemStack[] kit;
    public TextComponent displayName;
    public List<TextComponent> description;
    public List<TextComponent> details;

    public double abilityDurationSeconds;
    public double abilityCooldownSeconds;
    public double ultimateDurationSeconds;
    public int    ultPointsRequired;

    public int abilityDurationRemainingTicks = 0;
    public int abilityCooldownRemainingTicks = 0;
    public int ultimateDurationRemainingTicks = 0;
    public int currentUltPoints = 0;

    public void applyKit() {}
    public void passiveRun() {}

    public void onUseItem(PlayerInteractEvent event) {}
    public void onJump(PlayerJumpEvent event) {}
    public void onDamageByEntity(EntityDamageByEntityEvent event) {}
    public void onLoadCrossbow(EntityLoadCrossbowEvent event) {}
    public void onDeath() {}

    public abstract void onAbilityStart();
    public abstract void abilityRun();
    public abstract void onAbilityEnd();

    public abstract void onUltimateStart();
    public abstract void ultimateRun();
    public abstract void onUltimateEnd();

}
