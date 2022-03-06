package com.blocky.dev.rushshop.auctionhouse;

import com.blocky.dev.rushshop.RushShop;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;

@SuppressWarnings("unused")
public class AuctionHouse {
    private static final RushShop plugin = RushShop.getInstance();
    private static AuctionHouse instance;
    public AuctionHouse() {
        long start = System.currentTimeMillis();
        initialize(this);

        plugin.getLogger().info("Loading the AuctionHouse...");
        setupCommands();
        AuctionHouseOrdering.order();

        String completeMessage = String.format("Loaded the AuctionHouse in %sms", System.currentTimeMillis() - start);
        plugin.getLogger().info(completeMessage);
    }
    private static void initialize(AuctionHouse inst) {
        if (instance == null) {
            instance = inst;
        }
    }
    private static void setupCommands() {
        long start = System.currentTimeMillis();

        registerCommand("auctionhouse", new CommandAuctionHouse());

        String completeMessage = String.format(" - Registered all commands in %sms", System.currentTimeMillis() - start);
        plugin.getLogger().info(completeMessage);
    }

    private static void registerCommand(String name, CommandExecutor executor) {
        PluginCommand command = plugin.getCommand(name);
        if (command == null) {
            throw new NullPointerException("Command " + name + " is null, please contact the plugin owner!");
        }
        command.setExecutor(executor);
    }
    public static AuctionHouse getInstance() {
        return instance;
    }
}
