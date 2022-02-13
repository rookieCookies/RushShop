package com.blocky.dev.rushshop;

import com.blocky.dev.filemanager.FileID;
import com.blocky.dev.filemanager.FileManager;
import com.blocky.dev.rushshop.auctionhouse.AuctionHouse;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class RushShop extends JavaPlugin {
    private static RushShop instance;
    private Economy economy;
    private FileManager fileManager;

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();

        instance = this;
        setupEconomy();
        initializeFiles();
        new AuctionHouse();

        String booted = String.format("RushShop has been enabled in %sms", System.currentTimeMillis() - start);
        getLogger().info(booted);

    }

    // Set up vault API economy
    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        economy = rsp.getProvider();
    }

    private void initializeFiles() {
        saveDefaultConfig();
        fileManager = new FileManager(getInstance());
        fileManager.addFile(FileID.MESSAGES, fileManager.create("files.language_file", "messages", true));
        fileManager.addFile(FileID.AUCTION_HOUSE_GUI, fileManager.create("auction_house.menu_gui", "auction_house_gui", true));
        fileManager.addFile(FileID.AUCTION_DATA, fileManager.create("auction_house.data_file", "data_template", false));
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public Economy getEconomy() {
        return economy;
    }
    public static RushShop getInstance() {
        return instance;
    }
}
