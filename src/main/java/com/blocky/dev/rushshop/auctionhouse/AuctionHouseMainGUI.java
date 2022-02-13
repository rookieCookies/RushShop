package com.blocky.dev.rushshop.auctionhouse;

import com.blocky.dev.Configuration;
import com.blocky.dev.GUI;
import com.blocky.dev.Misc;
import com.blocky.dev.filemanager.FileID;
import com.blocky.dev.rushshop.RushShop;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuctionHouseMainGUI extends GUI {

    private static final Logger LOGGER = RushShop.getInstance().getLogger();
    private int viewSlots;
    private final List<Integer> viewSlotsList = new ArrayList<>();
    private int currentPage = 1;
    private int maxPages;

    AuctionHouseMainGUI(HumanEntity entity) {
        super(FileID.AUCTION_HOUSE_GUI, "menu");
        viewSlots = 0;
        registerSelf();
        initializeItems();
        loadPage();
        entity.openInventory(inv);
    }

    @Override
    public void initializeItems() {
        inv.clear();
        viewSlots = 0;
        List<String> keys = getConfig().getStringList("items");
        for (String i : keys) {
            if (!i.startsWith("%")) {
                LOGGER.severe("Adding items without a slot is not supported yet, Coming soon!");
                continue;
            }
            List<String> split = List.of(i.split("%"));
            String dataString = split.get(1);
            if (split.size() != 3) {
                LOGGER.log(Level.SEVERE, "No Item was provided in Auction House Menu GUI at slot {0}", i);
                continue;
            }
            String item = split.get(2).substring(1);
            if (!dataString.contains("-")) {
                setItem(item, Integer.parseInt(split.get(1)), inv);
                continue;
            }
            String[] data = dataString.split("-");
            for (int j = Integer.parseInt(data[0]); j <= Integer.parseInt(data[1]); j++) {
                setItem(item, j, inv);
            }
        }
    }
    @Override
    public void setItem(String itemID, int slot, Inventory inv) {
        if (getItemsMap().containsKey(itemID)) {
            inv.setItem(slot, getItemsMap().get(itemID));
            return;
        }
        if ("{view}".equals(itemID)) {
            viewSlots += 1;
            viewSlotsList.add(slot);
            return;
        }
        Configuration item = getItems().getConfiguration(itemID);
        if (item == null) {
            LOGGER.log(Level.SEVERE,"Item ID {0} was not found in the Auction House GUI", itemID);
            return;
        }
        ItemStack itemStack = new ItemStack(Objects.requireNonNull(
                Material.getMaterial(item.getString("material", "STONE"))));
        ItemMeta meta = itemStack.getItemMeta();
        assert meta != null;
        meta.setDisplayName(Misc.coloured(item.getString("display_name", "&6Item")));
        item.setLog(false);
        meta.setLore(Misc.colouredList(item.getStringList("lore")));
        item.setLog(true);
        itemStack.setItemMeta(meta);
        inv.setItem(slot, itemStack);
        getItemsMap().put(itemID, itemStack);
    }
    private void loadPage() {
        initializeItems();
        Configuration dataConfig = new Configuration(RushShop.getInstance().getFileManager().getFile(FileID.AUCTION_DATA).getFileConfiguration());
        List<String> keys = new ArrayList<>(dataConfig.getKeys());
        int startingIndex = (currentPage - 1) * viewSlots;
        int endIndex = currentPage * viewSlots;
        maxPages = (int) Math.ceil((double) keys.size() / viewSlots);
        List<String> pageKeys = keys.subList(Math.min(keys.size(), startingIndex), Math.min(keys.size(), endIndex));
        // System.log.println keys, startingIndex, endIndex, pageKeys, maxPages, viewSlots
        for (int i = 0; i < inv.getSize(); i++) {
            if (inv.getItem(i) != null) {
                continue;
            }
            if (pageKeys.isEmpty()) {
                inv.setItem(i, new ItemStack(Material.AIR));
                continue;
            }
            inv.setItem(i, createViewItem(dataConfig.getConfiguration(pageKeys.get(0)).self()));
            pageKeys.remove(0);
        }
    }
    private static ItemStack createViewItem(ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        ItemStack baseItem = section.getItemStack("item", new ItemStack(Material.STONE));
        List<String> baseLore = Misc.getMessageList("auction_house.gui.display_item.lore");
        List<String> newLore = new ArrayList<>();
        for (String s : baseLore) {
            newLore.add(s
                    .replace("{owner}", Objects.requireNonNull(Bukkit.getPlayer(UUID.fromString(section.getString("owner", "")))).getDisplayName())
                    .replace("{price}", String.valueOf(section.getDouble("price", 0)))
            );
        }
        ItemStack newItem = new ItemStack(baseItem.getType(), 1);
        newItem.setItemMeta(baseItem.getItemMeta());
        ItemMeta meta = newItem.getItemMeta();
        assert meta != null;
        meta.setLore(newLore);
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(new NamespacedKey(RushShop.getInstance(), "auction_id"), PersistentDataType.STRING, section.getName());
        newItem.setItemMeta(meta);
        newItem.setAmount(baseItem.getAmount());
        return newItem;
    }
    @EventHandler
    public void event(InventoryClickEvent event){
        if (!inv.equals(event.getInventory())) {
            return;
        }
        event.setCancelled(true);
        if (event.getSlot() == getConfig().getInt("buttons.CLOSE")) {
            event.getWhoClicked().closeInventory();
            return;
        } else if (event.getSlot() == getConfig().getInt("buttons.NEXT_PAGE")) {
            if (currentPage == maxPages) {
                return;
            }
            currentPage += 1;
            loadPage();
            return;
        } else if (event.getSlot() == getConfig().getInt("buttons.PREVIOUS_PAGE")) {
            if (currentPage == 1) {
                return;
            }
            currentPage -= 1;
            loadPage();
            return;
        } else if (!viewSlotsList.contains(event.getSlot())) {
            return;
        }
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(new NamespacedKey(RushShop.getInstance(), "auction_id"), PersistentDataType.STRING)) {
            return;
        }
        String auctionID = pdc.get(new NamespacedKey(RushShop.getInstance(), "auction_id"), PersistentDataType.STRING);
        new AuctionHousePurchase(event.getWhoClicked(), auctionID);
    }
}
