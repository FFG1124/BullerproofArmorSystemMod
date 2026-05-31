package org.ffg1124.bullerproof_armor_system_mod.tooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;
import org.ffg1124.bullerproof_armor_system_mod.ballistic.BallisticUtils;
import org.ffg1124.bullerproof_armor_system_mod.command.AmmoTierManager;
import org.ffg1124.bullerproof_armor_system_mod.command.ArmorTierManager;
import org.ffg1124.bullerproof_armor_system_mod.command.WeaponTierManager;
import org.ffg1124.bullerproof_armor_system_mod.durability.CustomDurabilityManager;
import org.ffg1124.bullerproof_armor_system_mod.item.ArmorRepairKitItem;

import java.util.List;

@Mod.EventBusSubscriber(modid = Bullerproof_armor_system_mod.MODID)
public class ItemTierTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        List<Component> tooltip = event.getToolTip();
        ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemKey == null) return;
        String itemId = itemKey.toString();

        // ==================== 武器等级显示 ====================
        if (isWeapon(stack)) {
            int weaponTier = WeaponTierManager.getWeaponTier(itemId);

            if (weaponTier > 0) {
                float damageBonus = 1.0f + (weaponTier - 1) * 0.2f;
                float damageBonusPercent = (damageBonus - 1) * 100;

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("bullerproof_armor_system_mod.tooltip.weapon_tier", weaponTier)
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                tooltip.add(Component.translatable("bullerproof_armor_system_mod.tooltip.weapon_damage_bonus", String.format("%.0f", damageBonusPercent))
                        .withStyle(ChatFormatting.GOLD));
            }
            return;
        }

        // ==================== 弹药等级显示 ====================
        String ammoId = BallisticUtils.getAmmoIdFromNBT(stack);
        int ammoTier = 0;

        if (ammoId != null && !ammoId.isEmpty()) {
            ammoTier = AmmoTierManager.getAmmoTier(ammoId);
        }

        if (ammoTier == 0) {
            ammoTier = AmmoTierManager.getAmmoTier(itemId);
        }

        if (ammoTier > 0) {
            float damageBonus = 1.0f + (ammoTier - 1) * 0.2f;
            float damageBonusPercent = (damageBonus - 1) * 100;

            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("bullerproof_armor_system_mod.tooltip.ammo_tier", ammoTier)
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            tooltip.add(Component.translatable("bullerproof_armor_system_mod.tooltip.ammo_damage_bonus", String.format("%.0f", damageBonusPercent))
                    .withStyle(ChatFormatting.GOLD));
            return;
        }

        // ==================== 护甲等级显示 ====================
        if (stack.getItem() instanceof ArmorItem) {
            int armorTier = ArmorTierManager.getArmorTier(itemId);
            boolean isBroken = CustomDurabilityManager.isBroken(stack);
            int currentDurability = CustomDurabilityManager.getCurrentDurability(stack);
            int maxDurability = CustomDurabilityManager.getMaxDurability(stack);

            tooltip.add(Component.literal(""));

            // 护甲等级信息
            if (armorTier > 0) {
                tooltip.add(Component.translatable("bullerproof_armor_system_mod.tooltip.armor_tier", armorTier)
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            } else {
                tooltip.add(Component.translatable("bullerproof_armor_system_mod.tooltip.armor_tier_unconfigured")
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            }

            // ========== 自定义耐久显示 ==========
            tooltip.add(Component.literal(""));
            addCustomDurabilityInfo(tooltip, currentDurability, maxDurability, isBroken);

            // 损坏状态警告
            if (isBroken) {
                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("bullerproof_armor_system_mod.tooltip.armor_broken")
                        .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
            } else if (currentDurability <= maxDurability * 0.25) {
                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("bullerproof_armor_system_mod.tooltip.armor_low_durability")
                        .withStyle(ChatFormatting.GOLD));
            }

            int repairCount = ArmorRepairKitItem.getArmorRepairCount(stack);
            int reduction = ArmorRepairKitItem.getArmorMaxReduction(stack);
            int displayMax = ArmorRepairKitItem.getCurrentMaxDurability(stack);
            int originalMax = CustomDurabilityManager.getMaxDurability(stack);

            if (repairCount > 0) {
                tooltip.add(Component.literal("")
                        .append(Component.literal("修复次数: " + repairCount))
                        .withStyle(ChatFormatting.DARK_GRAY));
                tooltip.add(Component.literal("")
                        .append(Component.literal("最大耐久损耗: " + reduction))
                        .withStyle(ChatFormatting.DARK_GRAY));
                if (displayMax < originalMax) {
                    tooltip.add(Component.literal("")
                            .append(Component.literal("当前最大耐久: " + displayMax + " / " + originalMax))
                            .withStyle(ChatFormatting.RED));
                }
            }

            return;
        }
    }

    /**
     * 添加自定义耐久信息到工具提示
     */
    private static void addCustomDurabilityInfo(List<Component> tooltip, int current, int max, boolean isBroken) {
        if (max <= 0) {
            tooltip.add(Component.translatable("bullerproof_armor_system_mod.tooltip.durability_not_initialized")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        float percent = (float) current / max * 100;

        // 根据耐久百分比选择颜色
        ChatFormatting durabilityColor;
        if (isBroken) {
            durabilityColor = ChatFormatting.DARK_RED;
        } else if (percent > 75) {
            durabilityColor = ChatFormatting.GREEN;
        } else if (percent > 50) {
            durabilityColor = ChatFormatting.YELLOW;
        } else if (percent > 25) {
            durabilityColor = ChatFormatting.GOLD;
        } else {
            durabilityColor = ChatFormatting.RED;
        }

        // 耐久条（10格）
        String durabilityBar = getDurabilityBar(percent, isBroken);

        if (isBroken) {
            tooltip.add(Component.translatable("bullerproof_armor_system_mod.tooltip.durability")
                    .append(": ")
                    .append(Component.translatable("bullerproof_armor_system_mod.tooltip.durability_broken"))
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("  " + durabilityBar).withStyle(ChatFormatting.DARK_RED));
        } else {
            tooltip.add(Component.translatable("bullerproof_armor_system_mod.tooltip.durability")
                    .append(": ")
                    .append(Component.literal(current + " / " + max).withStyle(durabilityColor))
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("  " + durabilityBar).withStyle(durabilityColor));
            tooltip.add(Component.literal("  ")
                    .append(Component.literal(String.format("%.1f%%", percent)).withStyle(durabilityColor)));
        }
    }

    /**
     * 生成耐久条
     */
    private static String getDurabilityBar(float percent, boolean isBroken) {
        if (isBroken) {
            return "██████████"; // 全满但显示损坏状态
        }

        int filledBars = Math.round(percent / 10); // 10% 一格，共10格
        filledBars = Math.min(10, Math.max(0, filledBars));

        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            if (i < filledBars) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }
        return bar.toString();
    }

    private static boolean isWeapon(ItemStack stack) {
        return stack.getItem() instanceof SwordItem ||
                stack.getItem() instanceof AxeItem ||
                stack.getItem() instanceof BowItem ||
                stack.getItem() instanceof CrossbowItem ||
                stack.getItem() instanceof TridentItem;
    }
}