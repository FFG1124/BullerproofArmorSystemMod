package org.ffg1124.bullerproof_armor_system_mod.ballistic;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;
import org.ffg1124.bullerproof_armor_system_mod.command.AmmoTierManager;
import org.ffg1124.bullerproof_armor_system_mod.command.ArmorTierManager;
import org.ffg1124.bullerproof_armor_system_mod.command.GunTierManager;

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

        // ========== 获取攻击等级（弹药等级优先，枪械等级次之） ==========
        int attackTier = getAttackTier(source);

        // 应用攻击加成
        float damageAfterBonus = originalDamage;
        if (attackTier > 0) {
            float attackBonus = 1.0f + (attackTier - 1) * 0.2f;
            damageAfterBonus = originalDamage * attackBonus;
            if (DEBUG) {
                Bullerproof_armor_system_mod.getLogger().info(
                        "攻击加成: 等级={}, 倍率={}, 伤害={} -> {}",
                        attackTier, attackBonus, originalDamage, damageAfterBonus
                );
            }
            event.setAmount(damageAfterBonus);
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
            return;
        }

        // 计算减伤率（考虑穿透）
        float reduction = getArmorDamageReduction(armorTier);
        float penetration = getPenetration(attackTier, armorTier);
        float actualReduction = reduction * (1.0f - penetration);
        float newDamage = damageAfterBonus * (1.0f - actualReduction);
        newDamage = Math.max(0, newDamage);

        if (DEBUG) {
            Bullerproof_armor_system_mod.getLogger().info(
                    "护甲减伤: 护甲等级={}, 攻击等级={}, 减伤率={}%, 穿透={}%, 实际减伤={}%, 新伤害={}",
                    armorTier, attackTier,
                    String.format("%.0f", reduction * 100),
                    String.format("%.0f", penetration * 100),
                    String.format("%.0f", actualReduction * 100),
                    String.format("%.1f", newDamage)
            );
        }

        event.setAmount(newDamage);

        // 扣除耐久（基于原始伤害）
        damageArmorPiece(armorPiece, originalDamage, armorTier, entity);
    }

    /**
     * 获取攻击等级（弹药等级优先，枪械等级次之）
     */
    private static int getAttackTier(DamageSource source) {
        // 1. 从弹射物获取弹药等级
        if (source.getDirectEntity() instanceof Projectile projectile) {
            var nbt = projectile.getPersistentData();
            if (nbt.contains("TaczAmmoId")) {
                String ammoId = nbt.getString("TaczAmmoId");
                int tier = AmmoTierManager.getAmmoTier(ammoId);
                if (tier > 0) return tier;
            }
            if (nbt.contains("WeaponTier")) {
                return nbt.getInt("WeaponTier");
            }
        }

        // 2. 从攻击者手中获取弹药/枪械等级
        if (source.getEntity() instanceof LivingEntity attacker) {
            ItemStack mainHand = attacker.getMainHandItem();
            if (!mainHand.isEmpty()) {
                String itemId = ForgeRegistries.ITEMS.getKey(mainHand.getItem()).toString();

                // 优先检查弹药等级
                int ammoTier = AmmoTierManager.getAmmoTier(itemId);
                if (ammoTier > 0) return ammoTier;

                // 检查枪械等级（GunId）
                if (itemId.startsWith("tacz:")) {
                    int gunTier = GunTierManager.getGunTier(itemId);
                    if (gunTier > 0) {
                        if (DEBUG) {
                            Bullerproof_armor_system_mod.getLogger().info("枪械等级: {} -> {}级", itemId, gunTier);
                        }
                        return gunTier;
                    }
                }
            }
        }

        return 0;
    }

    /**
     * 计算穿透因子
     */
    private static float getPenetration(int attackTier, int armorTier) {
        if (attackTier <= 0 || armorTier <= 0) return 0f;

        int diff = attackTier - armorTier;

        if (diff >= 3) return 0.8f;
        if (diff == 2) return 0.6f;
        if (diff == 1) return 0.4f;
        if (diff == 0) return 0.2f;
        if (diff == -1) return 0.1f;
        return 0f;
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