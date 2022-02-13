package com.blocky.dev.rushshop.auctionhouse;

import com.blocky.dev.Config;
import com.blocky.dev.Misc;
import com.blocky.dev.filemanager.FileID;
import com.blocky.dev.rushshop.RushShop;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public class CommandAuctionHouse implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Misc.getMessage("command.player_only"));
            return false;
        } else if (args.length == 0) {
            ((Player) sender).performCommand("ah menu");
            return false;
        }
        Player p = (Player) sender;
        switch (args[0]) {
            case "add" -> argAdd(p, args);
            case "menu" -> new AuctionHouseMainGUI(p);
            default -> sender.sendMessage(Misc.getMessage("command.unknown_argument"));
        }
        return true;
    }

    private void argAdd(Player player, String[] args) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getItemMeta() == null || item.getType() == Material.AIR) {
            player.sendMessage(Misc.getMessage("auction_house.command.add.no_item"));
            return;
        }
        if (!Add.canParsePrice(args, player)) {
            return;
        }
        double price = Add.parsePrice(args[1]);
        ConfigurationSection dataBase = RushShop.getInstance().getFileManager().getFile(FileID.AUCTION_DATA).getFileConfiguration();
        for (int i = 0; i < Integer.parseInt(args[1]); i++) {
            UUID auctionID = UUID.randomUUID();
            if (dataBase.getConfigurationSection(auctionID.toString()) != null) {
                argAdd(player, args);
                return;
            }
            ConfigurationSection auction = dataBase.createSection(auctionID.toString());
            auction.set("owner", player.getUniqueId().toString());
            auction.set("creation_time", LocalDateTime.now().toString());
            auction.set("price", price);
//            auction.set("bids", new ArrayList<String>());
            auction.set("item", item);
            player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
            player.sendMessage(Misc.getMessage("auction_house.command.add.success")
                    .replace("{price}", String.valueOf(price))
                    .replace("{item_name}", item.getItemMeta().getDisplayName().isEmpty() ? item.getType().name() : item.getItemMeta().getDisplayName()));
        }
        RushShop.getInstance().getFileManager().getFile(FileID.AUCTION_DATA).save();
    }
    private static class Add {
        public static double parsePrice(String priceString) {
            return Double.parseDouble(priceString.trim());
        }
        public static boolean canParsePrice(String[] args, Player p) {
            double price;
            if (args.length < 2) {
                p.sendMessage(Misc.getMessage("command.illegal_usage").replace("{usage}", "/ah add <price>"));
                return false;
            }
            try {
                price = Double.parseDouble(args[1].trim());
            } catch (NumberFormatException e) {
                p.sendMessage(Misc.getMessage("auction_house.command.add.illegal_price"));
                return false;
            }
            double maxPrice = Config.getDouble("auction_house.max_price");
            double minPrice = Config.getDouble("auction_house.min_price");
            if (maxPrice >= 0 && price > maxPrice) {
                p.sendMessage(Misc.getMessage("auction_house.command.add.price_too_high").replace("{max_price}", String.valueOf(maxPrice)));
                return false;
            } else if (minPrice >= 0 && price < minPrice) {
                p.sendMessage(Misc.getMessage("auction_house.command.add.price_too_low").replace("{min_price}", String.valueOf(minPrice)));
                return false;
            }
            return true;
        }
    }
}
