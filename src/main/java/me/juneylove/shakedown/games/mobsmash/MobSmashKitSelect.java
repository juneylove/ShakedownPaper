package me.juneylove.shakedown.games.mobsmash;

import me.juneylove.shakedown.mechanics.KitSettings;
import me.juneylove.shakedown.scoring.TeamManager;
import me.juneylove.shakedown.ui.TextFormat;
import me.juneylove.shakedown.ui.Models;
import me.juneylove.shakedown.ui.GUIFormat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.IntStream;

import static me.juneylove.shakedown.ui.GUIFormat.menuSelectItem;

public class MobSmashKitSelect extends KitSettings.KitSetting {

    final int rows = 3;

    Random random;

    Inventory inventory;

    HashMap<String, Integer> selections = new HashMap<>();
    public HashMap<Map.Entry<String, Integer>, ItemStack[]> inventories = new HashMap<>();

    public MobSmashKitSelect() {

        random = new Random();

        this.selectionType = KitSettings.SelectionType.EVERY_ROUND;
        this.uniformItems = new ItemStack[]{GUIFormat.getMenuIcon(Models.KIT_SELECT)};

        String titleString = TextFormat.smallText("select kit");
        TextComponent.Builder title = Component.text().append(Component.text(titleString));
        title.append(TextFormat.negativeSpace(TextFormat.finalTextPos(titleString))); // replace
        title.append(GUIFormat.blockOutColumns(rows, List.of(1, 2, 3, 4, 5, 6, 7))); // replace

        //title.append(TextFormat.NegativeSpace(TextFormat.FinalTextPos(titleString)+34));
        //title.append(Component.text("\uE06D").color(NamedTextColor.WHITE));

        inventory = Bukkit.createInventory(null, rows*9, title.build());

        int slot = 1;
        for (Kits kit : Kits.values()) {

            if (slot%9 == 8) slot = slot+2;
            if (slot >= 27) break;

            ItemStack item = new ItemStack(menuSelectItem);
            ItemMeta meta = Bukkit.getServer().getItemFactory().getItemMeta(menuSelectItem);
            meta.displayName(kit.kit.displayName);
            meta.setCustomModelData(kit.unselectedModel);
            meta.lore(kit.kit.description);
            item.setItemMeta(meta);

            inventory.setItem(slot, item);

            slot++;

        }

    }

    public void openKitSelection(Player player) {

        player.getInventory().clear();
        player.openInventory(inventory);
        updateInventory(player);

    }

    public void onCloseKitSelection(Player player) {

        player.getInventory().clear();
        player.getInventory().setContents(uniformItems);

    }

    public void onKitSelect(Player player, int slotClicked) {

        if (slotClicked%9 == 8 || slotClicked%9 == 0) return;

        int choice;
        if (slotClicked < 9) {
            choice = slotClicked-1;
        } else if (slotClicked < 18) {
            choice = slotClicked-3;
        } else {
            choice = slotClicked-5;
        }

        if (getWhoSelectedKit(player.getName(), choice) != null) return;

        Kits chosenKit = Kits.getByIndex(choice);

        if (chosenKit != null) {
            selections.put(player.getName(), choice);

            MobKit kit = chosenKit.kit;
            kit.ign = player.getName();
            AbilityManager.kits.put(player.getName(), kit);

            updateTeamInventories(TeamManager.getTeam(player.getName()));
        }


    }

    private void updateTeamInventories(String team) {

        Set<String> teamMembers = TeamManager.getMembers(team);
        for (String member : teamMembers) {
            Player player = Bukkit.getPlayer(member);
            if (player != null && player.getOpenInventory().getType() != InventoryType.PLAYER) {

                // Team member has kit selection menu open
                updateInventory(player);

            }
        }

    }

