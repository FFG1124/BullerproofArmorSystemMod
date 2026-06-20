package org.ffg1124.bullerproof_armor_system_mod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;
import org.ffg1124.bullerproof_armor_system_mod.Config;
import org.ffg1124.bullerproof_armor_system_mod.durability.CustomDurabilityManager;
import org.ffg1124.bullerproof_armor_system_mod.util.PlatformHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// 移除静态导入，直接使用 PlatformHelper

public class ArmorRepairKitItem extends Item {

    private final RepairKitTier tier;

    // 长按任务存储（客户端计时用）
    private static final Map<UUID, Long> startTimeMap = new HashMap<>();

    public enum RepairKitTier {
        BASIC("初级", ChatFormatting.GRAY, 200, 0.5f),
        MEDIUM("中级", ChatFormatting.BLUE, 500, 0.6f),
        ADVANCED("高级", ChatFormatting.GOLD, 1000, 0.7f);

        public final String name;
        public final ChatFormatting color;
        public final int maxDurability;
        public final float speedModifier;

        RepairKitTier(String name, ChatFormatting color, int maxDurability, float speedModifier) {
            this.name = name;
            this.color = color;
            this.maxDurability = maxDurability;
            this.speedModifier = speedModifier;
        }

        public int getRequiredSeconds() {
            switch (this) {
                case BASIC: return Config.basicRepairTime;
                case MEDIUM: return Config.mediumRepairTime;
                case ADVANCED: return Config.advancedRepairTime;
                default: return 2;
            }
        }

        public int getRequiredTicks() {
            return getRequiredSeconds() * 20;
        }
    }

    private static class RepairTarget {
        final ItemStack armor;
        final int needAmount;

        RepairTarget(ItemStack armor, int needAmount) {
            this.armor = armor;
            this.needAmount = needAmount;
        }
    }

    public ArmorRepairKitItem(RepairKitTier tier) {
        super(new Item.Properties()
                .stacksTo(1)
                .durability(tier.maxDurability)
        );
        this.tier = tier;
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return true;
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return tier.maxDurability;
    }

    /**
     * 获取护甲原始最大耐久
     */
    public static int getOriginalMaxDurability(ItemStack armor) {
        CompoundTag tag = PlatformHelper.getTag(armor);
        if (tag == null || tag.isEmpty()) {
            int max = CustomDurabilityManager.getMaxDurability(armor);
            CompoundTag newTag = new CompoundTag();
            newTag.putInt("BasOriginalMax", max);
            PlatformHelper.setTag(armor, newTag);
            return max;
        }
        int original = tag.getInt("BasOriginalMax");
        if (original <= 0) {
            original = CustomDurabilityManager.getMaxDurability(armor);
            tag.putInt("BasOriginalMax", original);
            PlatformHelper.setTag(armor, tag);
        }
        return original;
    }

    /**
     * 获取护甲累计衰减值
     */
    public static int getArmorMaxReduction(ItemStack armor) {
        CompoundTag tag = PlatformHelper.getTag(armor);
        if (tag == null) return 0;
        return tag.getInt("BasMaxReduction");
    }

    /**
     * 获取护甲修复次数
     */
    public static int getArmorRepairCount(ItemStack armor) {
        CompoundTag tag = PlatformHelper.getTag(armor);
        if (tag == null) return 0;
        return tag.getInt("BasRepairCount");
    }

    /**
     * 获取护甲当前最大耐久（考虑衰减）
     */
    public static int getCurrentMaxDurability(ItemStack armor) {
        int original = getOriginalMaxDurability(armor);
        int reduction = getArmorMaxReduction(armor);
        int result = original - reduction;
        return Math.max(1, result);
    }

