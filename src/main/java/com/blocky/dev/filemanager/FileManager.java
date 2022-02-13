package com.blocky.dev.filemanager;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

@SuppressWarnings("unused")
public class FileManager {
    private final Map<String, ManagedFile> files;
    private final Plugin instance;
    public FileManager(Plugin instance) {
        this.instance = instance;
        files = new HashMap<>();
    }

    /**
     * Add an item to the file manager
     * @param id The ID of the file, you will access the file using this ID
     * @param file The file that will be registered
     */
    public ManagedFile addFile(FileID id, File file) {
        return addFile(id.getId(), file);
    }
    public ManagedFile addFile(String id, File file) {
        ManagedFile managedFile = new ManagedFile(file);
        files.put(id.toLowerCase(Locale.ROOT), managedFile);
        return getFile(id);
    }

    /**
     * Remove a file from the file manager
     * @param id The ID you used to register this file
     */
    public void removeFile(FileID id) {
        removeFile(id.getId());
    }
    public void removeFile(String id) {
        getFile(id).delete();
        files.remove(id.toLowerCase(Locale.ROOT));
    }

    /**
     * Get an existing item from the file manager, returns null if it does not exist
     * @param id The ID you used to register the file
     * @return The file, or null if it does not exist
     */
    public ManagedFile getFile(FileID id) {
        return getFile(id.getId());
    }
    public ManagedFile getFile(String id) {
        String n = id.toLowerCase(Locale.ROOT);
        if (!files.containsKey(n)) {
            return null;
        }
        return files.get(n);
    }

    /**
     * Used to get the file manager's map variable. Is not recommended
     */
    public Map<String, ManagedFile> getFilesMap() {
        return files;
    }

    /**
     * Get all the available IDs
     */
    public List<String> getFileIDs() {
        List<String> idList = new ArrayList<>();
        for (Map.Entry<String, ManagedFile> entry : files.entrySet()) {
            idList.add(entry.getKey());
        }
        return idList;
    }
    /**
     * Get all the available Files
     */
    public List<ManagedFile> getAllFiles() {
        List<ManagedFile> fileList = new ArrayList<>();
        for (Map.Entry<String, ManagedFile> entry : files.entrySet()) {
            fileList.add(entry.getValue());
        }
        return fileList;
    }

    /**
     * Save all the files to the hard drive
     */
    public void saveAll() {
        for (Map.Entry<String, ManagedFile> entry : files.entrySet()) {
            entry.getValue().save();
        }
    }

    /**
     * Creates a file, if the file already exists update the file saving the old configurations
     * @param pathInConfig The config path that contains the name of the file, generates the path if it does not exist
     * @param fileName The name of the file that is in the JAR itself
     * @return The updated/generated file
     */
    public File create(String pathInConfig, String fileName, boolean update) {
        String configuredFilePath = instance.getConfig().getString(pathInConfig);
        if (configuredFilePath == null) {
            instance.getConfig().set(pathInConfig, fileName);
            instance.saveConfig();
            configuredFilePath = instance.getConfig().getString(pathInConfig, "");
        }
        if (!configuredFilePath.endsWith(".yml")) {
            configuredFilePath += ".yml";
        }
        String filePath = configuredFilePath;

        File existingFile = new File(instance.getDataFolder(), filePath);

        // If the existing file does not exist, create a new file from the JAR and rename it to the value in config
        if (!existingFile.exists()) {
            instance.saveResource(fileName + ".yml", false);
            File defaultFile = new File(instance.getDataFolder(), fileName + ".yml");

            int i = 5;
            while (!defaultFile.renameTo(existingFile) && i > 0) {
                i--;
            }
            return existingFile;
        }
        if (!update) {
            return existingFile;
        }

        // Cache the existing file
        YamlConfiguration existingFileConfiguration = YamlConfiguration.loadConfiguration(existingFile);

        // Create the file from JAR
        instance.saveResource(fileName + ".yml", true);

        File fileFromJar = new File(instance.getDataFolder(), fileName + ".yml");
        YamlConfiguration fileFromJarConfiguration = YamlConfiguration.loadConfiguration(fileFromJar);

        // Restore the values from the old file
        for (Map.Entry<String, Object> existingEntry : existingFileConfiguration.getValues(true).entrySet()) {
            if (!fileFromJarConfiguration.contains(existingEntry.getKey())) {
                fileFromJarConfiguration.set(existingEntry.getKey(), existingEntry.getValue());
            }
        }
        try {
            fileFromJarConfiguration.save(fileFromJar);
        } catch (IOException e) { e.printStackTrace(); }
        int i = 5;
        while (!fileFromJar.renameTo(existingFile) && i > 0) {
            i--;
        }
        return existingFile;
    }
}