    private void updateInventory(Player player) {

        int slot = 1;
        int kitNumber = 0;
        for (Kits kit : Kits.values()) {

            if (slot%9 == 8) slot = slot+2;
            if (slot >= 27) break;

            ItemStack item = inventory.getItem(slot);
            ItemMeta meta  = item.getItemMeta();

            TextComponent displayName = kit.kit.displayName;

            String whoSelected = getWhoSelectedKit(player.getName(), kitNumber);
            if (whoSelected != null) {

                if (whoSelected.equals(player.getName())) {
                    meta.setCustomModelData(kit.userSelectedModel);
                    displayName = displayName.append(Component.text(" [Selected]").color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD));
                } else {
                    meta.setCustomModelData(kit.teamSelectedModel);
                    displayName = displayName.append(Component.text(" [" + whoSelected + "]").color(NamedTextColor.YELLOW).decorate(TextDecoration.BOLD));
                }

            } else {

                meta.setCustomModelData(kit.unselectedModel);

            }

            meta.displayName(displayName);
            meta.lore(kit.kit.description);
            item.setItemMeta(meta);
            player.getOpenInventory().setItem(slot, item);

            kitNumber++;
            slot++;

        }

    }

    private String getWhoSelectedKit(String ign, int kitNumber) {

        Set<String> teamMembers = TeamManager.getMembers(TeamManager.getTeam(ign));
        for (String member : teamMembers) {
            if (selections.containsKey(member) && selections.get(member) == kitNumber) {
                return member;
            }
        }
        return null;

    }

    public void endKitSelection(World world) {

        List<Player> players = world.getPlayers();

        for (Player player : players) {
            if (TeamManager.isGamePlayer(player.getName())) {

                if (player.getOpenInventory().getType() != InventoryType.PLAYER) {

                    // still has kit selection menu open
                    player.closeInventory();

                }

                if (!selections.containsKey(player.getName())) {

                    // Player has not selected kit, give random remaining kit
                    List<Integer> unclaimedKits = new ArrayList<>(IntStream.rangeClosed(0, Kits.values().length - 1).boxed().toList());

                    String team = TeamManager.getTeam(player.getName());
                    Set<String> teamMembers = TeamManager.getMembers(team);
                    for (String member : teamMembers) {
                        if (selections.containsKey(member)) unclaimedKits.remove(selections.get(member));
                    }

                    int randomIndex = random.nextInt(unclaimedKits.size());
                    int chosenKit = unclaimedKits.get(randomIndex);
                    selections.put(player.getName(), chosenKit);

                    MobKit kit = Kits.getByIndex(chosenKit).kit;
                    kit.ign = player.getName();
                    AbilityManager.kits.put(player.getName(), kit);

                }

                String ign = player.getName();
                int choice = selections.get(ign);
                Map.Entry<String, Integer> entry = Map.entry(ign, choice);

                if (inventories.containsKey(entry)) { // same player has previously chosen same kit
                    KitSettings.giveKit(player, inventories.get(entry));
                } else { // new kit for this player, give default kit arrangement
                    KitSettings.giveKit(player, Kits.getByIndex(selections.get(ign)).kit.kit);
                }

                for (TextComponent textComponent : Kits.getByIndex(selections.get(ign)).kit.details) {
                    player.sendMessage(textComponent);
                }

            }
        }

    }

    @Override
    public void onRespawn(Player player) {

        String ign = player.getName();
        int choice = selections.get(ign);
        Map.Entry<String, Integer> entry = Map.entry(ign, choice);

        if (inventories.containsKey(entry)) { // same player has previously chosen same kit
            KitSettings.giveKit(player, inventories.get(entry));
        } else { // new kit for this player, give default kit arrangement
            KitSettings.giveKit(player, Kits.getByIndex(selections.get(ign)).kit.kit);
        }

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

                int choice = selections.get(player.getName());
                Map.Entry<String, Integer> entry = Map.entry(player.getName(), choice);
                ItemStack[] contents = player.getInventory().getContents().clone();
                ItemStack[] copy = new ItemStack[contents.length];
                int i = 0;
                for (ItemStack item : contents) {
                    if (item == null) copy[i] = null;
                    else copy[i] = item.clone();
                    i++;
                }
                inventories.put(entry, copy);

            }
        }

    }

}
