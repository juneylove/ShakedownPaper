package me.juneylove.shakedown.mechanics;

import me.juneylove.shakedown.games.Games;
import me.juneylove.shakedown.mechanics.abilities.HealBow;
import me.juneylove.shakedown.scoring.TeamManager;
import me.juneylove.shakedown.ui.TextFormat;
import me.juneylove.shakedown.ui.Models;
import me.juneylove.shakedown.ui.GUIFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.IntStream;

public class KitSettings {

    private Random random = null;

    private List<List<Integer>> kitColumns = new ArrayList<>();

    {
        kitColumns.add(0, List.of(4));
        kitColumns.add(1, List.of(3, 5));
        kitColumns.add(2, List.of(2, 4, 6));
        kitColumns.add(3, List.of(1, 3, 5, 7));
        kitColumns.add(4, List.of(0, 2, 4, 6, 8));
        kitColumns.add(5, List.of(1, 2, 3, 5, 6, 7));
        kitColumns.add(6, List.of(1, 2, 3, 4, 5, 6, 7));
        kitColumns.add(7, List.of(0, 1, 2, 3, 5, 6, 7, 8));
        kitColumns.add(8, List.of(0, 1, 2, 3, 4, 5, 6, 7, 8));
    }

    //=========================================================================

    public enum SelectionType {
        NONE,
        FIRST_ROUND,
        EVERY_ROUND
    }

    public abstract static class KitSetting {
        public boolean copiedToNextRound;
        public SelectionType selectionType = SelectionType.NONE;
        public ItemStack[] uniformItems;
        public abstract void setInventories(World world);
        public abstract void onRoundStart(World world);
        public abstract void onRespawn(Player player);
    }

    //=========================================================================

    public class NoKit extends KitSetting {

        @Override
        public void setInventories(World world) {

            List<Player> players = world.getPlayers();
            for (Player player : players) {
                if (TeamManager.isGamePlayer(player.getName())) {

                    player.getInventory().clear();

                }
            }

        }

        @Override
        public void onRoundStart(World world) {}

        @Override
        public void onRespawn(Player player) {}

    }

    public class Uniform extends KitSetting {

        public HashMap<String, ItemStack[]> inventories = new HashMap<>();

        public Uniform(HashMap<Integer, ItemStack> items) {
            uniformItems = populateInventory(items);
        }

        public Uniform(ItemStack[] items) {
            uniformItems = items;
        }

        @Override
        public void setInventories(World world) {

            List<Player> players = world.getPlayers();
            for (Player player : players) {
                if (TeamManager.isGamePlayer(player.getName())) {

                    if (inventories.containsKey(player.getName())) {
                        giveKit(player, inventories.get(player.getName()));
                    } else {
                        giveKit(player, uniformItems);
                    }

                }
            }

        }

        @Override
        public void onRoundStart(World world) {

            if (!copiedToNextRound) return;

            List<Player> players = world.getPlayers();
            for (Player player : players) {
                if (TeamManager.isGamePlayer(player.getName())) {
                    inventories.put(player.getName(), player.getInventory().getContents());
                }
            }

        }

        @Override
        public void onRespawn(Player player) {

        }

    }

    public class Persistent extends KitSetting {

        ItemStack[] firstRoundItems;

        public Persistent(ItemStack[] firstRoundItems, ItemStack[] everyRoundItems) {

            this.firstRoundItems = firstRoundItems;
            this.uniformItems = everyRoundItems;

        }

        @Override
        public void setInventories(World world) {

            List<Player> players = world.getPlayers();
            for (Player player : players) {

                if (Games.CURRENT_GAME.currentRoundNum == 1) {
                    // For round 1, start with firstRoundItems
                    ItemStack[] newContents = combineItemSets(firstRoundItems, uniformItems);
                    giveKit(player, newContents);
                } else {
                    // Add everyRoundItems to existing inventory for all other rounds
                    giveKit(player, uniformItems, true);
                }

            }

        }

        @Override
        public void onRoundStart(World world) {
        }

        @Override
        public void onRespawn(Player player) {

        }

    }

    public class Selection extends KitSetting {

        public HashMap<Map.Entry<String, Integer>, ItemStack[]> inventories = new HashMap<>();
        ItemStack[] itemsForEveryone;
        HashMap<String, Integer> playerChoices;
        public List<ItemStack[]> kitOptions; // just the option set, without any common items
        public List<ItemStack[]> fullKits; // each entry includes both items everyone gets and one option set

        int numOfKits;
        List<Integer> columns;
        int rows;
        Inventory inventory;

