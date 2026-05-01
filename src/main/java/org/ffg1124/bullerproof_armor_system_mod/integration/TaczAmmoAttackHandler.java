package org.ffg1124.bullerproof_armor_system_mod.integration;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;
import org.ffg1124.bullerproof_armor_system_mod.ballistic.BallisticUtils;
import org.ffg1124.bullerproof_armor_system_mod.command.AmmoTierManager;

@Mod.EventBusSubscriber(modid = Bullerproof_armor_system_mod.MODID)
public class TaczAmmoAttackHandler {

    private static final boolean DEBUG = true;

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        DamageSource source = event.getSource();

        // 检查是否是弹射物伤害
        if (!(source.getDirectEntity() instanceof Projectile projectile)) {
            return;
        }

        // 尝试获取弹射物中的弹药信息
        ItemStack ammoStack = getAmmoFromProjectile(projectile);
        if (ammoStack.isEmpty()) {
            return;
        }

        // 获取弹药等级
        int ammoTier = BallisticUtils.getAmmoTierFromItem(ammoStack);

        if (DEBUG && ammoTier > 0) {
            Bullerproof_armor_system_mod.getLogger().info(
                    "TACZ弹药伤害: 弹药={}, 等级={}, 原伤害={}",
                    ammoStack.getDisplayName().getString(),
                    ammoTier,
                    event.getAmount()
            );
        }

        if (ammoTier > 0) {
            // 根据弹药等级增加伤害
            float damageBonus = 1.0f + (ammoTier - 1) * 0.2f;
            float newDamage = event.getAmount() * damageBonus;

            if (DEBUG) {
                Bullerproof_armor_system_mod.getLogger().info(
                        "弹药加成: +{}% 伤害, 原伤害={}, 新伤害={}",
                        String.format("%.0f", (damageBonus - 1) * 100),
                        event.getAmount(),
                        newDamage
                );
            }

            event.setAmount(newDamage);
        }
    }

    /**
     * 从弹射物中获取弹药ItemStack
     * 这里需要根据TACZ模组的实际API来获取，以下是示例代码
     */
    private static ItemStack getAmmoFromProjectile(Projectile projectile) {
        // TODO: 根据TACZ模组的实际API获取弹药
        // 如果没有TACZ API，可以通过NBT或Capability获取

        // 尝试从弹射物的NBT中获取弹药信息
        var nbt = projectile.getPersistentData();
        if (nbt.contains("TaczAmmoId")) {
            String ammoId = nbt.getString("TaczAmmoId");
            // 创建一个代表该弹药的ItemStack用于等级查询
            return net.minecraftforge.registries.ForgeRegistries.ITEMS
                    .getValue(new net.minecraft.resources.ResourceLocation(ammoId))
                    .getDefaultInstance();
        }

        return ItemStack.EMPTY;
    }
}