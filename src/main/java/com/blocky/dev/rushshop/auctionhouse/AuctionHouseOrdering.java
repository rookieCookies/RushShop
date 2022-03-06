package com.blocky.dev.rushshop.auctionhouse;

import com.blocky.dev.Configuration;
import com.blocky.dev.filemanager.FileID;
import com.blocky.dev.filemanager.ManagedFile;
import com.blocky.dev.rushshop.RushShop;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AuctionHouseOrdering {
    public static List<String> priceOrderLowToHigh = new ArrayList<>();
    public static List<String> priceOrderHighToLow = new ArrayList<>();
    public static List<String> baseOrder = new ArrayList<>();
    public static void order(){
        orderBase();
        orderByPrice();
    }
    public static void orderBase() {
        Configuration dataConfig = new Configuration(RushShop.getInstance().getFileManager().getFile(FileID.AUCTION_DATA).getFileConfiguration());
        baseOrder = new ArrayList<>(dataConfig.getKeys());
    }
    public static void orderByPrice() {
        List<String> keys = new ArrayList<>(baseOrder);
        keys.sort(new MoneyCompactor());
        priceOrderLowToHigh = keys;
        priceOrderHighToLow = reverseList(keys);
    }
    private static List<String> reverseList(List<String> list) {
        String[] array = list.toArray(new String[0]);
        for (int i = 0; i < array.length / 2; i++) {
            String tmp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = tmp;
        }
        return new ArrayList<>(List.of(array));
    }
}
class MoneyCompactor implements Comparator<String> {
    private static final ManagedFile dataMFile = RushShop.getInstance().getFileManager().getFile(FileID.AUCTION_DATA);
    @Override
    public int compare(String o1, String o2) {
        ConfigurationSection dataConfig = new Configuration(dataMFile.getFileConfiguration()).self();
        return (int) Math.round(dataConfig.getDouble(o1 + ".price") - dataConfig.getDouble(o2 + ".price"));
    }
}
