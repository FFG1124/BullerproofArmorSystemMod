package org.ffg1124.bullerproof_armor_system_mod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.ForgeRegistries;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;
import org.ffg1124.bullerproof_armor_system_mod.Config;
import org.ffg1124.bullerproof_armor_system_mod.durability.CustomDurabilityManager;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ArmorRepairKitItem extends Item {

    private final RepairKitTier tier;

    // 记录每个玩家开始长按的时间（客户端和服务端共享）
    private static final Map<UUID, Long> startTimeMap = new HashMap<>();
    // 记录每个玩家是否已完成长按（由客户端通知）
    private static final Map<UUID, Boolean> holdCompleteMap = new HashMap<>();

    public enum RepairKitTier {
        BASIC("初级", ChatFormatting.GRAY, 200, 4, 0.5f),   // 2秒
        MEDIUM("中级", ChatFormatting.BLUE, 500, 6, 0.6f),  // 3秒
        ADVANCED("高级", ChatFormatting.GOLD, 1000, 11, 0.7f); // 4秒

        public final String name;
        public final ChatFormatting color;
        public final int maxDurability;
        public final int requiredSeconds;
        public final int requiredTicks;
        public final float speedModifier;

        RepairKitTier(String name, ChatFormatting color, int maxDurability, int requiredSeconds, float speedModifier) {
            this.name = name;
            this.color = color;
            this.maxDurability = maxDurability;
            this.requiredSeconds = requiredSeconds;
            this.requiredTicks = requiredSeconds * 20;
            this.speedModifier = speedModifier;
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

    public static int getOriginalMaxDurability(ItemStack armor) {
        CompoundTag tag = armor.getTag();
        if (tag == null) {
            int max = CustomDurabilityManager.getMaxDurability(armor);
            armor.getOrCreateTag().putInt("BasOriginalMax", max);
            return max;
        }
        int original = tag.getInt("BasOriginalMax");
        if (original <= 0) {
            original = CustomDurabilityManager.getMaxDurability(armor);
            tag.putInt("BasOriginalMax", original);
        }
        return original;
    }

    public static int getArmorMaxReduction(ItemStack armor) {
        CompoundTag tag = armor.getTag();
        if (tag == null) return 0;
        return tag.getInt("BasMaxReduction");
    }

    public static int getArmorRepairCount(ItemStack armor) {
        CompoundTag tag = armor.getTag();
        if (tag == null) return 0;
        return tag.getInt("BasRepairCount");
    }

    public static int getCurrentMaxDurability(ItemStack armor) {
        int original = getOriginalMaxDurability(armor);
        int reduction = getArmorMaxReduction(armor);
        int result = original - reduction;
        return Math.max(1, result);
    }

    private void doRepair(Level level, Player player, ItemStack kitStack) {
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
            ItemStack armorStack = player.getItemBySlot(slot);  // 使用 armorStack 而不是 armor
            if (!armorStack.isEmpty() && armorStack.getItem() instanceof net.minecraft.world.item.ArmorItem) {
                String itemId = ForgeRegistries.ITEMS.getKey(armorStack.getItem()).toString();
                int armorTier = org.ffg1124.bullerproof_armor_system_mod.command.ArmorTierManager.getArmorTier(itemId);
                if (armorTier > 0) {
                    int current = CustomDurabilityManager.getCurrentDurability(armorStack);
                    int max = getCurrentMaxDurability(armorStack);
                    boolean isBroken = CustomDurabilityManager.isBroken(armorStack);

                    Bullerproof_armor_system_mod.getLogger().info("检查护甲: {} - 当前耐久: {}, 最大耐久: {}, 已损坏: {}",
                            armorStack.getDisplayName().getString(), current, max, isBroken);

                    // 允许修复已损坏的护甲
                    if (current < max) {
                        int need = max - current;
                        targets.add(new RepairTarget(armorStack, need));  // 使用 armorStack
                        totalNeeded += need;
                        Bullerproof_armor_system_mod.getLogger().info("需要修复: {} 点", need);
                    }
                }
            }
        }

        // 主手
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty() && mainHand.getItem() instanceof net.minecraft.world.item.ArmorItem && mainHand != kitStack) {
            String itemId = ForgeRegistries.ITEMS.getKey(mainHand.getItem()).toString();
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
            String itemId = ForgeRegistries.ITEMS.getKey(offHand.getItem()).toString();
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

        if (targets.isEmpty()) {
            player.sendSystemMessage(Component.literal("§c✗ 没有需要修复的护甲"));
            return;
        }

        int actualTotalRepair = Math.min(totalNeeded, availableDurability);

        if (actualTotalRepair <= 0) {
            player.sendSystemMessage(Component.literal("§c✗ 维修包已没有剩余耐久"));
            return;
        }

        applySlowEffect(player);

        int remainingRepair = actualTotalRepair;
        int totalRepaired = 0;

        for (RepairTarget target : targets) {
            if (remainingRepair <= 0) break;
            int toRepair = Math.min(target.needAmount, remainingRepair);
            int repaired = repairArmorWithDegradation(target.armor, toRepair);
            totalRepaired += repaired;
            remainingRepair -= toRepair;
        }

        int newDamage = kitStack.getDamageValue() + actualTotalRepair;
        if (newDamage >= kitStack.getMaxDamage()) {
            kitStack.setDamageValue(kitStack.getMaxDamage());
            player.sendSystemMessage(Component.literal("§c⚠ 维修包已耗尽耐久"));
        } else {
            kitStack.setDamageValue(newDamage);
        }

        int remaining = kitStack.getMaxDamage() - kitStack.getDamageValue();

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

        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
    }

    private int repairArmorWithDegradation(ItemStack armor, int repairAmount) {
        CompoundTag tag = armor.getOrCreateTag();

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

        // 更新耐久
        int newDurability = currentDurability + toRepair;
        tag.putInt(CustomDurabilityManager.NBT_CUSTOM_DURABILITY, newDurability);

        // 如果修复后耐久大于0，清除损坏标记
        if (newDurability > 0) {
            tag.putBoolean(CustomDurabilityManager.NBT_IS_BROKEN, false);
            Bullerproof_armor_system_mod.getLogger().info("护甲已修复，清除损坏标记");
        }

        return toRepair;
    }

    private void applySlowEffect(Player player) {
        int amplifier = Math.round((1.0f - tier.speedModifier) / 0.15f);
        amplifier = Math.max(0, Math.min(4, amplifier));

        MobEffectInstance slowEffect = new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                tier.requiredTicks,
                amplifier,
                false,
                true
        );
        player.addEffect(slowEffect);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack kitStack = player.getItemInHand(hand);

        if (kitStack.getDamageValue() >= kitStack.getMaxDamage()) {
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.literal("§c✗ 维修包已损坏，无法使用"));
            }
            return InteractionResultHolder.fail(kitStack);
        }

        if (!Config.repairKitHoldToUse) {
            // ==================== 点击模式 ====================
            if (level.isClientSide) {
                // 客户端：显示提示和动画
                player.displayClientMessage(Component.literal(tier.color + "🔧 开始修复护甲..."), true);
                player.swing(InteractionHand.MAIN_HAND);
            } else {
                // 服务端：应用缓慢效果
                Bullerproof_armor_system_mod.getLogger().info("点击模式 - 应用缓慢效果");
                applySlowEffect(player);

                // 延迟1秒后执行修复
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {}
                    player.getServer().execute(() -> {
                        doRepair(level, player, kitStack);
                        // 修复完成后移除缓慢效果
                        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                        Bullerproof_armor_system_mod.getLogger().info("点击模式 - 修复完成，缓慢效果已移除");
                    });
                }).start();
            }
            return InteractionResultHolder.sidedSuccess(kitStack, level.isClientSide);
        }

        // ==================== 长按模式 ====================
        if (level.isClientSide) {
            startTimeMap.put(player.getUUID(), System.currentTimeMillis());
            player.displayClientMessage(Component.literal(
                    tier.color + "🔧 长按 " + tier.requiredSeconds + " 秒修复护甲..."
            ), true);
            player.swing(InteractionHand.MAIN_HAND);
        }

        player.startUsingItem(hand);
        return InteractionResultHolder.sidedSuccess(kitStack, level.isClientSide);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (!Config.repairKitHoldToUse) return;
        if (!(entity instanceof Player player)) return;

        if (level.isClientSide) {
            // 客户端：计算实际长按时间
            Long startTime = startTimeMap.get(player.getUUID());
            if (startTime != null) {
                long elapsedMs = System.currentTimeMillis() - startTime;
                boolean completed = elapsedMs >= tier.requiredSeconds * 1000;

                Bullerproof_armor_system_mod.getLogger().info("客户端长按释放 - 需要: {} 秒, 实际: {} 毫秒, 完成: {}",
                        tier.requiredSeconds, elapsedMs, completed);

                if (completed) {
                    // 告诉服务端执行修复（通过发送数据包的方式）
                    // 简化：直接让服务端执行
                    holdCompleteMap.put(player.getUUID(), true);
                }
                startTimeMap.remove(player.getUUID());
            }
        }

        if (!level.isClientSide) {
            // 服务端：检查是否完成
            Boolean completed = holdCompleteMap.remove(player.getUUID());
            if (completed != null && completed) {
                Bullerproof_armor_system_mod.getLogger().info("服务端收到完成信号，执行修复");
                ItemStack kitStack = player.getMainHandItem();
                if (kitStack.isEmpty() || !(kitStack.getItem() instanceof ArmorRepairKitItem)) {
                    kitStack = player.getOffhandItem();
                }
                if (!kitStack.isEmpty() && kitStack.getItem() instanceof ArmorRepairKitItem) {
                    applySlowEffect(player);
                    doRepair(level, player, kitStack);
                }
            } else {
                Bullerproof_armor_system_mod.getLogger().info("长按未完成，取消修复");
                player.displayClientMessage(Component.literal("§c✗ 修复取消"), true);
            }
        }

        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        if (!Config.repairKitHoldToUse) return 0;
        return 72000; // 很大的值，让物品可以长时间使用
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
            Long startTime = startTimeMap.get(player.getUUID());
            if (startTime != null) {
                long elapsedMs = System.currentTimeMillis() - startTime;
                int percent = (int) (elapsedMs * 100 / (tier.requiredSeconds * 1000));
                percent = Math.min(100, percent);

                if (percent > 0 && percent % 10 == 0 && percent < 100) {
                    player.displayClientMessage(Component.literal(
                            tier.color + "🔧 修复进度: " + percent + "%"
                    ), true);
                }
            }
        }
    }

    @Override
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
                    .append(Component.literal("长按时间: " + tier.requiredSeconds + " 秒"))
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