package org.ffg1124.bullerproof_armor_system_mod.integration;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;
import org.ffg1124.bullerproof_armor_system_mod.command.AmmoTierManager;
import org.ffg1124.bullerproof_armor_system_mod.command.GunTierManager;

/**
 * Pillager's Gun（掠夺者的枪）模组适配器
 *
 * 该模组让掠夺者使用枪械，支持 TACZ 枪械联动。
 * 需要适配：
 * 1. 掠夺者射击时获取枪械等级并加成伤害
 * 2. 检测掠夺者背包中的弹药等级
 */
@Mod.EventBusSubscriber(modid = Bullerproof_armor_system_mod.MODID)
public class PillagersGunIntegration {

    private static final boolean DEBUG = true;

    // Pillager's Gun 的 Mod ID
    private static final String PILLAGERS_GUN_MOD_ID = "pillagers_gun";

    // 掠夺者使用的枪械物品 ID 前缀
    private static final String[] GUN_PREFIXES = {
            "pillagers_gun:pistol",
            "pillagers_gun:assault_rifle",
            "pillagers_gun:shotgun",
            "pillagers_gun:sniper_rifle",
            "pillagers_gun:bazooka"
    };

    /**
     * 判断是否为 Pillager's Gun 的枪械
     */
    public static boolean isPillagersGun(ItemStack stack) {
        if (stack.isEmpty()) return false;
        String itemId = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();

        // 检查是否为 Pillager's Gun 的枪械
        if (itemId.startsWith(PILLAGERS_GUN_MOD_ID)) {
            return true;
        }

        // 检查已知枪械 ID
        for (String prefix : GUN_PREFIXES) {
            if (itemId.equals(prefix)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断是否为 Pillager's Gun 的弹射物（子弹）
     */
    public static boolean isPillagersGunProjectile(Projectile projectile) {
        // Pillager's Gun 的子弹实体通常有特定名称
        String entityName = projectile.getType().toString().toLowerCase();
        return entityName.contains("bullet") ||
                entityName.contains("pillager") ||
                entityName.contains("projectile");
    }

    /**
     * 获取枪械的等级 ID（用于配置）
     */
    public static String getGunTierId(ItemStack stack) {
        if (stack.isEmpty()) return null;
        return ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
    }

    /**
     * 处理掠夺者射击伤害
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        DamageSource source = event.getSource();

        // 检查是否为弹射物伤害
        if (!(source.getDirectEntity() instanceof Projectile projectile)) {
            return;
        }

        // 检查是否为 Pillager's Gun 的子弹
        if (!isPillagersGunProjectile(projectile)) {
            return;
        }

        // 获取射击者（应该是掠夺者）
        if (!(source.getEntity() instanceof LivingEntity shooter)) {
            return;
        }

        // 获取射击者手持的枪械
        ItemStack gunStack = shooter.getMainHandItem();

        // 如果主手不是枪械，检查副手
        if (!isPillagersGun(gunStack)) {
            gunStack = shooter.getOffhandItem();
            if (!isPillagersGun(gunStack)) {
                return;
            }
        }

        String gunId = getGunTierId(gunStack);
        if (gunId == null) return;

        // 获取枪械等级（优先使用动态等级，否则使用配置等级）
        int gunTier = GunTierManager.getGunTier(gunId);

        // 如果枪械没有配置等级，尝试根据枪械类型推断
        if (gunTier <= 0) {
            gunTier = getDefaultGunTier(gunId);
        }

        if (DEBUG && gunTier > 0) {
            Bullerproof_armor_system_mod.getLogger().info(
                    "[Pillagers Gun] 掠夺者使用 {} 等级 {} 造成伤害 {}",
                    gunId, gunTier, event.getAmount()
            );
        }

        if (gunTier > 0) {
            // 根据枪械等级增加伤害
            float damageBonus = 1.0f + (gunTier - 1) * 0.2f;
            float newDamage = event.getAmount() * damageBonus;

            if (DEBUG) {
                Bullerproof_armor_system_mod.getLogger().info(
                        "[Pillagers Gun] 伤害加成: +{}%, {} -> {}",
                        String.format("%.0f", (damageBonus - 1) * 100),
                        event.getAmount(), newDamage
                );
            }

            event.setAmount(newDamage);
        }
    }

    /**
     * 根据枪械类型获取默认等级
     */
    private static int getDefaultGunTier(String gunId) {
        if (gunId.contains("pistol")) return 2;
        if (gunId.contains("assault_rifle")) return 3;
        if (gunId.contains("shotgun")) return 3;
        if (gunId.contains("sniper")) return 4;
        if (gunId.contains("bazooka")) return 5;
        return 0;
    }
}