        public Selection(boolean reselectEveryRound, ItemStack[] itemsForEveryone, List<ItemStack[]> options) {

            if (random == null) random = new Random();

            if (reselectEveryRound) {
                this.selectionType = SelectionType.EVERY_ROUND;
            } else {
                this.selectionType = SelectionType.FIRST_ROUND;
            }

            this.uniformItems = new ItemStack[]{GUIFormat.getMenuIcon(Models.KIT_SELECT)};
            this.itemsForEveryone = itemsForEveryone;
            this.playerChoices = new HashMap<>();
            this.kitOptions = new ArrayList<>();
            for (ItemStack[] option : options) {
                if (option.length == 0) continue;
                kitOptions.add(option);
            }
            this.fullKits = new ArrayList<>();
            int optionCount = 0;
            for (ItemStack[] option : options) {
                fullKits.add(combineItemSets(this.itemsForEveryone, option));
                optionCount++;
                if (optionCount >= 9) break;
            }

            this.numOfKits = kitOptions.size();
            this.columns = kitColumns.get(numOfKits-1);

            int largestKitSize = 0;
            for (ItemStack[] kit : kitOptions) {
                if (kit.length > largestKitSize) largestKitSize = kit.length;
            }
            this.rows = Math.min(largestKitSize+1, 6);

            TextComponent inventoryTitle = kitSelectTitle(rows, columns);
            this.inventory = Bukkit.createInventory(null, 9*rows, inventoryTitle);

            int kitIndex = 0;
            for (int column : columns) {

                ItemStack[] kit = kitOptions.get(kitIndex);
                for (int row=1; row<rows; row++) {
                    if (row-1 >= kit.length) break;
                    inventory.setItem(9*row + column, kit[row-1]);
                }
                kitIndex++;

            }

        }

        public void endKitSelection(World world) {

            List<Player> players = world.getPlayers();

            for (Player player : players) {
                if (TeamManager.isGamePlayer(player.getName())) {

                    if (!playerChoices.containsKey(player.getName())) {

                        // Player has not selected kit, give random remaining kit
                        List<Integer> unclaimedKits = new ArrayList<>(IntStream.rangeClosed(0, numOfKits - 1).boxed().toList());

                        String team = TeamManager.getTeam(player.getName());
                        Set<String> teamMembers = TeamManager.getMembers(team);
                        for (String member : teamMembers) {
                            if (playerChoices.containsKey(member)) unclaimedKits.remove(playerChoices.get(member));
                        }

                        int randomIndex = random.nextInt(unclaimedKits.size());
                        int chosenKit = unclaimedKits.get(randomIndex);
                        playerChoices.put(player.getName(), chosenKit);

                    }

                    String ign = player.getName();
                    int choice = playerChoices.get(ign);
                    Map.Entry<String, Integer> entry = Map.entry(ign, choice);

                    if (inventories.containsKey(entry)) {
                        giveKit(player, inventories.get(entry));
                    } else {
                        giveKit(player, fullKits.get(playerChoices.get(player.getName())));
                    }


                }
            }

        }

        public void openKitSelect(Player player) {

            String team = TeamManager.getTeam(player.getName());
            Set<String> teamMembers = TeamManager.getMembers(team);

            for (int column : columns) {
                this.inventory.setItem(column, GUIFormat.getMenuIcon(Models.UNCLAIMED_KIT));
            }

            for (String member : teamMembers) {

                if (playerChoices.containsKey(member)) {

                    int column = columns.get(playerChoices.get(member));

                    if (member.equals(player.getName())) {
                        ItemStack chosenKitIndicator = GUIFormat.getMenuIcon(Models.SELECTED_KIT);
                        this.inventory.setItem(column, chosenKitIndicator);
                    } else {
                        ItemStack memberIcon = GUIFormat.customItemName(GUIFormat.getTeamMemberIcon(team),
                                Component.text(player.getName()).color(TextFormat.GetTextColor(team)).decorate(TextDecoration.BOLD));
                        this.inventory.setItem(column, memberIcon);
                    }

                }

            }

            player.openInventory(this.inventory);

            Inventory bottomInventory = player.getOpenInventory().getBottomInventory();
            bottomInventory.clear();

            // Place common items (itemsForEveryone array) in lower section of inventory
            int slot = 18;
            for (ItemStack stack : itemsForEveryone) {
                bottomInventory.setItem(slot, stack);
                slot++;
                if (slot >= 36) break; // don't place anything in the hotbar
            }

        }

        public void onKitSelectClose(Player player) {

            if (TeamManager.isGamePlayer(player.getName())) {
                player.getInventory().clear();
                player.getInventory().setContents(uniformItems);
            }

        }

