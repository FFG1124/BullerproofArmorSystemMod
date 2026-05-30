package org.ffg1124.bullerproof_armor_system_mod.durability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;
import org.ffg1124.bullerproof_armor_system_mod.Config;

public class CustomDurabilityManager {

    public static final String NBT_CUSTOM_DURABILITY = "BasCustomDurability";
    public static final String NBT_MAX_CUSTOM_DURABILITY = "BasMaxCustomDurability";
    public static final String NBT_IS_BROKEN = "BasIsBroken";

    /**
     * 根据护甲等级获取最大耐久（从配置文件读取）
     */
    public static int getMaxDurabilityByTier(int armorTier) {
        return Config.getArmorDurabilityByTier(armorTier);
    }

    /**
     * 初始化护甲耐久
     */
    public static void initCustomDurability(ItemStack stack, int armorTier) {
        if (stack.isEmpty()) return;
        if (!Config.isCustomDurabilityEnabled()) return;

        CompoundTag tag = stack.getOrCreateTag();

        if (!tag.contains(NBT_MAX_CUSTOM_DURABILITY)) {
            int maxDurability = getMaxDurabilityByTier(armorTier);
            tag.putInt(NBT_MAX_CUSTOM_DURABILITY, maxDurability);
            tag.putInt(NBT_CUSTOM_DURABILITY, maxDurability);
            tag.putBoolean(NBT_IS_BROKEN, false);

            if (Bullerproof_armor_system_mod.getLogger().isDebugEnabled()) {
                Bullerproof_armor_system_mod.getLogger().info(
                        "初始化护甲耐久: {} -> {}/{}",
                        stack.getDisplayName().getString(), maxDurability, maxDurability
                );
            }
        }

        if (!tag.contains(NBT_CUSTOM_DURABILITY)) {
            int maxDurability = tag.getInt(NBT_MAX_CUSTOM_DURABILITY);
            tag.putInt(NBT_CUSTOM_DURABILITY, maxDurability);
            tag.putBoolean(NBT_IS_BROKEN, false);
        }
    }

    /**
     * 获取当前耐久值
     */
    public static int getCurrentDurability(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (!Config.isCustomDurabilityEnabled()) return stack.getMaxDamage() - stack.getDamageValue();

        CompoundTag tag = stack.getTag();
        if (tag == null) return 0;
        return tag.getInt(NBT_CUSTOM_DURABILITY);
    }

    /**
     * 获取最大耐久值
     */
    public static int getMaxDurability(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (!Config.isCustomDurabilityEnabled()) return stack.getMaxDamage();

        CompoundTag tag = stack.getTag();
        if (tag == null) return 0;
        return tag.getInt(NBT_MAX_CUSTOM_DURABILITY);
    }

    /**
     * 判断护甲是否损坏
     */
    public static boolean isBroken(ItemStack stack) {
        if (stack.isEmpty()) return true;
        if (!Config.isCustomDurabilityEnabled()) return stack.getDamageValue() >= stack.getMaxDamage();

        CompoundTag tag = stack.getTag();
        if (tag == null) return false;
        return tag.getBoolean(NBT_IS_BROKEN);
    }

    /**
     * 扣除耐久（伤害多少扣多少耐久 × 倍率）
     */
    public static boolean damageCustomDurability(ItemStack stack, float damage) {
        if (stack.isEmpty()) return false;
        if (!Config.isCustomDurabilityEnabled()) {
            // 如果未启用自定义耐久，使用原版耐久
            // 注意：这里只扣除耐久，不检查是否损坏（原版会自动处理）
            int durabilityLoss = Math.max(1, (int) Math.ceil(damage));
            stack.setDamageValue(stack.getDamageValue() + durabilityLoss);
            return stack.getDamageValue() >= stack.getMaxDamage();
        }

        CompoundTag tag = stack.getOrCreateTag();

        if (tag.getBoolean(NBT_IS_BROKEN)) {
            return true;
        }

        int currentDurability = tag.getInt(NBT_CUSTOM_DURABILITY);
        int maxDurability = tag.getInt(NBT_MAX_CUSTOM_DURABILITY);

        if (maxDurability <= 0) {
            return false;
        }

        // 伤害多少扣多少耐久 × 配置倍率
        float multiplier = Config.getDurabilityLossMultiplier();
        int durabilityLoss = Math.max(1, (int) Math.ceil(damage * multiplier));
        durabilityLoss = Math.min(durabilityLoss, maxDurability);

        int newDurability = currentDurability - durabilityLoss;

        if (newDurability <= 0) {
            newDurability = 0;
            tag.putBoolean(NBT_IS_BROKEN, true);
            if (Bullerproof_armor_system_mod.getLogger().isDebugEnabled()) {
                Bullerproof_armor_system_mod.getLogger().info("护甲已损坏: {}", stack.getDisplayName().getString());
            }
        }

        tag.putInt(NBT_CUSTOM_DURABILITY, newDurability);

        if (Bullerproof_armor_system_mod.getLogger().isDebugEnabled()) {
            Bullerproof_armor_system_mod.getLogger().info(
                    "耐久变化: {} -> {} (损耗: {}, 伤害: {}, 倍率: {})",
                    currentDurability, newDurability, durabilityLoss, damage, multiplier
            );
        }

        return newDurability <= 0;
    }

    /**
     * 修复护甲
     */
    public static void repairArmor(ItemStack stack, int amount) {
        if (stack.isEmpty()) return;
        if (!Config.isCustomDurabilityEnabled()) {
            // 原版修复逻辑
            int newDamage = stack.getDamageValue() - amount;
            if (newDamage < 0) newDamage = 0;
            stack.setDamageValue(newDamage);
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        int current = tag.getInt(NBT_CUSTOM_DURABILITY);
        int max = tag.getInt(NBT_MAX_CUSTOM_DURABILITY);

        if (max <= 0) return;

        int newDurability = Math.min(max, current + amount);
        tag.putInt(NBT_CUSTOM_DURABILITY, newDurability);

        if (newDurability > 0) {
            tag.putBoolean(NBT_IS_BROKEN, false);
        }
    }
}