    /**
     * 核心修复逻辑
     */
    private void doRepair(Level level, Player player, ItemStack kitStack) {
        Bullerproof_armor_system_mod.getLogger().info("========================================");
        Bullerproof_armor_system_mod.getLogger().info("doRepair 被调用！");
        Bullerproof_armor_system_mod.getLogger().info("========================================");
        Bullerproof_armor_system_mod.getLogger().info("========== 开始执行护甲修复 ==========");
        Bullerproof_armor_system_mod.getLogger().info("玩家: {}", player.getName().getString());
        Bullerproof_armor_system_mod.getLogger().info("维修包等级: {}", tier.name);

        if (kitStack.getDamageValue() >= kitStack.getMaxDamage()) {
            Bullerproof_armor_system_mod.getLogger().info("修复失败: 维修包已损坏");
            player.sendSystemMessage(Component.literal("§c✗ 维修包已损坏，无法使用"));
            return;
        }

        int availableDurability = kitStack.getMaxDamage() - kitStack.getDamageValue();
        Bullerproof_armor_system_mod.getLogger().info("维修包剩余耐久: {}/{}", availableDurability, kitStack.getMaxDamage());

        List<RepairTarget> targets = new ArrayList<>();
        int totalNeeded = 0;

        // 收集需要修复的护甲 - 装备栏
        net.minecraft.world.entity.EquipmentSlot[] slots = {
                net.minecraft.world.entity.EquipmentSlot.HEAD,
                net.minecraft.world.entity.EquipmentSlot.CHEST,
                net.minecraft.world.entity.EquipmentSlot.LEGS,
                net.minecraft.world.entity.EquipmentSlot.FEET
        };

        for (net.minecraft.world.entity.EquipmentSlot slot : slots) {
            ItemStack armorStack = player.getItemBySlot(slot);
            if (!armorStack.isEmpty() && armorStack.getItem() instanceof net.minecraft.world.item.ArmorItem) {
                String itemId = BuiltInRegistries.ITEM.getKey(armorStack.getItem()).toString();
                int armorTier = org.ffg1124.bullerproof_armor_system_mod.command.ArmorTierManager.getArmorTier(itemId);
                if (armorTier > 0) {
                    int current = CustomDurabilityManager.getCurrentDurability(armorStack);
                    int max = getCurrentMaxDurability(armorStack);
                    boolean isBroken = CustomDurabilityManager.isBroken(armorStack);

                    Bullerproof_armor_system_mod.getLogger().info("检查护甲: {} - 当前耐久: {}, 最大耐久: {}, 已损坏: {}",
                            armorStack.getDisplayName().getString(), current, max, isBroken);

                    if (current < max) {
                        int need = max - current;
                        targets.add(new RepairTarget(armorStack, need));
                        totalNeeded += need;
                        Bullerproof_armor_system_mod.getLogger().info("需要修复: {} 点", need);
                    }
                }
            }
        }

        // 主手
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty() && mainHand.getItem() instanceof net.minecraft.world.item.ArmorItem && mainHand != kitStack) {
            String itemId = BuiltInRegistries.ITEM.getKey(mainHand.getItem()).toString();
            int armorTier = org.ffg1124.bullerproof_armor_system_mod.command.ArmorTierManager.getArmorTier(itemId);
            if (armorTier > 0) {
                int current = CustomDurabilityManager.getCurrentDurability(mainHand);
                int max = getCurrentMaxDurability(mainHand);
                if (current < max) {
                    int need = max - current;
                    targets.add(new RepairTarget(mainHand, need));
                    totalNeeded += need;
                }
            }
        }

        // 副手
        ItemStack offHand = player.getOffhandItem();
        if (!offHand.isEmpty() && offHand.getItem() instanceof net.minecraft.world.item.ArmorItem && offHand != kitStack) {
            String itemId = BuiltInRegistries.ITEM.getKey(offHand.getItem()).toString();
            int armorTier = org.ffg1124.bullerproof_armor_system_mod.command.ArmorTierManager.getArmorTier(itemId);
            if (armorTier > 0) {
                int current = CustomDurabilityManager.getCurrentDurability(offHand);
                int max = getCurrentMaxDurability(offHand);
                if (current < max) {
                    int need = max - current;
                    targets.add(new RepairTarget(offHand, need));
                    totalNeeded += need;
                }
            }
        }

        Bullerproof_armor_system_mod.getLogger().info("总计需要修复: {} 点耐久", totalNeeded);

        if (targets.isEmpty()) {
            Bullerproof_armor_system_mod.getLogger().info("修复失败: 没有需要修复的护甲");
            player.sendSystemMessage(Component.literal("§c✗ 没有需要修复的护甲"));
            return;
        }

