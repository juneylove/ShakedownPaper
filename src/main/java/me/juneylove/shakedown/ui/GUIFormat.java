package me.juneylove.shakedown.ui;

import me.juneylove.shakedown.scoring.TeamManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class GUIFormat {

    public static final Component DANGER_ICON = Component.text("\uE04B");

    public static final Component[] EMPTY_COLUMNS = {
            Component.text("\uE061").color(NamedTextColor.WHITE),
            Component.text("\uE062").color(NamedTextColor.WHITE),
            Component.text("\uE063").color(NamedTextColor.WHITE),
            Component.text("\uE064").color(NamedTextColor.WHITE),
            Component.text("\uE065").color(NamedTextColor.WHITE),
            Component.text("\uE066").color(NamedTextColor.WHITE)};
    public static final Component[] ALL_KITS_INCLUDE = {
            Component.text("\uE067").color(NamedTextColor.WHITE),
            Component.text("\uE068").color(NamedTextColor.WHITE),
            Component.text("\uE069").color(NamedTextColor.WHITE),
            Component.text("\uE06A").color(NamedTextColor.WHITE),
            Component.text("\uE06B").color(NamedTextColor.WHITE),
            Component.text("\uE06C").color(NamedTextColor.WHITE)};

    public static Material menuSelectItem = Material.GHAST_TEAR;

    public static ItemStack getMenuIcon(Models model) {

        ItemStack item = new ItemStack(menuSelectItem);
        ItemMeta meta = Bukkit.getServer().getItemFactory().getItemMeta(menuSelectItem);
        meta.displayName(model.name);
        meta.setCustomModelData(model.num);
        item.setItemMeta(meta);

        return item;

    }

    public static boolean isMenuIcon(ItemStack stack, Models model) {

        return stack != null
                && stack.hasItemMeta()
                && stack.getItemMeta().hasCustomModelData()
                && stack.getItemMeta().getCustomModelData() == model.num;

    }

    public static ItemStack getTeamMemberIcon(String team) {

        int teamNumber = TeamManager.getTeamNumber(team);
        String modelName = "TEAM_" + teamNumber + "_MEMBER";
        Models model = Models.valueOf(modelName);
        return getMenuIcon(model);

    }

    public static ItemStack customItemName(Material material, Component name) {
        return customItemName(new ItemStack(material), name);
    }

    public static ItemStack customItemName(ItemStack stack, Component name) {

        ItemMeta meta;
        if (stack.hasItemMeta()) {
            meta = stack.getItemMeta();
        } else {
            meta = Bukkit.getServer().getItemFactory().getItemMeta(stack.getType());
        }
        meta.displayName(name);
        stack.setItemMeta(meta);
        return stack;

    }

    public static ItemStack addLore(Material material, List<Component> lore) {
        return addLore(new ItemStack(material), lore);
    }

    public static ItemStack addLore(ItemStack stack, List<Component> lore) {

        ItemMeta meta;
        if (stack.hasItemMeta()) {
            meta = stack.getItemMeta();
        } else {
            meta = Bukkit.getServer().getItemFactory().getItemMeta(stack.getType());
        }
        meta.lore(lore);
        stack.setItemMeta(meta);
        return stack;

    }

    public static ItemStack unbreakable(Material type) {

        return unbreakable(new ItemStack(type));

    }

    public static ItemStack unbreakable(ItemStack item) {

        ItemMeta meta = Bukkit.getServer().getItemFactory().getItemMeta(item.getType());
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        item.setItemMeta(meta);
        return item;

    }

    public static TextComponent blockOutColumns(int rows, List<Integer> usedColumns) {

        Component column = EMPTY_COLUMNS[rows-1].append(Component.text(TextFormat.NEGATIVE_ONE_PX));

        TextComponent.Builder str = Component.text().content(TextFormat.NEGATIVE_ONE_PX);

        for (int i=0; i<9; i++) {

            if (usedColumns.contains(i)) {
                str.append(TextFormat.padSpaces(18));
            } else {
                str.append(column);
            }

        }

        return str.build();

    }

}
