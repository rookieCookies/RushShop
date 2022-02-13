package com.blocky.dev.rushshop.auctionhouse;

import com.blocky.dev.Configuration;
import com.blocky.dev.GUI;
import com.blocky.dev.Misc;
import com.blocky.dev.filemanager.FileID;
import com.blocky.dev.rushshop.RushShop;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

public class AuctionHousePurchase extends GUI {
    private static final Logger LOGGER = RushShop.getInstance().getLogger();
    private final Configuration item;

    public AuctionHousePurchase(HumanEntity entity, String ID) {
        super(FileID.AUCTION_HOUSE_GUI, "purchase");
        item = new Configuration(
                Misc.getConfigurationSection(
                        RushShop.getInstance().getFileManager().getFile(FileID.AUCTION_DATA).getFileConfiguration(), ID)
        );
        entity.openInventory(inv);
        registerSelf();
        initializeItems();
    }
    @Override
    public void initializeItems() {
        super.initializeItems();
        int itemSlot = getConfig().getInt("buttons.ITEM");
        ItemStack displayItem = createViewItem(item.self());
        inv.setItem(itemSlot, displayItem);
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
        } else if (event.getSlot() == getConfig().getInt("buttons.PURCHASE")) {
            purchaseItem((Player) event.getWhoClicked());
        }

    }
    private void purchaseItem(Player player) {
        double price = item.getDouble("price");
        ItemStack itemStack = item.getItemStack("item");
        Economy eco = RushShop.getInstance().getEconomy();
        if (eco.getBalance(player) < price) {
            player.sendMessage(Misc.getMessage("auction_house.command.purchase.insufficient_balance"));
            return;
        }
        player.sendMessage(Misc.getMessage("auction_house.command.purchase.insufficient_balance")
                .replace("{item_name}", itemStack.getItemMeta().getDisplayName())
                .replace("{price}", String.valueOf(price)));
        eco.withdrawPlayer(player, price);
        for (ItemStack i : player.getInventory().addItem(itemStack).values()) {
            player.getWorld().dropItem(player.getLocation(), i);
        }
        RushShop.getInstance().getFileManager().getFile(FileID.AUCTION_DATA).getFileConfiguration().set(Objects.requireNonNull(item.self().getCurrentPath()), null);

        new AuctionHouseMainGUI(player);
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
}
