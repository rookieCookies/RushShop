package com.blocky.dev;


import com.blocky.dev.rushshop.RushShop;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class Config {
    private static final Plugin instance = RushShop.getInstance();

    Config() { throw new IllegalStateException("Utility class"); }

    public static long getLong(String path) {
        return getLong(path, 0);
    }
    public static long getLong(String path, long defaultValue) {
        if (instance.getConfig().contains(path)) {
            return instance.getConfig().getLong(path);
        }
        invalid(path);
        return defaultValue;
    }
    public static double getDouble(String path) {
        return getDouble(path, 0);
    }
    public static double getDouble(String path, double defaultValue) {
        if (instance.getConfig().contains(path)) {
            return instance.getConfig().getDouble(path);
        }
        invalid(path);
        return defaultValue;
    }
    public static int getInt(String path) {
        return getInteger(path);
    }
    public static int getInt(String path, int defaultValue) {
        return getInteger(path, defaultValue);
    }
    public static int getInteger(String path) {
        return getInteger(path, 0);
    }
    public static int getInteger(String path, int defaultValue) {
        if (instance.getConfig().contains(path)) {
            return instance.getConfig().getInt(path);
        }
        invalid(path);
        return defaultValue;
    }
    public static String getString(String path) {
        return getString(path, "Error");
    }
    public static String getString(String path, String defaultValue) {
        if (instance.getConfig().contains(path)) {
            return instance.getConfig().getString(path);
        }
        invalid(path);
        return defaultValue;
    }
    public static List<String> getStringList(String path) {
        return getStringList(path, new ArrayList<>());
    }
    public static List<String> getStringList(String path, List<String> defaultValue) {
        if (instance.getConfig().contains(path)) {
            return instance.getConfig().getStringList(path);
        }
        invalid(path);
        return defaultValue;
    }
    private static void invalid(String path) {
        String message = String.format("There is a missing value in config.yml (%s)", path.replace(".", " > "));
        instance.getLogger().warning(message);
    }
}

