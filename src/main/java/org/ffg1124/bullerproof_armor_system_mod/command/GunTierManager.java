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

public class GunTierManager {

    private static final Map<String, Integer> gunTierMap = new ConcurrentHashMap<>();
    private static final Map<String, Integer> dynamicTierMap = new ConcurrentHashMap<>(); // 动态等级（优先级最高）
    private static final Map<String, Boolean> lockedMap = new ConcurrentHashMap<>();

    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("bullerproof_armor_system/gun_tiers.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    static {
        loadFromFile();
    }

    private static class ConfigData {
        Map<String, Integer> gunTiers = new HashMap<>();
        Map<String, Boolean> locked = new HashMap<>();
    }

    public static void saveToFile() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            ConfigData data = new ConfigData();
            data.gunTiers = new HashMap<>(gunTierMap);
            data.locked = new HashMap<>(lockedMap);

            String json = GSON.toJson(data);
            Files.writeString(CONFIG_PATH, json);

            Bullerproof_armor_system_mod.getLogger().debug("Saved gun tier config");
        } catch (IOException e) {
            Bullerproof_armor_system_mod.getLogger().error("Failed to save gun tier config", e);
        }
    }

    public static boolean loadFromFile() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                Bullerproof_armor_system_mod.getLogger().info("Gun config file not found, using defaults");
                return false;
            }

            String json = Files.readString(CONFIG_PATH);
            ConfigData data = GSON.fromJson(json, ConfigData.class);

            if (data != null) {
                gunTierMap.clear();
                if (data.gunTiers != null) {
                    gunTierMap.putAll(data.gunTiers);
                }
                lockedMap.clear();
                if (data.locked != null) {
                    lockedMap.putAll(data.locked);
                }

                Bullerproof_armor_system_mod.getLogger().info("Loaded gun tier config");
                return true;
            }
        } catch (IOException e) {
            Bullerproof_armor_system_mod.getLogger().error("Failed to load gun tier config", e);
        }
        return false;
    }

    public static boolean reloadFromFile() {
        Bullerproof_armor_system_mod.getLogger().info("Reloading gun tier config...");
        boolean success = loadFromFile();
        if (success) {
            Bullerproof_armor_system_mod.getLogger().info("Reloaded successfully");
        } else {
            Bullerproof_armor_system_mod.getLogger().warn("Reload failed, keeping current config");
        }
        return success;
    }

    /**
     * 设置枪械等级（静态配置，持久化）
     */
    public static boolean setGunTier(String gunId, int tier) {
        if (gunId == null || gunId.isEmpty()) return false;
        if (tier < 1 || tier > 6) return false;
        if (isLocked(gunId)) return false;

        gunTierMap.put(gunId, tier);
        saveToFile();
        return true;
    }

    /**
     * 设置动态等级（不持久化，由弹药同步使用，优先级高于静态配置）
     */
    public static void setDynamicGunTier(String gunId, int tier) {
        if (gunId == null || gunId.isEmpty()) return;
        if (tier < 1 || tier > 6) {
            dynamicTierMap.remove(gunId);
            return;
        }
        dynamicTierMap.put(gunId, tier);
    }

    /**
     * 清除动态等级
     */
    public static void clearDynamicGunTier(String gunId) {
        dynamicTierMap.remove(gunId);
    }

    /**
     * 获取枪械等级（优先动态等级）
     */
    public static int getGunTier(String gunId) {
        if (gunId == null || gunId.isEmpty()) return 0;
        // 动态等级优先（由弹药同步设置）
        if (dynamicTierMap.containsKey(gunId)) {
            return dynamicTierMap.get(gunId);
        }
        return gunTierMap.getOrDefault(gunId, 0);
    }

    public static int getGunTier(Item item) {
        if (item == null) return 0;
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        if (key == null) return 0;
        return getGunTier(key.toString());
    }

    public static boolean removeGunTier(String gunId) {
        if (gunId == null || gunId.isEmpty()) return false;
        if (isLocked(gunId)) return false;

        boolean removed = gunTierMap.remove(gunId) != null;
        dynamicTierMap.remove(gunId);
        if (removed) saveToFile();
        return removed;
    }

    public static void lock(String gunId) {
        if (gunId == null || gunId.isEmpty()) return;
        lockedMap.put(gunId, true);
        saveToFile();
    }

    public static void unlock(String gunId) {
        if (gunId == null || gunId.isEmpty()) return;
        lockedMap.remove(gunId);
        saveToFile();
    }

    public static boolean isLocked(String gunId) {
        if (gunId == null || gunId.isEmpty()) return false;
        return lockedMap.getOrDefault(gunId, false);
    }

    public static Map<String, Integer> getAllConfigured() {
        Map<String, Integer> result = new HashMap<>(gunTierMap);
        result.putAll(dynamicTierMap);
        return result;
    }

    public static void clearAll() {
        gunTierMap.clear();
        dynamicTierMap.clear();
        saveToFile();
    }

    public static void resetAll() {
        gunTierMap.clear();
        dynamicTierMap.clear();
        lockedMap.clear();
        saveToFile();
    }

    public static Path getConfigPath() {
        return CONFIG_PATH;
    }
}