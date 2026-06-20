package org.ffg1124.bullerproof_armor_system_mod.ballistic;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;
import org.ffg1124.bullerproof_armor_system_mod.command.ArmorTierManager;
import org.ffg1124.bullerproof_armor_system_mod.durability.CustomDurabilityManager;
import org.ffg1124.bullerproof_armor_system_mod.util.PlatformHelper;

@EventBusSubscriber(modid = Bullerproof_armor_system_mod.MODID)
public class BallisticAttackHandler {

    private static final boolean DEBUG = true;

    @SubscribeEvent
    public static void onLivingHurt(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();
        DamageSource source = event.getSource();
        float originalDamage = event.getOriginalDamage();

        if (DEBUG) {
            Bullerproof_armor_system_mod.getLogger().info(
                    "伤害事件: 实体={}, 伤害来源={}, 原伤害={}",
                    entity.getName().getString(),
                    source.getMsgId(),
                    originalDamage
            );
        }

        // 直接获取护甲（简化示例，仅处理胸部护甲以避免重复代码）
        ItemStack armorPiece = entity.getItemBySlot(EquipmentSlot.CHEST);
        int armorTier = getArmorTierFromItem(armorPiece);

        if (armorTier <= 0) {
            if (DEBUG) Bullerproof_armor_system_mod.getLogger().info("该部位无配置护甲，跳过");
            return;
        }

        if (CustomDurabilityManager.isBroken(armorPiece)) {
            if (DEBUG) Bullerproof_armor_system_mod.getLogger().info("护甲已损坏，不再提供减伤");
            return;
        }

        CustomDurabilityManager.initCustomDurability(armorPiece, armorTier);
        event.getOriginalDamage();  // 设置伤害为 0

        if (DEBUG) {
            Bullerproof_armor_system_mod.getLogger().info(
                    "护甲减伤: 等级={}, 减伤=100%, 原伤害={}, 新伤害=0",
                    armorTier, originalDamage
            );
        }

        CustomDurabilityManager.damageCustomDurability(armorPiece, originalDamage);
    }

    private static int getArmorTierFromItem(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        String itemId = PlatformHelper.getItemId(stack);
        return ArmorTierManager.getArmorTier(itemId);
    }
}