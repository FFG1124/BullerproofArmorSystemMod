package org.ffg1124.bullerproof_armor_system_mod.integration;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;
import org.ffg1124.bullerproof_armor_system_mod.command.WeaponTierManager;

@Mod.EventBusSubscriber(modid = Bullerproof_armor_system_mod.MODID)
public class VanillaProjectileHandler {

    private static final boolean DEBUG = true;

    @SubscribeEvent
    public static void onArrowSpawn(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof AbstractArrow arrow)) return;
        if (!(arrow.getOwner() instanceof LivingEntity shooter)) return;

        ItemStack weapon = shooter.getMainHandItem();

        // 弓的等级传递给箭矢
        if (weapon.getItem() instanceof BowItem || weapon.getItem() instanceof CrossbowItem) {
            String weaponId = net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(weapon.getItem()).toString();
            int weaponTier = WeaponTierManager.getWeaponTier(weaponId);

            if (weaponTier > 0) {
                arrow.getPersistentData().putInt("WeaponTier", weaponTier);
                if (DEBUG) {
                    Bullerproof_armor_system_mod.getLogger().info(
                            "箭矢获得武器等级加成: {}级", weaponTier
                    );
                }
            }
        }
    }
}