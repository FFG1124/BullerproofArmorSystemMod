package org.ffg1124.bullerproof_armor_system_mod.ballistic;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;
import org.ffg1124.bullerproof_armor_system_mod.command.AmmoTierManager;
import org.ffg1124.bullerproof_armor_system_mod.command.ArmorTierManager;
import org.ffg1124.bullerproof_armor_system_mod.command.GunTierManager;
import org.ffg1124.bullerproof_armor_system_mod.durability.CustomDurabilityManager;

@Mod.EventBusSubscriber(modid = Bullerproof_armor_system_mod.MODID)
public class BallisticAttackHandler {

    private static final boolean DEBUG = true;

    /**
     * 处理直接伤害（子弹、弓箭等）
     */
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

        int attackTier = getAttackTier(source);
        String bodyPart = determineHitBodyPart(source, entity);
        ItemStack armorPiece = getArmorForBodyPart(entity, bodyPart);
        int armorTier = getArmorTierFromItem(armorPiece);

        processArmorProtection(event, armorPiece, armorTier, attackTier, originalDamage);
    }

    /**
     * 处理效果伤害（中毒、燃烧、凋零等 - 喷火枪）
     */
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        float originalDamage = event.getAmount();

        if (!isEffectDamage(source)) {
            return;
        }

        if (DEBUG) {
            Bullerproof_armor_system_mod.getLogger().info(
                    "效果伤害事件: 实体={}, 伤害来源={}, 原伤害={}",
                    entity.getName().getString(),
                    source.getMsgId(),
                    originalDamage
            );
        }

        LivingEntity attacker = null;
        if (source.getEntity() instanceof LivingEntity living) {
            attacker = living;
        }

        if (attacker == null) return;

        ItemStack weapon = attacker.getMainHandItem();
        String itemId = ForgeRegistries.ITEMS.getKey(weapon.getItem()).toString();

        if (!itemId.equals("zombiekit:flamethrower")) {
            return;
        }

        int attackTier = GunTierManager.getGunTier(itemId);

        if (DEBUG && attackTier > 0) {
            Bullerproof_armor_system_mod.getLogger().info(
                    "[喷火枪] 效果伤害检测: 等级={}, 原伤害={}",
                    attackTier, originalDamage
            );
        }

        String bodyPart = "chest";
        ItemStack armorPiece = getArmorForBodyPart(entity, bodyPart);
        int armorTier = getArmorTierFromItem(armorPiece);

        processEffectDamageProtection(event, armorPiece, armorTier, attackTier, originalDamage);
    }

    /**
     * 判断是否是效果伤害
     */
    private static boolean isEffectDamage(DamageSource source) {
        String msgId = source.getMsgId();
        return msgId.equals("inFire") ||
                msgId.equals("onFire") ||
                msgId.equals("lava") ||
                msgId.equals("hotFloor") ||
                msgId.equals("magic") ||
                msgId.equals("wither") ||
                msgId.equals("drown") ||
                msgId.equals("cramming") ||
                msgId.equals("starve") ||
                msgId.equals("poison") ||
                msgId.equals("indirectMagic") ||
                msgId.equals("thorns") ||
                msgId.equals("fall") ||
                msgId.equals("flyIntoWall");
    }

    /**
     * 处理直接伤害的护甲减伤
     */
    private static void processArmorProtection(LivingHurtEvent event, ItemStack armorPiece,
                                               int armorTier, int attackTier, float originalDamage) {
        if (armorTier <= 0) {
            if (DEBUG) Bullerproof_armor_system_mod.getLogger().info("该部位无配置护甲，跳过");
            return;
        }

        if (CustomDurabilityManager.isBroken(armorPiece)) {
            if (DEBUG) Bullerproof_armor_system_mod.getLogger().info("护甲已损坏，不再提供减伤");
            return;
        }

        CustomDurabilityManager.initCustomDurability(armorPiece, armorTier);
        event.setAmount(0);

        if (DEBUG) {
            Bullerproof_armor_system_mod.getLogger().info(
                    "护甲减伤: 等级={}, 减伤=100%, 原伤害={}, 新伤害=0",
                    armorTier, originalDamage
            );
        }

        boolean isBrokenNow = CustomDurabilityManager.damageCustomDurability(armorPiece, originalDamage);
        if (DEBUG && isBrokenNow) {
            Bullerproof_armor_system_mod.getLogger().info("护甲已损坏: {}", armorPiece.getDisplayName().getString());
        }
    }

    /**
     * 处理效果伤害的护甲减伤（喷火枪专用）
     */
    private static void processEffectDamageProtection(LivingDamageEvent event, ItemStack armorPiece,
                                                      int armorTier, int attackTier, float originalDamage) {
        if (armorTier <= 0) {
            if (DEBUG) Bullerproof_armor_system_mod.getLogger().info("效果伤害: 该部位无配置护甲，跳过");
            return;
        }

        if (CustomDurabilityManager.isBroken(armorPiece)) {
            if (DEBUG) Bullerproof_armor_system_mod.getLogger().info("效果伤害: 护甲已损坏，不再提供减伤");
            return;
        }

        CustomDurabilityManager.initCustomDurability(armorPiece, armorTier);
        event.setAmount(0);

        if (DEBUG) {
            Bullerproof_armor_system_mod.getLogger().info(
                    "效果伤害减伤: 护甲等级={}, 攻击等级={}, 原伤害={}, 新伤害=0",
                    armorTier, attackTier, originalDamage
            );
        }

        float adjustedDamage = originalDamage * 0.5f;
        boolean isBrokenNow = CustomDurabilityManager.damageCustomDurability(armorPiece, originalDamage);
        if (DEBUG && isBrokenNow) {
            Bullerproof_armor_system_mod.getLogger().info("效果伤害导致护甲损坏: {}", armorPiece.getDisplayName().getString());
        }
    }

    /**
     * 获取攻击等级
     */
    private static int getAttackTier(DamageSource source) {
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

        if (source.getEntity() instanceof LivingEntity attacker) {
            ItemStack mainHand = attacker.getMainHandItem();
            if (!mainHand.isEmpty()) {
                String itemId = ForgeRegistries.ITEMS.getKey(mainHand.getItem()).toString();

                // 检查喷火枪
                if (itemId.equals("zombiekit:flamethrower")) {
                    int tier = GunTierManager.getGunTier(itemId);
                    if (tier > 0) return tier;
                }

                int ammoTier = AmmoTierManager.getAmmoTier(itemId);
                if (ammoTier > 0) return ammoTier;

                int weaponTier = GunTierManager.getGunTier(itemId);
                if (weaponTier > 0) return weaponTier;
            }
        }

        return 0;
    }

    // ==================== 辅助方法 ====================

    private static String determineHitBodyPart(DamageSource source, LivingEntity entity) {
        if (source.getDirectEntity() != null) {
            double attackerY = source.getDirectEntity().getY();
            double entityY = entity.getY() + entity.getEyeHeight();
            double heightDifference = attackerY - entityY;

            if (heightDifference > 0.5) return "head";
            else if (heightDifference < -0.3) return "legs";
            else return "chest";
        }
        return "chest";
    }

    private static ItemStack getArmorForBodyPart(LivingEntity entity, String bodyPart) {
        switch (bodyPart.toLowerCase()) {
            case "head": return entity.getItemBySlot(EquipmentSlot.HEAD);
            case "chest": return entity.getItemBySlot(EquipmentSlot.CHEST);
            case "legs": return entity.getItemBySlot(EquipmentSlot.LEGS);
            case "feet": return entity.getItemBySlot(EquipmentSlot.FEET);
            default: return ItemStack.EMPTY;
        }
    }

    private static int getArmorTierFromItem(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        var itemKey = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemKey == null) return 0;
        return ArmorTierManager.getArmorTier(itemKey.toString());
    }
}