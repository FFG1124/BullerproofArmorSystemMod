package org.ffg1124.bullerproof_armor_system_mod.ballistic;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;
import org.ffg1124.bullerproof_armor_system_mod.command.ArmorTierManager;

@Mod.EventBusSubscriber(modid = Bullerproof_armor_system_mod.MODID)
public class BallisticAttackHandler {

    private static final boolean DEBUG = true;
    private static final float DURABILITY_DAMAGE_MULTIPLIER = 1.0f; // 耐久消耗倍数

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        float originalDamage = event.getAmount();

        if (DEBUG) {
            Bullerproof_armor_system_mod.getLogger().info(
                    "伤害事件触发: 实体={}, 原伤害={}",
                    entity.getName().getString(),
                    originalDamage
            );
        }

        // 确定击中部位
        String bodyPart = determineHitBodyPart(source, entity);
        EquipmentSlot targetSlot = getSlotForBodyPart(bodyPart);

        if (DEBUG) {
            Bullerproof_armor_system_mod.getLogger().info("击中部位: {}, 槽位: {}", bodyPart, targetSlot);
        }

        // 获取击中部位的护甲
        ItemStack armorPiece = getArmorForBodyPart(entity, bodyPart);
        int armorTier = getArmorTierFromItem(armorPiece);

        if (armorTier > 0) {
            float newDamage = calculateDamageWithArmor(originalDamage, armorTier);
            float reduction = 1.0f - (newDamage / originalDamage);

            if (DEBUG) {
                Bullerproof_armor_system_mod.getLogger().info(
                        "护甲减伤: 护甲等级={}, 减伤={}%, 原伤害={}, 新伤害={}",
                        armorTier, String.format("%.0f", reduction * 100), originalDamage, newDamage
                );
            }

            event.setAmount(newDamage);

            // 对击中部位的护甲扣除耐久
            damageArmorPiece(armorPiece, originalDamage, armorTier, entity);
        } else if (DEBUG) {
            Bullerproof_armor_system_mod.getLogger().info("该部位无护甲，全额伤害");
        }
    }

    /**
     * 对护甲扣除耐久
     */
    private static void damageArmorPiece(ItemStack armor, float damage, int armorTier, LivingEntity entity) {
        if (armor.isEmpty()) return;
        if (!armor.isDamageableItem()) return;

        // 计算耐久消耗：基础伤害 * 耐久倍数 / 护甲等级（高级护甲更耐打）
        int durabilityLoss = Math.max(1, (int)(damage * DURABILITY_DAMAGE_MULTIPLIER / Math.max(1, armorTier)));

        if (DEBUG) {
            Bullerproof_armor_system_mod.getLogger().info("扣除护甲耐久: {}, 当前耐久={}/{}, 扣除量={}",
                    armor.getDisplayName().getString(),
                    armor.getMaxDamage() - armor.getDamageValue(),
                    armor.getMaxDamage(),
                    durabilityLoss);
        }

        // 正确的 hurtAndBreak 调用方式 (Forge 1.20.1)
        armor.hurtAndBreak(durabilityLoss, entity, (e) -> {});
    }

    /**
     * 根据身体部位获取对应的装备槽位
     */
    private static EquipmentSlot getSlotForBodyPart(String bodyPart) {
        switch (bodyPart.toLowerCase()) {
            case "head":
                return EquipmentSlot.HEAD;
            case "chest":
                return EquipmentSlot.CHEST;
            case "legs":
                return EquipmentSlot.LEGS;
            case "feet":
                return EquipmentSlot.FEET;
            default:
                return EquipmentSlot.CHEST;
        }
    }

    private static String determineHitBodyPart(DamageSource source, LivingEntity entity) {
        if (source.getDirectEntity() != null) {
            double attackerY = source.getDirectEntity().getY();
            double entityY = entity.getY() + entity.getEyeHeight();
            double heightDifference = attackerY - entityY;

            if (heightDifference > 0.5) {
                return "head";
            } else if (heightDifference < -0.3) {
                return "legs";
            } else {
                return "chest";
            }
        }
        return "chest";
    }

    private static ItemStack getArmorForBodyPart(LivingEntity entity, String bodyPart) {
        switch (bodyPart.toLowerCase()) {
            case "head":
                return entity.getItemBySlot(EquipmentSlot.HEAD);
            case "chest":
                return entity.getItemBySlot(EquipmentSlot.CHEST);
            case "legs":
                return entity.getItemBySlot(EquipmentSlot.LEGS);
            case "feet":
                return entity.getItemBySlot(EquipmentSlot.FEET);
            default:
                return ItemStack.EMPTY;
        }
    }

    private static int getArmorTierFromItem(ItemStack stack) {
        if (stack.isEmpty()) return 0;

        var itemKey = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemKey == null) return 0;
        String itemId = itemKey.toString();

        return ArmorTierManager.getArmorTier(itemId);
    }

    private static float getArmorDamageReduction(int armorTier) {
        if (armorTier <= 0) return 0f;
        return Math.min(armorTier * 0.1f, 0.6f);
    }

    private static float calculateDamageWithArmor(float originalDamage, int armorTier) {
        if (armorTier <= 0) return originalDamage;
        float reduction = getArmorDamageReduction(armorTier);
        return originalDamage * (1.0f - reduction);
    }

    public static void debugEntityArmor(LivingEntity entity) {
        if (!DEBUG) return;

        Bullerproof_armor_system_mod.getLogger().info("===== 实体护甲调试 =====");
        Bullerproof_armor_system_mod.getLogger().info("实体: {}", entity.getName().getString());

        String[] slots = {"头部", "胸部", "腿部", "脚部"};
        EquipmentSlot[] equipmentSlots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

        for (int i = 0; i < slots.length; i++) {
            ItemStack armor = entity.getItemBySlot(equipmentSlots[i]);
            int tier = getArmorTierFromItem(armor);
            float reduction = tier * 0.1f;

            Bullerproof_armor_system_mod.getLogger().info(
                    "{}: {} (等级={}, 减伤={}%)",
                    slots[i],
                    armor.isEmpty() ? "无" : armor.getDisplayName().getString(),
                    tier,
                    String.format("%.0f", reduction * 100)
            );
        }

        Bullerproof_armor_system_mod.getLogger().info("=======================");
    }
}