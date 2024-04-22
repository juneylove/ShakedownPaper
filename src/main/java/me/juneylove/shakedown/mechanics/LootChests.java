package me.juneylove.shakedown.mechanics;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class LootChests {

    protected static final int LEVEL_1_ROWS = 3;
    protected static final int LEVEL_2_ROWS = 4;
    protected static final int LEVEL_3_ROWS = 5;
    protected static final int LEVEL_4_ROWS = 6;

    private static final HashSet<Material> UsedSaplings = new HashSet<>(Arrays.asList(
            Material.SPRUCE_SAPLING,
            Material.OAK_SAPLING,
            Material.ACACIA_SAPLING,
            Material.BIRCH_SAPLING,
            Material.JUNGLE_SAPLING,
            Material.DARK_OAK_SAPLING,
            Material.CHERRY_SAPLING
    ));

    @SuppressWarnings("FieldMayBeFinal")
    private static Set<String> JustOpenedChest = new HashSet<>();

    public static void ClearRecentOpens() {
        JustOpenedChest.clear();
    }

    public static class LootChest {
/*
        LEVEL_1_NORTH(1, BlockFace.NORTH), // SPRUCE 0
        LEVEL_1_SOUTH(1, BlockFace.SOUTH), // SPRUCE 1
        LEVEL_1_EAST (1, BlockFace.EAST),  // OAK 0
        LEVEL_1_WEST (1, BlockFace.WEST),  // OAK 1
        LEVEL_2_NORTH(2, BlockFace.NORTH), // ACACIA 0
        LEVEL_2_SOUTH(2, BlockFace.SOUTH), // ACACIA 1
        LEVEL_2_EAST (2, BlockFace.EAST),  // BIRCH 0
        LEVEL_2_WEST (2, BlockFace.WEST),  // BIRCH 1
        LEVEL_3_NORTH(3, BlockFace.NORTH), // JUNGLE 0
        LEVEL_3_SOUTH(3, BlockFace.SOUTH), // JUNGLE 1
        LEVEL_3_EAST (3, BlockFace.EAST),  // DARK OAK 0
        LEVEL_3_WEST (3, BlockFace.WEST);  // DARK OAK 1
        LEVEL_4_NORTH(4, BlockFace.NORTH), // CHERRY 0
        LEVEL_4_SOUTH(4, BlockFace.SOUTH), // CHERRY 1
*/
        public final int level;
        public final BlockFace front;
        public final Material sapling;
        public final int stage;

        public LootChest(int level, BlockFace front) {

            this.level = level;
            this.front = front;

            if (front == BlockFace.NORTH || front == BlockFace.SOUTH) {

                this.sapling = switch (level) {
                    default -> Material.SPRUCE_SAPLING;
                    case 2  -> Material.ACACIA_SAPLING;
                    case 3  -> Material.JUNGLE_SAPLING;
                    case 4  -> Material.CHERRY_SAPLING;
                };

            } else {

                this.sapling = switch (level) {
                    default -> Material.OAK_SAPLING;
                    case 2  -> Material.BIRCH_SAPLING;
                    case 3  -> Material.DARK_OAK_SAPLING;
                    case 4  -> Material.CHERRY_SAPLING;
                };

            }

            // Restrict level 4 to north/south because we only have one sapling to work with
            if (level == 4) {
                if (front == BlockFace.EAST) front = BlockFace.NORTH;
                if (front == BlockFace.WEST) front = BlockFace.SOUTH;
            }

            if (front == BlockFace.NORTH || front == BlockFace.EAST) {
                this.stage = 0;
            } else {
                this.stage = 1;
            }

        }

    }

    public static int ChestLevel(Material sapling) {

        return switch (sapling) {
            default -> 1;
            case ACACIA_SAPLING, BIRCH_SAPLING -> 2;
            case JUNGLE_SAPLING, DARK_OAK_SAPLING -> 3;
            case CHERRY_SAPLING -> 4;
        };

    }

    public static boolean IsLootChest(Material type) {
        return UsedSaplings.contains(type);
    }

    public static Optional<Location> FindCustomChest(Player player) {

        if (JustOpenedChest.contains(player.getName())) {
            return Optional.empty();
        }

        Block target = player.getTargetBlockExact(5);

        if (target != null) {

            if (IsLootChest(target.getType())) {
                return Optional.of(target.getLocation());
            }

            if (target.getType() == Material.LILY_PAD) {

                Block below = target.getRelative(BlockFace.DOWN);
                if (IsLootChest(below.getType())) {

                    Vector eyeLoc = player.getEyeLocation().toVector();
                    Vector direction = player.getEyeLocation().getDirection();
                    double reachDistance = 4.6; // dialed in through lots of testing! even though it seems wrong! do not change!
                    RayTraceResult trace = below.getBoundingBox().rayTrace(eyeLoc, direction, reachDistance);

                    if (trace != null) {
                        return Optional.of(below.getLocation());
                    }

                }

            }

        }
        return Optional.empty();

    }

    public static void OpenChest(@NotNull Location loc, Player player) {

        JustOpenedChest.add(player.getName()); // need this list because every interact calls the interact listener like 2-4 times

        //noinspection ConstantConditions
        loc.getWorld().playSound(loc, Sound.BLOCK_CHEST_OPEN, 1, 1);

        int chestLevel = ChestLevel(loc.getBlock().getType());
        int rows = switch (chestLevel) {
            case 1  -> LEVEL_1_ROWS;
            case 2  -> LEVEL_2_ROWS;
            case 3  -> LEVEL_3_ROWS;
            default -> LEVEL_4_ROWS;
        };

        Inventory inv = Bukkit.createInventory(player, rows*9, "Level " + chestLevel + " Chest");
        PopulateLoot(loc, chestLevel, inv);
        player.openInventory(inv);

        loc.getBlock().setType(Material.AIR, false);
        loc.add(0, 1, 0).getBlock().setType(Material.AIR, false);

    }

    public static void PopulateLoot(Location loc, int chestLevel, Inventory inv) {

        List<LootChestTable.LootItem> loot = LootChestTable.RandomLoot(chestLevel);

        for (LootChestTable.LootItem item : loot) {
            ItemStack stack = EncodeAttributes(loc, new ItemStack(item.stack.getType(), item.stack.getAmount()));
            inv.setItem(item.index, stack);
        }

    }

    private static ItemStack EncodeAttributes(Location loc, ItemStack stack) {

        AttributeModifier x = new AttributeModifier("" + loc.getX(), 0.0, AttributeModifier.Operation.ADD_NUMBER);
        AttributeModifier y = new AttributeModifier("" + loc.getY(), 0.0, AttributeModifier.Operation.ADD_NUMBER);
        AttributeModifier z = new AttributeModifier("" + loc.getZ(), 0.0, AttributeModifier.Operation.ADD_NUMBER);

        ItemMeta meta = stack.getItemMeta();
        //noinspection ConstantConditions
        meta.addAttributeModifier(Attribute.GENERIC_FOLLOW_RANGE, x);
        meta.addAttributeModifier(Attribute.GENERIC_MOVEMENT_SPEED, y);
        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, z);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        // this should never happen!! defensive error detection
        if (stack.getType() == LootChestTable.ERROR_ITEM_1 ||
            stack.getType() == LootChestTable.ERROR_ITEM_2 ||
            stack.getType() == LootChestTable.ERROR_ITEM_3) {
            meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "Loot generation error, please report this!");
        }

        stack.setItemMeta(meta);
        return stack;

    }

}
