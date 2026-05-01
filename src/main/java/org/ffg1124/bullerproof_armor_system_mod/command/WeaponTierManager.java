package org.ffg1124.bullerproof_armor_system_mod.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WeaponTierManager {

    private static final Map<String, Integer> weaponTierMap = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> lockedMap = new ConcurrentHashMap<>();
    private static final Map<String, Integer> builtInTiers = new HashMap<>();

    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("bullerproof_armor_system/weapon_tiers.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static class ConfigData {
        Map<String, Integer> weaponTiers = new HashMap<>();
        Map<String, Boolean> locked = new HashMap<>();
    }

    static {
        loadFromFile();
    }

    public static void saveToFile() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            ConfigData data = new ConfigData();
            data.weaponTiers = new HashMap<>(weaponTierMap);
            data.locked = new HashMap<>(lockedMap);

            String json = GSON.toJson(data);
            Files.writeString(CONFIG_PATH, json);

            Bullerproof_armor_system_mod.getLogger().debug("Saved weapon tier config");
        } catch (IOException e) {
            Bullerproof_armor_system_mod.getLogger().error("Failed to save weapon tier config", e);
        }
    }

    public static boolean loadFromFile() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                Bullerproof_armor_system_mod.getLogger().info("Weapon config file not found, using defaults");
                return false;
            }

            String json = Files.readString(CONFIG_PATH);
            ConfigData data = GSON.fromJson(json, ConfigData.class);

            if (data != null) {
                weaponTierMap.clear();
                if (data.weaponTiers != null) {
                    weaponTierMap.putAll(data.weaponTiers);
                }
                lockedMap.clear();
                if (data.locked != null) {
                    lockedMap.putAll(data.locked);
                }

                Bullerproof_armor_system_mod.getLogger().info("Loaded weapon tier config");
                return true;
            }
        } catch (IOException e) {
            Bullerproof_armor_system_mod.getLogger().error("Failed to load weapon tier config", e);
        }
        return false;
    }

    public static boolean reloadFromFile() {
        Bullerproof_armor_system_mod.getLogger().info("Reloading weapon tier config...");
        boolean success = loadFromFile();
        if (success) {
            Bullerproof_armor_system_mod.getLogger().info("Reloaded successfully");
        } else {
            Bullerproof_armor_system_mod.getLogger().warn("Reload failed, keeping current config");
        }
        return success;
    }

    public static boolean setWeaponTier(String weaponId, int tier) {
        if (tier < 1 || tier > 6) {
            return false;
        }
        if (isLocked(weaponId)) {
            return false;
        }
        weaponTierMap.put(weaponId, tier);
        saveToFile();
        return true;
    }

    public static void setWeaponTierInternal(String weaponId, int tier) {
        if (weaponId == null || weaponId.isEmpty()) return;
        if (tier < 1 || tier > 6) return;

        builtInTiers.put(weaponId, tier);
        if (!weaponTierMap.containsKey(weaponId)) {
            weaponTierMap.put(weaponId, tier);
        }
    }

    public static int getWeaponTier(String weaponId) {
        return weaponTierMap.getOrDefault(weaponId, 0);
    }

    public static int getWeaponTier(Item item) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        if (key == null) return 0;
        return getWeaponTier(key.toString());
    }

    public static boolean removeWeaponTier(String weaponId) {
        if (isLocked(weaponId)) {
            return false;
        }
        boolean removed = weaponTierMap.remove(weaponId) != null;
        if (removed) saveToFile();
        return removed;
    }

    public static void lock(String weaponId) {
        lockedMap.put(weaponId, true);
        saveToFile();
    }

    public static void unlock(String weaponId) {
        lockedMap.remove(weaponId);
        saveToFile();
    }

    public static boolean isLocked(String weaponId) {
        return lockedMap.getOrDefault(weaponId, false);
    }

    public static Map<String, Integer> getAllConfigured() {
        return new HashMap<>(weaponTierMap);
    }

    public static Map<String, Boolean> getAllLocked() {
        return new HashMap<>(lockedMap);
    }

    public static void clearAll() {
        weaponTierMap.clear();
        saveToFile();
    }

    public static void resetAll() {
        weaponTierMap.clear();
        lockedMap.clear();
        saveToFile();
    }

    public static Path getConfigPath() {
        return CONFIG_PATH;
    }
}