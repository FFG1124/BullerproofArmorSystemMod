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
    private static final float DURABILITY_DAMAGE_MULTIPLIER = 1.0f; // 耐久损耗系数，可调

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        float originalDamage = event.getAmount();

        if (DEBUG) {
            Bullerproof_armor_system_mod.getLogger().info(
                    "伤害事件: 实体={}, 伤害来源={}, 原伤害={}",
                    entity.getName().getString(),
                    source.getMsgId(),
                    originalDamage
            );
        }

        // 确定击中部位
        String bodyPart = determineHitBodyPart(source, entity);
        ItemStack armorPiece = getArmorForBodyPart(entity, bodyPart);
        int armorTier = getArmorTierFromItem(armorPiece);

        // 无有效护甲等级，不处理
        if (armorTier <= 0) {
            if (DEBUG) Bullerproof_armor_system_mod.getLogger().info("该部位无配置护甲，跳过");
            return;
        }

        // 检查护甲是否已损坏（耐久 >= 最大耐久）
        if (isArmorBroken(armorPiece)) {
            if (DEBUG) Bullerproof_armor_system_mod.getLogger().info("护甲已损坏，不再提供减伤");
            return; // 损坏的护甲不提供减伤（也可改为极低减伤，见下方注释）
        }

        // 计算减伤率
        float reduction = getArmorDamageReduction(armorTier);
        float newDamage = originalDamage * (1.0f - reduction);
        newDamage = Math.max(0, newDamage);

        if (DEBUG) {
            Bullerproof_armor_system_mod.getLogger().info(
                    "护甲减伤: 等级={}, 减伤={}%, 原伤害={}, 新伤害={}",
                    armorTier, String.format("%.0f", reduction * 100), originalDamage, newDamage
            );
        }

        // 应用减伤后的伤害
        event.setAmount(newDamage);

        // 扣除耐久（基于原始伤害或减免的伤害量，可调整）
        damageArmorPiece(armorPiece, originalDamage, armorTier, entity);
    }

    /**
     * 判断护甲是否已损坏（耐久达到最大值）
     */
    private static boolean isArmorBroken(ItemStack armor) {
        if (armor.isEmpty()) return true;
        return armor.getDamageValue() >= armor.getMaxDamage();
    }

    /**
     * 扣除护甲耐久，耐久达到最大时不会让物品消失
     */
    private static void damageArmorPiece(ItemStack armor, float damage, int armorTier, LivingEntity entity) {
        if (armor.isEmpty()) return;
        if (!armor.isDamageableItem()) return;

        // 计算耐久消耗：基础伤害 * 倍数 / 护甲等级（高级护甲更耐用）
        int durabilityLoss = Math.max(1, (int) (damage * DURABILITY_DAMAGE_MULTIPLIER / Math.max(1, armorTier)));

        int currentDamage = armor.getDamageValue();
        int newDamage = currentDamage + durabilityLoss;

        // 限制最大耐久不超过物品最大耐久（不会损坏消失）
        if (newDamage >= armor.getMaxDamage()) {
            newDamage = armor.getMaxDamage();
            if (DEBUG && currentDamage < armor.getMaxDamage()) {
                Bullerproof_armor_system_mod.getLogger().info("护甲已损坏（耐久归零），但物品保留");
            }
        }

        armor.setDamageValue(newDamage);

        if (DEBUG) {
            Bullerproof_armor_system_mod.getLogger().info(
                    "扣除护甲耐久: {}, 原耐久={}/{}, 扣除量={}, 新耐久={}/{}",
                    armor.getDisplayName().getString(),
                    armor.getMaxDamage() - currentDamage, armor.getMaxDamage(),
                    durabilityLoss,
                    armor.getMaxDamage() - newDamage, armor.getMaxDamage()
            );
        }

        // 可选：耐久归零时触发额外效果（如播放破碎音效）
        if (newDamage >= armor.getMaxDamage()) {
            // 可以在这里添加音效或粒子效果
        }
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
        // 简单判定：根据攻击者高度差
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
}