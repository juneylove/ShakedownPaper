package me.juneylove.shakedown.mechanics;

import me.juneylove.shakedown.Main;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class LootChestTable {

    private enum Side {
        LEFT(3),
        LEFT_MIDDLE(4),
        MIDDLE(5),
        RIGHT_MIDDLE(6),
        RIGHT(7);

        final int slotNumber;

        Side(int column) {
            slotNumber = column-1;
        }
    }

    static final Material ERROR_ITEM_1 = Material.FEATHER;
    static final Material ERROR_ITEM_2 = Material.SLIME_BALL;
    static final Material ERROR_ITEM_3 = Material.GUNPOWDER;

    static List<LootEntry> level1 = new ArrayList<>();
    static List<LootEntry> level2 = new ArrayList<>();
    static List<LootEntry> level3 = new ArrayList<>();
    static List<LootEntry> level4 = new ArrayList<>();

    static List<LootEntry> error1 = new ArrayList<>();
    static List<LootEntry> error2 = new ArrayList<>();

    public static boolean loaded = false;

    @SuppressWarnings("InnerClassMayBeStatic")
    protected class LootItem {

        ItemStack stack;
        int index;

        LootItem(Material type, int amount, int index) {

            this.stack = new ItemStack(type, amount);
            this.index = index;

        }

    }

    protected class LootEntry {

        int weight;
        List<LootItem> items;

        LootEntry(int weight, List<LootItem> items) {

            this.weight = weight;
            this.items = Objects.requireNonNullElseGet(items, ArrayList::new);

        }

    }

    private int Slot(int row, Side side) {
        return (row-1)*9 + side.slotNumber;
    }

    public void LoadLootTables() {

        // Default items for testing, *should* never come up
        error1.add(new LootEntry(1, null));
        error1.get(0).items.add(ParseLootItem(ERROR_ITEM_1.name() + " x 1 in row 1 middle", LootChests.LEVEL_1_ROWS));
        error2.add(new LootEntry(1, null));
        error2.get(0).items.add(ParseLootItem(ERROR_ITEM_2.name() + " x 1 in row 1 middle", LootChests.LEVEL_1_ROWS));

        // Load actual loot tables
        File folder = new File(Main.getInstance().getDataFolder(), "lootTables");
        File[] files = folder.listFiles();
        if (files == null) return;

        for (File file : files) {

            if (!file.getName().equals("level1.yml") && !file.getName().equals("level2.yml") && !file.getName().equals("level3.yml") && !file.getName().equals("level4.yml")) {
                Main.getInstance().getLogger().info("LoadLootTables: ignoring file " + file.getName());
                continue;
            }

            YamlConfiguration data = YamlConfiguration.loadConfiguration(file);
            int i = 0;
            for (String entry : data.getKeys(false)) {

                int tempWeight = data.getInt(entry + ".weight");
                switch (file.getName()) {

                    case "level1.yml":
                        level1.add(new LootEntry(tempWeight, null));
                        break;
                    case "level2.yml":
                        level2.add(new LootEntry(tempWeight, null));
                        break;
                    case "level3.yml":
                        level3.add(new LootEntry(tempWeight, null));
                        break;
                    case "level4.yml":
                        level4.add(new LootEntry(tempWeight, null));
                        break;

                }

                List<String> items = data.getStringList(entry + ".items");
                for (String item : items) {

                    switch (file.getName()) {

                        case "level1.yml":
                            level1.get(i).items.add(ParseLootItem(item, LootChests.LEVEL_1_ROWS));
                            break;
                        case "level2.yml":
                            level2.get(i).items.add(ParseLootItem(item, LootChests.LEVEL_2_ROWS));
                            break;
                        case "level3.yml":
                            level3.get(i).items.add(ParseLootItem(item, LootChests.LEVEL_3_ROWS));
                            break;
                        case "level4.yml":
                            level4.get(i).items.add(ParseLootItem(item, LootChests.LEVEL_4_ROWS));
                            break;

                    }

                }
                i++;

            }

        }
        loaded = true;

    }

    private LootItem ParseLootItem(String item, int maxRows) {

        Material type;
        try {
            type = Material.valueOf(item.split(" ")[0].replace(' ', '_').toUpperCase());
        } catch (Exception ignored) {
            type = ERROR_ITEM_3;
        }

        int amount;
        if (type.getMaxStackSize() == 1) {
            amount = 1;
        } else {
            try {
                amount = Integer.parseInt(item.split(" x ")[1].split(" ")[0]);
            } catch (Exception ignored) {
                amount = 1;
            }
        }

        int row;
        try {
            row = Integer.parseInt(item.split("row ")[1].split(" ")[0]);
        } catch (Exception ignored) {
            row = 1;
        }

        Side side;
        try {
            side = Side.valueOf(item.split("row " + row + " ")[1].toUpperCase());
        } catch (Exception ignored) {
            side = Side.MIDDLE;
        }

        if (row > maxRows) row = maxRows;

        return new LootItem(type, amount, Slot(row, side));

    }

    public static List<LootItem> RandomLoot(int chestLevel) {

        List<LootEntry> level = switch (chestLevel) {
            case 1  -> level1;
            case 2  -> level2;
            case 3  -> level3;
            case 4  -> level4;
            default -> error1;
        };

        int totalWeight = 0;
        for (LootEntry entry : level) {
            totalWeight += entry.weight;
        }

        int result = new Random().nextInt(totalWeight) + 1;
        for (LootEntry entry : level) {

            result -= entry.weight;
            if (result <= 0) {
                return entry.items;
            }

        }
        return error2.get(0).items;

    }

}
