package org.ffg1124.bullerproof_armor_system_mod.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class PlatformHelper {

    public static String getItemId(ItemStack stack) {
        if (stack.isEmpty()) return "";
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return key == null ? "" : key.toString();
    }

    /**
     * 获取 ItemStack 的 CompoundTag（使用 Data Components）
     */
    public static CompoundTag getTag(ItemStack stack) {
        if (stack.isEmpty()) return null;
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return null;
        return data.copyTag();
    }

    /**
     * 获取或创建 ItemStack 的 CompoundTag
     */
    public static CompoundTag getOrCreateTag(ItemStack stack) {
        if (stack.isEmpty()) return new CompoundTag();
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return new CompoundTag();
        }
        return data.copyTag();
    }

    /**
     * 设置 ItemStack 的 NBT 数据
     */
    public static void setTag(ItemStack stack, CompoundTag tag) {
        if (stack.isEmpty()) return;
        if (tag == null || tag.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }

    /**
     * 检查 ItemStack 是否包含指定 NBT 键
     */
    public static boolean hasTagKey(ItemStack stack, String key) {
        CompoundTag tag = getTag(stack);
        return tag != null && tag.contains(key);
    }
}