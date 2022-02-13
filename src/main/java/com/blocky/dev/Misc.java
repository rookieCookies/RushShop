package com.blocky.dev;

import com.blocky.dev.filemanager.FileID;
import com.blocky.dev.filemanager.ManagedFile;
import com.blocky.dev.rushshop.RushShop;
import org.apache.commons.lang.IllegalClassException;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.logging.Level;

@SuppressWarnings("ALL")
public class Misc {
    private static final Map<String, String> shortcutMessages = new HashMap<>();
    private static final ManagedFile messagesMFile = RushShop.getInstance().getFileManager().getFile(FileID.MESSAGES);

    Misc() {
        throw new IllegalClassException("Utility class");
    }

    public static String coloured(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
    public static List<String> colouredList(List<String> text) {
        List<String> list = new ArrayList<>();
        for (String i : text) {
            list.add(coloured(i));
        }
        return list;
    }

    public static ConfigurationSection getConfigurationSection(ConfigurationSection section, String path) {
        if (!section.contains(path) || !section.isConfigurationSection(path)) {
            return section.createSection(path);
        }
        return section.getConfigurationSection(path);
    }

    public static String getMessage(String path) {
        return getMessage(path, false);
    }
    public static String getMessage(String path, boolean noKeyCode) {
        ConfigurationSection configuration = messagesMFile.getFileConfiguration();
        if (!configuration.contains(path)) {
            RushShop.getInstance().getLogger().log(Level.SEVERE, "Missing message in the language file: {0}", path);
            configuration.set(path, path);
            messagesMFile.save();
            return path;
        }
        if (configuration.get(path) == null) {
            RushShop.getInstance().getLogger().log(Level.SEVERE, "Missing message in the language file: {0}", path);
            configuration.set(path, path);
            messagesMFile.save();
            return path;
        }
        String str = Objects.requireNonNull(configuration.get(path)).toString();
        // If the message does not contain any keys, just return the message
        if (!str.contains("%key") || noKeyCode) {
            return Misc.coloured(str);
        }
        return formatMessage(str);
    }
    public static List<String> getMessageList(String path) {
        ConfigurationSection configuration = messagesMFile.getFileConfiguration();
        if (!configuration.contains(path)) {
            RushShop.getInstance().getLogger().log(Level.SEVERE, "Missing list in the language file: {0}", path);
            configuration.set(path, path);
            messagesMFile.save();
            return new ArrayList<>();
        }
        if (configuration.get(path) == null) {
            RushShop.getInstance().getLogger().log(Level.SEVERE, "Missing list in the language file: {0}", path);
            configuration.set(path, new ArrayList<String>());
            messagesMFile.save();
            return new ArrayList<>();
        }
        List<String> str = configuration.getStringList(path);
        List<String> finalMessage = new ArrayList<>();
        for (String s : str) {
            finalMessage.add(formatMessage(s));
        }
        return finalMessage;
    }
    private static String formatMessage(String str) {
        ConfigurationSection configuration = messagesMFile.getFileConfiguration();
        List<String> split = new java.util.ArrayList<>(List.of(str.split("%")));
        StringBuilder message = new StringBuilder();
        // Iterate through all the values in the list
        for (String s : split) {
            // If the iterated value does not contain key skip it, since it can not be a keycode
            if (!s.contains("key ")) {
                message.append(s);
                continue;
            }
            String key = s.split(" ")[1];
            if (!(configuration.contains("keys." + key))) {
                configuration.set("keys." + key, key);
                messagesMFile.save();
                RushShop.getInstance().getLogger().log(Level.SEVERE, "Keycode not found: {0}", key);
                continue;
            }
            // Get the keycode
            if (shortcutMessages.containsKey(key)) {
                message.append(shortcutMessages.get(key));
                continue;
            }
            // Get the message linked to the keycode
            String shortcutMessage = getMessage("keys." + key);
            shortcutMessages.put(key, shortcutMessage);
            message.append(shortcutMessage);
        }
        return Misc.coloured(message.toString());
    }
}
