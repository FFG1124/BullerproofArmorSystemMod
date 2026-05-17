package org.ffg1124.bullerproof_armor_system_mod.integration;

import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;

@Mod.EventBusSubscriber(modid = Bullerproof_armor_system_mod.MODID)
public class TaczAmmoAttackHandler {

    // 此类已弃用，攻击加成已整合到 BallisticAttackHandler 中
    // 如果不需要可以删除此文件

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 不再处理，避免重复计算
        // 所有伤害计算由 BallisticAttackHandler 统一处理
    }
}