package me.juneylove.shakedown.mechanics.abilities;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class HealBow {

    @SuppressWarnings("FieldMayBeFinal")
    private static List<String> HEALERS = new ArrayList<>();

    private static final Sound HEAL_SOUND_EFFECT = Sound.BLOCK_RESPAWN_ANCHOR_CHARGE;
    private static final double HEALTH_BOOST = 4.0;

    public static ItemStack getItem() {

        ItemStack item = new ItemStack(Material.BOW);
        ItemMeta meta = Bukkit.getServer().getItemFactory().getItemMeta(Material.BOW);
        //noinspection ConstantConditions
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Healing Bow");
        meta.setCustomModelData(1);
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;

    }

    public static boolean isHealBow(ItemStack item) {

        return item.getType() == Material.BOW && item.hasItemMeta() && item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() == 1;

    }

    public static void SetHealer(String ign) {
        HEALERS.add(ign);
    }

    public static void SetHealer(Player player) {
        HEALERS.add(player.getName());
    }

    public static void ClearHealers() {
        HEALERS.clear();
    }

    public static boolean IsHealer(String ign) {
        return HEALERS.contains(ign);
    }

    public static void HealTeammate(Player healer, Player target, AbstractArrow arrow) {

        arrow.remove();

        target.setHealth(target.getHealth() + HEALTH_BOOST);
        target.playSound(target.getLocation(), HEAL_SOUND_EFFECT, 1.0f, 1.0f);

        healer.playSound(healer.getLocation(), HEAL_SOUND_EFFECT, 1.0f, 1.0f);

    }

}
