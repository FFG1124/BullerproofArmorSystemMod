package org.ffg1124.bullerproof_armor_system_mod.ballistic;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;
import org.ffg1124.bullerproof_armor_system_mod.command.AmmoTierManager;
import org.ffg1124.bullerproof_armor_system_mod.command.ArmorTierManager;
import org.ffg1124.bullerproof_armor_system_mod.util.PlatformHelper;

import java.util.HashSet;
import java.util.Set;

public class BallisticUtils {

    private static final String NBT_AMMO_ID = "AmmoId";
    private static final Set<String> processedAmmoIds = new HashSet<>();

    public static String getAmmoIdFromNBT(ItemStack stack) {
        if (stack.isEmpty()) return null;
        CompoundTag tag = PlatformHelper.getTag(stack);
        if (tag != null && tag.contains(NBT_AMMO_ID)) {
            return tag.getString(NBT_AMMO_ID);
        }
        return null;
    }

    public static boolean hasAmmoIdNBT(ItemStack stack) {
        return getAmmoIdFromNBT(stack) != null;
    }

    private static boolean syncAmmoIdToConfig(String ammoId, ItemStack stack) {
        if (ammoId == null || ammoId.isEmpty()) return false;
        if (processedAmmoIds.contains(ammoId)) return false;
        if (AmmoTierManager.isLocked(ammoId)) return false;

        int existingTier = AmmoTierManager.getAmmoTier(ammoId);
        if (existingTier > 0) {
            processedAmmoIds.add(ammoId);
            return false;
        }

        int tier = 1;
        boolean success = AmmoTierManager.setAmmoTier(ammoId, tier);
        if (success) {
            processedAmmoIds.add(ammoId);
            Bullerproof_armor_system_mod.getLogger().info("从AmmoId同步弹药配置: {} -> {}级", ammoId, tier);
        }
        return success;
    }

    public static void scanEntityItems(LivingEntity entity) {
        if (entity == null) return;

        int syncedCount = 0;

        String ammoId = getAmmoIdFromNBT(entity.getMainHandItem());
        if (ammoId != null && syncAmmoIdToConfig(ammoId, entity.getMainHandItem())) syncedCount++;

        ammoId = getAmmoIdFromNBT(entity.getOffhandItem());
        if (ammoId != null && syncAmmoIdToConfig(ammoId, entity.getOffhandItem())) syncedCount++;

        if (syncedCount > 0) {
            Bullerproof_armor_system_mod.getLogger().info("同步了 {} 个AmmoId到配置", syncedCount);
        }
    }

    public static void clearAmmoIdCache() {
        processedAmmoIds.clear();
    }

    public static int getAmmoTierFromItem(ItemStack stack) {
        if (stack.isEmpty()) return 0;

        String ammoId = getAmmoIdFromNBT(stack);
        if (ammoId != null && !ammoId.isEmpty()) {
            syncAmmoIdToConfig(ammoId, stack);
            int tier = AmmoTierManager.getAmmoTier(ammoId);
            if (tier > 0) {
                return tier;
            }
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId == null) return 0;
        String id = itemId.toString();

        if (id.startsWith("tacz:")) {
            int tier = AmmoTierManager.getAmmoTier(id);
            if (tier > 0) return tier;
            return 1;
        }

        return AmmoTierManager.getAmmoTier(id);
    }

    public static int getArmorTierFromItem(ItemStack stack) {
        if (stack.isEmpty()) return 0;

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId == null) return 0;
        String id = itemId.toString();

        return ArmorTierManager.getArmorTier(id);
    }

    public static float getArmorDamageReduction(int armorTier) {
        if (armorTier <= 0) return 0f;
        return Math.min(armorTier * 0.1f, 0.6f);
    }

    public static float getSingleArmorProtection(int armorTier) {
        return getArmorDamageReduction(armorTier) * 100;
    }

    public static float getDamageReductionForSingleArmor(int armorTier, int ammoTier) {
        return getArmorDamageReduction(armorTier);
    }

    public static float calculateDamageWithArmor(float originalDamage, int armorTier) {
        if (armorTier <= 0) return originalDamage;
        float reduction = getArmorDamageReduction(armorTier);
        return originalDamage * (1.0f - reduction);
    }

    public static ItemStack getArmorForBodyPart(LivingEntity entity, String bodyPart) {
        return switch (bodyPart.toLowerCase()) {
            case "head" -> entity.getItemBySlot(EquipmentSlot.HEAD);
            case "chest" -> entity.getItemBySlot(EquipmentSlot.CHEST);
            case "legs" -> entity.getItemBySlot(EquipmentSlot.LEGS);
            case "feet" -> entity.getItemBySlot(EquipmentSlot.FEET);
            default -> ItemStack.EMPTY;
        };
    }
}