package com.blocky.dev.rushshop.auctionhouse.gui;

import com.blocky.dev.Config;
import com.blocky.dev.Configuration;
import com.blocky.dev.GUI;
import com.blocky.dev.Misc;
import com.blocky.dev.filemanager.FileID;
import com.blocky.dev.rushshop.RushShop;
import com.blocky.dev.rushshop.auctionhouse.AuctionHouseOrdering;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuctionHouseMainGUI extends GUI {

    private static final Logger LOGGER = RushShop.getInstance().getLogger();
    private final List<Integer> viewSlotsList = new ArrayList<>();
    private final Map<Integer, String> itemUUIDs = new HashMap<>();
    private int currentPage = 1;
    private final int order;
    private int maxPages;

    public AuctionHouseMainGUI(HumanEntity entity) {
        super(FileID.AUCTION_HOUSE_GUI, "menu");
        this.order = Config.i.getInt("auction_house.order", 0);
        registerSelf();
        initializeItems();
        loadPage();
        entity.openInventory(inv);
    }

    private void clearViewSlots() {
        for (int i : viewSlotsList) {
            inv.setItem(i, null);
        }
    }
    @Override
    public void setItem(String itemID, int slot, Inventory inv) {
        if (getItemsMap().containsKey(itemID)) {
            inv.setItem(slot, getItemsMap().get(itemID));
            return;
        }
        if ("{view}".equals(itemID)) {
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
        clearViewSlots();
        Configuration dataConfig = new Configuration(RushShop.getInstance().getFileManager().getFile(FileID.AUCTION_DATA).getFileConfiguration());
        int startingIndex = (currentPage - 1) * viewSlotsList.size();
        int endIndex = currentPage * viewSlotsList.size();
        int size = AuctionHouseOrdering.baseOrder.size();
        maxPages = (int) Math.ceil((double) size / viewSlotsList.size());
        List<String> pageKeys = getOrderedList(startingIndex, endIndex);
        for (int i : viewSlotsList) {
            if (i >= inv.getSize()) {
                continue;
            }
            if (pageKeys.isEmpty()) {
                return;
            }
            setViewItem(dataConfig.getConfiguration(pageKeys.get(0)).self(), i);

            pageKeys.remove(0);
        }
    }
    private List<String> getOrderedList(int start, int end) {
        List<String> base = AuctionHouseOrdering.baseOrder;
        return switch (order) {
            /*
             0 = Base Order
             1 = Price Ascending
             */
            case 0 -> base.subList(Math.min(base.size(), start), Math.min(base.size(), end));
            case 1 -> AuctionHouseOrdering.priceOrderLowToHigh.subList(Math.min(base.size(), start), Math.min(base.size(), end));
            default -> throw new IllegalStateException("Unexpected value: " + order);
        };
    }
    private void setViewItem(ConfigurationSection section, int slot) {
        if (section == null) {
            return;
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
        newItem.setItemMeta(meta);
        newItem.setAmount(baseItem.getAmount());
        inv.setItem(slot, newItem);
        itemUUIDs.put(slot, section.getName());
    }
    @EventHandler
    public void event(InventoryClickEvent event){
        if (!inv.equals(event.getInventory())) {
            return;
        }
        event.setCancelled(true);
        if (!inv.equals(event.getClickedInventory())) {
            return;
        }
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
        String auctionID = itemUUIDs.get(event.getSlot());
        new AuctionHousePurchaseGUI(event.getWhoClicked(), auctionID);
    }
}
