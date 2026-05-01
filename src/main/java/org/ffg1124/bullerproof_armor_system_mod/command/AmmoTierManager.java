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

public class AmmoTierManager {

    private static final Map<String, Integer> ammoTierMap = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> lockedMap = new ConcurrentHashMap<>();
    private static final Map<String, Integer> builtInTiers = new HashMap<>();

    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("bullerproof_armor_system/ammo_tiers.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void init() {
    }

    private static class ConfigData {
        Map<String, Integer> ammoTiers = new HashMap<>();
        Map<String, Boolean> locked = new HashMap<>();
    }

    static {
        loadFromFile();
    }

    public static void saveToFile() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            ConfigData data = new ConfigData();
            data.ammoTiers = new HashMap<>(ammoTierMap);
            data.locked = new HashMap<>(lockedMap);

            String json = GSON.toJson(data);
            Files.writeString(CONFIG_PATH, json);

            Bullerproof_armor_system_mod.getLogger().debug("Saved ammo tier config");
        } catch (IOException e) {
            Bullerproof_armor_system_mod.getLogger().error("Failed to save ammo tier config", e);
        }
    }

    public static boolean loadFromFile() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                Bullerproof_armor_system_mod.getLogger().info("Config file not found, using defaults");
                return false;
            }

            String json = Files.readString(CONFIG_PATH);
            ConfigData data = GSON.fromJson(json, ConfigData.class);

            if (data != null) {
                ammoTierMap.clear();
                if (data.ammoTiers != null) {
                    ammoTierMap.putAll(data.ammoTiers);
                }
                lockedMap.clear();
                if (data.locked != null) {
                    lockedMap.putAll(data.locked);
                }

                Bullerproof_armor_system_mod.getLogger().info("Loaded ammo tier config");
                return true;
            }
        } catch (IOException e) {
            Bullerproof_armor_system_mod.getLogger().error("Failed to load ammo tier config", e);
        }
        return false;
    }

    public static boolean reloadFromFile() {
        Bullerproof_armor_system_mod.getLogger().info("Reloading ammo tier config...");
        boolean success = loadFromFile();
        if (success) {
            Bullerproof_armor_system_mod.getLogger().info("Reloaded successfully");
        } else {
            Bullerproof_armor_system_mod.getLogger().warn("Reload failed, keeping current config");
        }
        return success;
    }

    public static boolean setAmmoTier(String ammoId, int tier) {
        if (ammoId == null || ammoId.isEmpty()) {
            return false;
        }
        if (tier < 1 || tier > 6) {
            return false;
        }
        if (isLocked(ammoId)) {
            return false;
        }
        ammoTierMap.put(ammoId, tier);
        saveToFile();
        return true;
    }

    public static void setAmmoTierInternal(String armorId, int tier) {
        if (armorId == null || armorId.isEmpty()) return;
        if (tier < 1 || tier > 6) return;

        builtInTiers.put(armorId, tier);
        // 只有当用户没有设置过时才使用内置值
        if (!ammoTierMap.containsKey(armorId)) {
            ammoTierMap.put(armorId, tier);
        }
    }

    public static int getAmmoTier(String ammoId) {
        // 添加空值检查
        if (ammoId == null || ammoId.isEmpty()) {
            return 0;
        }
        return ammoTierMap.getOrDefault(ammoId, 0);
    }

    public static int getAmmoTier(Item item) {
        if (item == null) return 0;
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        if (key == null) return 0;
        return getAmmoTier(key.toString());
    }

    public static boolean removeAmmoTier(String ammoId) {
        if (ammoId == null || ammoId.isEmpty()) {
            return false;
        }
        if (isLocked(ammoId)) {
            return false;
        }
        boolean removed = ammoTierMap.remove(ammoId) != null;
        if (removed) saveToFile();
        return removed;
    }

    public static void lock(String ammoId) {
        if (ammoId == null || ammoId.isEmpty()) return;
        lockedMap.put(ammoId, true);
        saveToFile();
    }

    public static void unlock(String ammoId) {
        if (ammoId == null || ammoId.isEmpty()) return;
        lockedMap.remove(ammoId);
        saveToFile();
    }

    public static boolean isLocked(String ammoId) {
        if (ammoId == null || ammoId.isEmpty()) return false;
        return lockedMap.getOrDefault(ammoId, false);
    }

    public static Map<String, Integer> getAllConfigured() {
        return new HashMap<>(ammoTierMap);
    }

    public static Map<String, Boolean> getAllLocked() {
        return new HashMap<>(lockedMap);
    }

    public static void clearAll() {
        ammoTierMap.clear();
        saveToFile();
    }

    public static void resetAll() {
        ammoTierMap.clear();
        lockedMap.clear();
        saveToFile();
    }

    public static Path getConfigPath() {
        return CONFIG_PATH;
    }
}