        int actualTotalRepair = Math.min(totalNeeded, availableDurability);
        Bullerproof_armor_system_mod.getLogger().info("实际可修复: {} 点耐久", actualTotalRepair);

        if (actualTotalRepair <= 0) {
            Bullerproof_armor_system_mod.getLogger().info("修复失败: 维修包已没有剩余耐久");
            player.sendSystemMessage(Component.literal("§c✗ 维修包已没有剩余耐久"));
            return;
        }

        // 应用减速效果
        applySlowEffect(player);

        // 执行修复
        int remainingRepair = actualTotalRepair;
        int totalRepaired = 0;

        for (RepairTarget target : targets) {
            if (remainingRepair <= 0) break;
            int toRepair = Math.min(target.needAmount, remainingRepair);
            int repaired = repairArmorWithDegradation(target.armor, toRepair);
            totalRepaired += repaired;
            remainingRepair -= toRepair;
            Bullerproof_armor_system_mod.getLogger().info("修复护甲: +{} 耐久", toRepair);
        }

        // 扣除维修包耐久
        int newDamage = kitStack.getDamageValue() + actualTotalRepair;
        if (newDamage >= kitStack.getMaxDamage()) {
            kitStack.setDamageValue(kitStack.getMaxDamage());
            Bullerproof_armor_system_mod.getLogger().info("维修包已耗尽耐久");
            player.sendSystemMessage(Component.literal("§c⚠ 维修包已耗尽耐久"));
        } else {
            kitStack.setDamageValue(newDamage);
            Bullerproof_armor_system_mod.getLogger().info("维修包扣除耐久: {}", actualTotalRepair);
        }

        int remaining = kitStack.getMaxDamage() - kitStack.getDamageValue();

        Bullerproof_armor_system_mod.getLogger().info("========== 修复完成 ==========");
        Bullerproof_armor_system_mod.getLogger().info("总计修复: {} 点耐久", totalRepaired);
        Bullerproof_armor_system_mod.getLogger().info("维修包剩余耐久: {}/{}", remaining, kitStack.getMaxDamage());

        if (actualTotalRepair < totalNeeded) {
            player.sendSystemMessage(Component.literal(
                    tier.color + "✓ 维修包剩余耐久不足，已修复 " + totalRepaired + "/" + totalNeeded + " 点护甲耐久，" +
                            "维修包剩余耐久: " + remaining + "/" + kitStack.getMaxDamage()
            ));
        } else {
            player.sendSystemMessage(Component.literal(
                    tier.color + "✓ 修复完成！修复了 " + totalRepaired + " 点护甲耐久，" +
                            "维修包剩余耐久: " + remaining + "/" + kitStack.getMaxDamage()
            ));
        }

