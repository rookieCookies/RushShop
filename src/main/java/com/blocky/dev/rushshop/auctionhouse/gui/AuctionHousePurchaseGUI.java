package com.blocky.dev.rushshop.auctionhouse.gui;

import com.blocky.dev.Config;
import com.blocky.dev.Configuration;
import com.blocky.dev.GUI;
import com.blocky.dev.Misc;
import com.blocky.dev.filemanager.FileID;
import com.blocky.dev.rushshop.RushShop;
import com.blocky.dev.rushshop.auctionhouse.AuctionHouseOrdering;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuctionHousePurchaseGUI extends GUI {
    private static final Logger LOGGER = RushShop.getInstance().getLogger();
    private final Configuration item;

    public AuctionHousePurchaseGUI(HumanEntity entity, String id) {
        super(FileID.AUCTION_HOUSE_GUI, "purchase");
        item = new Configuration(
                Misc.getConfigurationSection(
                        RushShop.getInstance().getFileManager().getFile(FileID.AUCTION_DATA).getFileConfiguration(), id)
        );
        registerSelf();
        initializeItems();
        entity.openInventory(inv);
    }
    @Override
    public void initializeItems() {
        super.initializeItems();
        int itemSlot = getConfig().getInt("buttons.ITEM");
        setViewItem(item.self(), itemSlot);
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
        } else if (event.getSlot() == getConfig().getInt("buttons.PURCHASE")) {
            purchaseItem((Player) event.getWhoClicked());
        }

    }
    private void purchaseItem(Player player) {
        double price = item.getDouble("price");
        ItemStack itemStack = item.getItemStack("item");
        Economy eco = RushShop.getInstance().getEconomy();
        if (eco.getBalance(player) < price) {
            player.sendMessage(Misc.getMessage("auction_house.command.purchase.insufficient_balance")
                    .replace("{item_name}", itemStack.getItemMeta() != null ? itemStack.getItemMeta().getDisplayName() : itemStack.getType().name())
                    .replace("{price}", String.valueOf(price)));
            return;
        }
        int type = Config.i.getInt("auction_house.sale_tax.type", 0);
        double tax = Config.i.getInt("auction_house.sale_tax.value", 0);
        double priceToReceive = price;
        if (type == 0) {
            priceToReceive -= tax;
        } else if (type == 1) {
            priceToReceive -= price * (tax / 100);
        } else {
            LOGGER.log(Level.SEVERE, "Invalid sale tax type: {0}", type);
            return;
        }
        if (priceToReceive < 0) {
            priceToReceive = 0;
        }
        eco.withdrawPlayer(player, price);
        eco.depositPlayer(Bukkit.getOfflinePlayer(UUID.fromString(item.getString("owner"))), priceToReceive);
        for (ItemStack i : player.getInventory().addItem(itemStack).values()) {
            player.getWorld().dropItem(player.getLocation(), i);
        }
        RushShop.getInstance().getFileManager().getFile(FileID.AUCTION_DATA).getFileConfiguration().set(Objects.requireNonNull(item.self().getCurrentPath()), null);
        AuctionHouseOrdering.order();

        new AuctionHouseMainGUI(player);
    }
    private void setViewItem(ConfigurationSection section, int slot) {
        if (section == null) {
            return;
        }
        ItemStack baseItem = section.getItemStack("item", new ItemStack(Material.STONE));
        List<String> baseLore = Misc.colouredList(getConfig().getStringList("gui_items.display_item.lore"));
        List<String> newLore = baseItem.getItemMeta() != null && baseItem.getItemMeta().getLore() != null ? baseItem.getItemMeta().getLore() : new ArrayList<>();
        assert newLore != null;
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
        meta.setDisplayName(getConfig().getString("gui_items.display_item.name")
                .replace("{default_name}", baseItem.getItemMeta() != null ? baseItem.getItemMeta().getDisplayName() : baseItem.getType().name()));
        newItem.setItemMeta(meta);
        newItem.setAmount(baseItem.getAmount());
        inv.setItem(slot, newItem);
    }
}
