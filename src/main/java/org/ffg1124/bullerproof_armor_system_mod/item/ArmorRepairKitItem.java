package org.ffg1124.bullerproof_armor_system_mod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;
import org.ffg1124.bullerproof_armor_system_mod.durability.CustomDurabilityManager;

import javax.annotation.Nullable;
import java.util.List;

public class ArmorRepairKitItem extends Item {

    private final RepairKitTier tier;

    public enum RepairKitTier {
        BASIC("初级", 200, ChatFormatting.GRAY),
        MEDIUM("中级", 500, ChatFormatting.BLUE),
        ADVANCED("高级", 1000, ChatFormatting.GOLD);

        public final String name;
        public final int repairAmount;
        public final ChatFormatting color;

        RepairKitTier(String name, int repairAmount, ChatFormatting color) {
            this.name = name;
            this.repairAmount = repairAmount;
            this.color = color;
        }
    }

    public ArmorRepairKitItem(RepairKitTier tier) {
        super(new Item.Properties()
                .stacksTo(16)
        );
        this.tier = tier;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack kitStack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            // 获取玩家装备的所有护甲
            boolean repairedAny = false;
            int totalRepaired = 0;

            // 检查所有装备栏
            for (var slot : new net.minecraft.world.entity.EquipmentSlot[]{
                    net.minecraft.world.entity.EquipmentSlot.HEAD,
                    net.minecraft.world.entity.EquipmentSlot.CHEST,
                    net.minecraft.world.entity.EquipmentSlot.LEGS,
                    net.minecraft.world.entity.EquipmentSlot.FEET
            }) {
                ItemStack armor = player.getItemBySlot(slot);
                if (!armor.isEmpty() && armor.getItem() instanceof net.minecraft.world.item.ArmorItem) {
                    String itemId = ForgeRegistries.ITEMS.getKey(armor.getItem()).toString();
                    int armorTier = org.ffg1124.bullerproof_armor_system_mod.command.ArmorTierManager.getArmorTier(itemId);

                    if (armorTier > 0) {
                        int currentDurability = CustomDurabilityManager.getCurrentDurability(armor);
                        int maxDurability = CustomDurabilityManager.getMaxDurability(armor);

                        if (currentDurability < maxDurability) {
                            int newDurability = Math.min(maxDurability, currentDurability + tier.repairAmount);
                            int repaired = newDurability - currentDurability;

                            // 修复护甲
                            org.ffg1124.bullerproof_armor_system_mod.durability.CustomDurabilityManager.repairArmor(armor, repaired);
                            totalRepaired += repaired;
                            repairedAny = true;

                            Bullerproof_armor_system_mod.getLogger().info(
                                    "玩家 {} 使用{}维修包修复了 {}: +{} 耐久",
                                    player.getName().getString(), tier.name, armor.getDisplayName().getString(), repaired
                            );
                        }
                    }
                }
            }

            // 同时检查主手和副手（可能手持护甲）
            ItemStack mainHand = player.getMainHandItem();
            if (!mainHand.isEmpty() && mainHand.getItem() instanceof net.minecraft.world.item.ArmorItem) {
                String itemId = ForgeRegistries.ITEMS.getKey(mainHand.getItem()).toString();
                int armorTier = org.ffg1124.bullerproof_armor_system_mod.command.ArmorTierManager.getArmorTier(itemId);

                if (armorTier > 0) {
                    int currentDurability = CustomDurabilityManager.getCurrentDurability(mainHand);
                    int maxDurability = CustomDurabilityManager.getMaxDurability(mainHand);

                    if (currentDurability < maxDurability) {
                        int newDurability = Math.min(maxDurability, currentDurability + tier.repairAmount);
                        int repaired = newDurability - currentDurability;
                        CustomDurabilityManager.repairArmor(mainHand, repaired);
                        totalRepaired += repaired;
                        repairedAny = true;
                    }
                }
            }

            ItemStack offHand = player.getOffhandItem();
            if (!offHand.isEmpty() && offHand.getItem() instanceof net.minecraft.world.item.ArmorItem) {
                String itemId = ForgeRegistries.ITEMS.getKey(offHand.getItem()).toString();
                int armorTier = org.ffg1124.bullerproof_armor_system_mod.command.ArmorTierManager.getArmorTier(itemId);

                if (armorTier > 0) {
                    int currentDurability = CustomDurabilityManager.getCurrentDurability(offHand);
                    int maxDurability = CustomDurabilityManager.getMaxDurability(offHand);

                    if (currentDurability < maxDurability) {
                        int newDurability = Math.min(maxDurability, currentDurability + tier.repairAmount);
                        int repaired = newDurability - currentDurability;
                        CustomDurabilityManager.repairArmor(offHand, repaired);
                        totalRepaired += repaired;
                        repairedAny = true;
                    }
                }
            }

            if (repairedAny) {
                // 消耗一个维修包
                kitStack.shrink(1);
                player.sendSystemMessage(Component.literal("§a✓ 使用" + tier.name + "维修包，共修复了 " + totalRepaired + " 点耐久"));
                Bullerproof_armor_system_mod.getLogger().info(
                        "玩家 {} 使用{}维修包，共修复 {} 点耐久",
                        player.getName().getString(), tier.name, totalRepaired
                );
            } else {
                player.sendSystemMessage(Component.literal("§c✗ 没有需要修复的护甲"));
            }
        }

        return InteractionResultHolder.sidedSuccess(kitStack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("")
                .append(Component.literal("右键使用，修复穿戴的护甲"))
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("")
                .append(Component.literal("修复量: +" + tier.repairAmount + " 耐久"))
                .withStyle(tier.color));
        tooltip.add(Component.literal("")
                .append(Component.literal("可修复已配置等级的护甲"))
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}