        // 修复完成后移除缓慢效果
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
    }

    /**
     * 修复单件护甲并减少最大耐久
     */
    private int repairArmorWithDegradation(ItemStack armor, int repairAmount) {
        CompoundTag tag = PlatformHelper.getOrCreateTag(armor);

        int originalMax = getOriginalMaxDurability(armor);
        int currentReduction = tag.getInt("BasMaxReduction");
        int currentMax = originalMax - currentReduction;
        int currentDurability = CustomDurabilityManager.getCurrentDurability(armor);

        int missing = currentMax - currentDurability;
        if (missing <= 0) return 0;

        int toRepair = Math.min(repairAmount, missing);

        int repairCount = tag.getInt("BasRepairCount");
        int durabilityLoss = Math.max(1, (int) (toRepair * 0.1f * (1 + repairCount * 0.05f)));

        int newReduction = currentReduction + durabilityLoss;
        tag.putInt("BasMaxReduction", newReduction);
        tag.putInt("BasRepairCount", repairCount + 1);

        int newDurability = currentDurability + toRepair;
        tag.putInt(CustomDurabilityManager.NBT_CUSTOM_DURABILITY, newDurability);

        if (newDurability > 0) {
            tag.putBoolean(CustomDurabilityManager.NBT_IS_BROKEN, false);
        }

        PlatformHelper.setTag(armor, tag);
        return toRepair;
    }

    /**
     * 应用减速效果
     */
    private void applySlowEffect(Player player) {
        int amplifier = Math.round((1.0f - tier.speedModifier) / 0.15f);
        amplifier = Math.max(0, Math.min(4, amplifier));

        MobEffectInstance slowEffect = new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                tier.getRequiredTicks(),
                amplifier,
                false,
                true
        );
        player.addEffect(slowEffect);
        Bullerproof_armor_system_mod.getLogger().info("应用缓慢效果，等级: {}, 持续时间: {} ticks", amplifier, tier.getRequiredTicks());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack kitStack = player.getItemInHand(hand);

        Bullerproof_armor_system_mod.getLogger().info("维修包使用 - 玩家: {}, 维修包等级: {}", player.getName().getString(), tier.name);

        if (kitStack.getDamageValue() >= kitStack.getMaxDamage()) {
            Bullerproof_armor_system_mod.getLogger().info("维修包已损坏，无法使用");
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.literal("§c✗ 维修包已损坏，无法使用"));
            }
            return InteractionResultHolder.fail(kitStack);
        }

        // 默认使用长按模式（Config.repairKitHoldToUse 默认为 true）
        if (!Config.repairKitHoldToUse) {
            // ==================== 点击模式 ====================
            if (level.isClientSide) {
                player.displayClientMessage(Component.literal(tier.color + "🔧 开始修复护甲..."), true);
                player.swing(InteractionHand.MAIN_HAND);
            } else {
                Bullerproof_armor_system_mod.getLogger().info("点击模式 - 应用缓慢效果并开始修复");
                applySlowEffect(player);
                doRepair(level, player, kitStack);
            }
            return InteractionResultHolder.sidedSuccess(kitStack, level.isClientSide);
        }

        // ==================== 长按模式（默认） ====================
        if (level.isClientSide) {
            startTimeMap.put(player.getUUID(), System.currentTimeMillis());
            Bullerproof_armor_system_mod.getLogger().info("长按模式 - 开始计时, 需要 {} 秒", tier.getRequiredSeconds());
            player.displayClientMessage(Component.literal(
                    tier.color + "🔧 长按 " + tier.getRequiredSeconds() + " 秒修复护甲..."
            ), true);
            player.swing(InteractionHand.MAIN_HAND);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.sidedSuccess(kitStack, level.isClientSide);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        Bullerproof_armor_system_mod.getLogger().info("========== releaseUsing 被调用 ==========");

        if (!Config.repairKitHoldToUse) {
            Bullerproof_armor_system_mod.getLogger().info("长按模式未启用，跳过");
            return;
        }
        if (!(entity instanceof Player player)) {
            Bullerproof_armor_system_mod.getLogger().info("entity 不是 Player");
            return;
        }

        Bullerproof_armor_system_mod.getLogger().info("玩家: {}", player.getName().getString());
        Bullerproof_armor_system_mod.getLogger().info("维修包等级: {}", tier.name);

        int useDuration = stack.getUseDuration(entity);
        int useTime = useDuration - timeCharged;
        long requiredTicks = tier.getRequiredTicks();

        Bullerproof_armor_system_mod.getLogger().info("useDuration: {}, timeCharged: {}, useTime: {}",
                useDuration, timeCharged, useTime);
        Bullerproof_armor_system_mod.getLogger().info("需要 ticks: {}, 需要秒: {}", requiredTicks, tier.getRequiredSeconds());

        if (!level.isClientSide) {
            if (useTime >= requiredTicks) {
                Bullerproof_armor_system_mod.getLogger().info("长按完成！准备执行修复");

                ItemStack kitStack = player.getMainHandItem();
                if (kitStack.isEmpty() || !(kitStack.getItem() instanceof ArmorRepairKitItem)) {
                    kitStack = player.getOffhandItem();
                }

                if (!kitStack.isEmpty() && kitStack.getItem() instanceof ArmorRepairKitItem) {
                    Bullerproof_armor_system_mod.getLogger().info("找到维修包，调用 doRepair");
                    applySlowEffect(player);
                    doRepair(level, player, kitStack);
                } else {
                    Bullerproof_armor_system_mod.getLogger().warn("找不到维修包物品！");
                }
            } else {
                int remaining = (int)(requiredTicks - useTime);
                Bullerproof_armor_system_mod.getLogger().info("长按时间不足，取消修复, 还需 {} ticks", remaining);
                player.displayClientMessage(Component.literal("§c✗ 修复取消（还需 " + (remaining / 20) + " 秒）"), true);
            }
        }

        startTimeMap.remove(player.getUUID());
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        if (!Config.repairKitHoldToUse) return 0;
        return Integer.MAX_VALUE;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        if (!Config.repairKitHoldToUse) return UseAnim.NONE;
        return UseAnim.BOW;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingTicks) {
        if (!Config.repairKitHoldToUse) return;
        if (!(entity instanceof Player player)) return;

        if (level.isClientSide) {
            int totalTicks = getUseDuration(stack, entity);
            int usedTicks = totalTicks - remainingTicks;
            int requiredTicks = tier.getRequiredTicks();
            int percent = usedTicks * 100 / requiredTicks;
            percent = Math.min(100, percent);

            if (percent > 0 && percent % 10 == 0 && percent < 100) {
                player.displayClientMessage(Component.literal(
                        tier.color + "🔧 修复进度: " + percent + "% (" + (usedTicks / 20) + "/" + (requiredTicks / 20) + "秒)"
                ), true);
            }
        }
    }

    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        int currentDurability = stack.getMaxDamage() - stack.getDamageValue();
        int maxDurability = stack.getMaxDamage();
        float percent = (float) currentDurability / maxDurability * 100;

        boolean holdToUse = Config.repairKitHoldToUse;

        if (holdToUse) {
            tooltip.add(Component.literal("")
                    .append(Component.literal("长按右键使用，修复穿戴的护甲"))
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("")
                    .append(Component.literal("长按时间: " + tier.getRequiredSeconds() + " 秒"))
                    .withStyle(ChatFormatting.AQUA));
        } else {
            tooltip.add(Component.literal("")
                    .append(Component.literal("右键点击使用，修复穿戴的护甲"))
                    .withStyle(ChatFormatting.GRAY));
        }

        tooltip.add(Component.literal("")
                .append(Component.literal("修复期间移动速度降低 " + (int)((1 - tier.speedModifier) * 100) + "%"))
                .withStyle(ChatFormatting.RED));
        tooltip.add(Component.literal("")
                .append(Component.literal("修复消耗: 1耐久 = 1护甲耐久"))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("")
                .append(Component.literal("⚠ 每次修复会减少护甲最大耐久"))
                .withStyle(ChatFormatting.DARK_RED));
        tooltip.add(Component.literal(""));

        ChatFormatting durabilityColor;
        if (currentDurability <= 0) {
            durabilityColor = ChatFormatting.DARK_RED;
        } else if (percent > 50) {
            durabilityColor = ChatFormatting.GREEN;
        } else if (percent > 25) {
            durabilityColor = ChatFormatting.YELLOW;
        } else {
            durabilityColor = ChatFormatting.RED;
        }

        tooltip.add(Component.literal("")
                .append(Component.literal("剩余维修耐久: "))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("")
                .append(Component.literal(currentDurability + " / " + maxDurability))
                .withStyle(durabilityColor));

        int filledBars = Math.round(percent / 10);
        StringBuilder bar = new StringBuilder("  ");
        for (int i = 0; i < 10; i++) {
            if (i < filledBars) {
                bar.append("█");
            } else {
                bar.append("░");
            }
        }
        tooltip.add(Component.literal(bar.toString()).withStyle(durabilityColor));

        if (currentDurability <= 0) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("⚠ 维修包已耗尽，无法使用")
                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * (float)(stack.getMaxDamage() - stack.getDamageValue()) / (float)stack.getMaxDamage());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float percent = (float)(stack.getMaxDamage() - stack.getDamageValue()) / (float)stack.getMaxDamage();
        if (percent > 0.5f) return 0x00FF00;
        if (percent > 0.25f) return 0xFFA500;
        return 0xFF0000;
    }
}