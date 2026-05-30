package org.ffg1124.bullerproof_armor_system_mod.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;
import org.ffg1124.bullerproof_armor_system_mod.durability.CustomDurabilityManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ArmorTierManager {

    private static final Map<String, Integer> armorTierMap = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> lockedMap = new ConcurrentHashMap<>();
    private static final Map<String, Integer> builtInTiers = new HashMap<>();

    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("bullerproof_armor_system/armor_tiers.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static class ConfigData {
        Map<String, Integer> armorTiers = new HashMap<>();
        Map<String, Boolean> locked = new HashMap<>();
    }

    static {
        loadFromFile();
    }

    public static void saveToFile() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());

            ConfigData data = new ConfigData();
            data.armorTiers = new HashMap<>(armorTierMap);
            data.locked = new HashMap<>(lockedMap);

            String json = GSON.toJson(data);
            Files.writeString(CONFIG_PATH, json);

            Bullerproof_armor_system_mod.getLogger().debug("Saved armor tier config");
        } catch (IOException e) {
            Bullerproof_armor_system_mod.getLogger().error("Failed to save armor tier config", e);
        }
    }

    public static boolean loadFromFile() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                Bullerproof_armor_system_mod.getLogger().info("Armor config file not found, using defaults");
                return false;
            }

            String json = Files.readString(CONFIG_PATH);
            ConfigData data = GSON.fromJson(json, ConfigData.class);

            if (data != null) {
                armorTierMap.clear();
                armorTierMap.putAll(data.armorTiers);
                lockedMap.clear();
                lockedMap.putAll(data.locked);

                Bullerproof_armor_system_mod.getLogger().info("Loaded armor tier config");
                return true;
            }
        } catch (IOException e) {
            Bullerproof_armor_system_mod.getLogger().error("Failed to load armor tier config", e);
        }
        return false;
    }

    public static boolean reloadFromFile() {
        Bullerproof_armor_system_mod.getLogger().info("Reloading armor tier config...");
        boolean success = loadFromFile();
        if (success) {
            Bullerproof_armor_system_mod.getLogger().info("Reloaded successfully");
        } else {
            Bullerproof_armor_system_mod.getLogger().warn("Reload failed, keeping current config");
        }
        return success;
    }

    public static boolean setArmorTier(String armorId, int tier) {
        if (tier < 1 || tier > 6) {
            return false;
        }
        if (isLocked(armorId)) {
            return false;
        }
        armorTierMap.put(armorId, tier);
        saveToFile();

        // ========== 新增：触发护甲耐久初始化 ==========
        triggerArmorDurabilityInit(armorId, tier);

        return true;
    }

    /**
     * 触发所有在线玩家手持或装备的护甲耐久初始化
     */
    private static void triggerArmorDurabilityInit(String armorId, int tier) {
        // 获取所有在线玩家
        var server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        for (var player : server.getPlayerList().getPlayers()) {
            // 检查主手
            ItemStack mainHand = player.getMainHandItem();
            if (!mainHand.isEmpty()) {
                String id = ForgeRegistries.ITEMS.getKey(mainHand.getItem()).toString();
                if (id.equals(armorId)) {
                    CustomDurabilityManager.initCustomDurability(mainHand, tier);
                    Bullerproof_armor_system_mod.getLogger().info(
                            "已初始化玩家 {} 手持护甲耐久: {}", player.getName().getString(), armorId
                    );
                }
            }

            // 检查所有装备栏
            for (var slot : new net.minecraft.world.entity.EquipmentSlot[]{
                    net.minecraft.world.entity.EquipmentSlot.HEAD,
                    net.minecraft.world.entity.EquipmentSlot.CHEST,
                    net.minecraft.world.entity.EquipmentSlot.LEGS,
                    net.minecraft.world.entity.EquipmentSlot.FEET
            }) {
                ItemStack armor = player.getItemBySlot(slot);
                if (!armor.isEmpty()) {
                    String id = ForgeRegistries.ITEMS.getKey(armor.getItem()).toString();
                    if (id.equals(armorId)) {
                        CustomDurabilityManager.initCustomDurability(armor, tier);
                        Bullerproof_armor_system_mod.getLogger().info(
                                "已初始化玩家 {} 装备护甲耐久: {}", player.getName().getString(), armorId
                        );
                    }
                }
            }

            // 检查背包
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (!item.isEmpty()) {
                    String id = ForgeRegistries.ITEMS.getKey(item.getItem()).toString();
                    if (id.equals(armorId)) {
                        CustomDurabilityManager.initCustomDurability(item, tier);
                    }
                }
            }
        }
    }

    public static void setArmorTierInternal(String armorId, int tier) {
        if (armorId == null || armorId.isEmpty()) return;
        if (tier < 1 || tier > 6) return;

        builtInTiers.put(armorId, tier);
        // 只有当用户没有设置过时才使用内置值
        if (!armorTierMap.containsKey(armorId)) {
            armorTierMap.put(armorId, tier);
        }
    }

    public static int getArmorTier(String armorId) {
        return armorTierMap.getOrDefault(armorId, 0);
    }

    public static int getArmorTier(Item item) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        if (key == null) return 0;
        return getArmorTier(key.toString());
    }

    public static boolean removeArmorTier(String armorId) {
        if (isLocked(armorId)) {
            return false;
        }
        boolean removed = armorTierMap.remove(armorId) != null;
        if (removed) saveToFile();
        return removed;
    }

    public static void lock(String armorId) {
        lockedMap.put(armorId, true);
        saveToFile();
    }

    public static void unlock(String armorId) {
        lockedMap.remove(armorId);
        saveToFile();
    }

    public static boolean isLocked(String armorId) {
        return lockedMap.getOrDefault(armorId, false);
    }

    public static Map<String, Integer> getAllConfigured() {
        return new HashMap<>(armorTierMap);
    }

    public static Map<String, Boolean> getAllLocked() {
        return new HashMap<>(lockedMap);
    }

    public static void clearAll() {
        armorTierMap.clear();
        saveToFile();
    }

    public static void resetAll() {
        armorTierMap.clear();
        lockedMap.clear();
        saveToFile();
    }

    public static Path getConfigPath() {
        return CONFIG_PATH;
    }
}