        public void onKitSelectClick(Player player, int slot) {

            int clickedColumn = slot%9;
            if (!columns.contains(clickedColumn)) return;

            int chosenKit = columns.indexOf(clickedColumn);
            if (getWhoSelectedKit(player.getName(), chosenKit) != null) return;

            selectKit(player.getName(), chosenKit);

        }

        private String getWhoSelectedKit(String ign, int kitNumber) {

            Set<String> teamMembers = TeamManager.getMembers(TeamManager.getTeam(ign));
            for (String member : teamMembers) {
                if (playerChoices.containsKey(member) && playerChoices.get(member) == kitNumber) {
                    return member;
                }
            }
            return null;

        }

        private void selectKit(String ign, int kitNumber) {

            int column = columns.get(kitNumber);
            Integer previousKit = playerChoices.put(ign, kitNumber);

            String team = TeamManager.getTeam(ign);
            ItemStack memberIcon = GUIFormat.customItemName(GUIFormat.getTeamMemberIcon(team),
                    Component.text(ign).color(TextFormat.GetTextColor(team)).decorate(TextDecoration.BOLD));

            Set<String> teamMembers = TeamManager.getMembers(team);
            for (String member : teamMembers) {
                Player player = Bukkit.getPlayer(member);
                if (player != null && player.getOpenInventory().getType() == InventoryType.CHEST) {

                    // Team member of person who selected the kit has their kit selector open, update to reflect the change
                    player.getOpenInventory().getTopInventory().setItem(column, memberIcon);

                    if (previousKit != null) {
                        int previousKitColumn = columns.get(previousKit);
                        player.getOpenInventory().getTopInventory().setItem(previousKitColumn, GUIFormat.getMenuIcon(Models.UNCLAIMED_KIT));
                    }

                }
            }

            ItemStack chosenKitIndicator = GUIFormat.getMenuIcon(Models.SELECTED_KIT);
            Player player = Bukkit.getPlayer(ign);
            if (player != null) {
                player.getOpenInventory().getTopInventory().setItem(column, chosenKitIndicator);
            }

        }

        private TextComponent kitSelectTitle(int rows, List<Integer> columns) {

            String titleString = TextFormat.smallText("select kit");
            TextComponent.Builder title = Component.text().append(Component.text(titleString));
            title.append(TextFormat.negativeSpace(TextFormat.finalTextPos(titleString)+1));
            title.append(GUIFormat.ALL_KITS_INCLUDE[rows-1]);
            title.append(TextFormat.negativeSpace(162));
            title.append(GUIFormat.blockOutColumns(rows, columns));

            return title.build();

        }

        @Override
        public void setInventories(World world) {

            List<Player> players = world.getPlayers();

            for (Player player : players) {
                if (TeamManager.isGamePlayer(player.getName())) {
                    player.getInventory().clear();
                    player.getInventory().setContents(uniformItems);
                }
            }

        }

        @Override
        public void onRoundStart(World world) {

            if (!copiedToNextRound) return;

            List<Player> players = world.getPlayers();
            for (Player player : players) {
                if (TeamManager.isGamePlayer(player.getName())) {

                    String ign = player.getName();
                    int choice = playerChoices.get(ign);
                    Map.Entry<String, Integer> entry = Map.entry(ign, choice);
                    inventories.put(entry, player.getInventory().getContents());

                }
            }

        }

        @Override
        public void onRespawn(Player player) {
            
        }

    }

    //=========================================================================

    public static void giveKit(Player player, ItemStack[] kit, boolean keepInventory) {

        if (keepInventory) {
            player.getInventory().addItem(kit);
        } else {
            player.getInventory().clear();
            player.getInventory().setContents(kit);
        }

        // Check for custom item functions
        for (ItemStack item : kit) {
            if (item == null) continue;

            if (HealBow.isHealBow(item)) HealBow.SetHealer(player);

        }

    }

    public static void giveKit(Player player, ItemStack[] kit) {
        giveKit(player, kit, false);
    }

    private static ItemStack[] combineItemSets(ItemStack[]... addedItems) {

        Inventory newInventory = Bukkit.createInventory(null, InventoryType.PLAYER);

        for (ItemStack[] itemSet : addedItems) {
            newInventory.addItem(itemSet);
        }
        return newInventory.getContents();

    }

    private ItemStack[] populateInventory(HashMap<Integer, ItemStack> items) {

        ItemStack[] array = new ItemStack[36];

        for (int i=0; i<36; i++) {
            array[i] = items.getOrDefault(i, null);
        }
        return array;

    }

    //=========================================================================

}
