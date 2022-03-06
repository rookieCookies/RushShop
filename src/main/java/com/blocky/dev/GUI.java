package com.blocky.dev;

import com.blocky.dev.filemanager.FileID;
import com.blocky.dev.rushshop.RushShop;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("ALL")
public class GUI implements Listener {
    private static final Logger LOGGER = RushShop.getInstance().getLogger();
    private static final Map<String, ItemStack> itemsMap = new HashMap<>();
    private final Configuration items;
    private final Configuration config;

    public final Inventory inv;

    protected GUI(FileID fileID, String guiID) {
        config = new Configuration(
                Misc.getConfigurationSection(
                        RushShop.getInstance().getFileManager().getFile(fileID).getFileConfiguration(),
                        guiID));
        items = new Configuration(
                Misc.getConfigurationSection(
                        RushShop.getInstance().getFileManager().getFile(fileID).getFileConfiguration(),
                        "items")
        );
        this.inv = Bukkit.createInventory(
                null,
                Math.min(config.getInt("size", 6), 6) * 9,
                Misc.coloured(config.getString("title", "&6Auction House"))
        );
    }

    public final void registerSelf() {
        RushShop.getInstance().getServer().getPluginManager().registerEvents(this, RushShop.getInstance());
    }

    public void initializeItems() {
        inv.clear();
        List<String> keys = config.getStringList("items");
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
    public void setItem(String itemID, int slot, Inventory inv) {
        if (itemsMap.containsKey(itemID)) {
            inv.setItem(slot, itemsMap.get(itemID));
            return;
        }
        Configuration item = items.getConfiguration(itemID);
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
        itemsMap.put(itemID, itemStack);
    }
    public ItemStack getCustomItem(String itemID, Map<String, String> customValues){
        ConfigurationSection section = getConfig().getConfiguration(itemID).self();
        if (section == null) {
            return null;
        }
        List<String> baseLore = section.getStringList("lore");
        List<String> newLore = new ArrayList<>();
        for (String s : baseLore) {
            for (Map.Entry<String, String> entry : customValues.entrySet()) {
                s = s.replace(entry.getKey(), entry.getValue());
            }
            newLore.add(s);
        }
        ItemStack newItem = new ItemStack(Material.valueOf(section.getString("material", "STONE")), 1);
        ItemMeta meta = newItem.getItemMeta();
        assert meta != null;
        meta.setLore(newLore);
        meta.setDisplayName(section.getString("name"));
        newItem.setItemMeta(meta);
        newItem.setAmount(section.getInt("amount", 1));
        return newItem;
    }
    public boolean isThisButton(String buttonID, int slot) {
        Configuration section = getConfig().getConfiguration("buttons");
        if (section.self().isInt(buttonID)) {
            return section.getInt(buttonID) == slot;
        }
        return section.getStringList(buttonID).contains(String.valueOf(slot));
    }

    public Configuration getConfig() {
        return config;
    }
    public Inventory getInv() {
        return inv;
    }
    public Configuration getItems() {
        return items;
    }
    public static Map<String, ItemStack> getItemsMap() {
        return itemsMap;
    }
}
