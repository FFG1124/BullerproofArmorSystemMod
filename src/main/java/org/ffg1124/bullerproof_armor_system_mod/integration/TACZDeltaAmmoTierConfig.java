package org.ffg1124.bullerproof_armor_system_mod.integration;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;
import org.ffg1124.bullerproof_armor_system_mod.command.AmmoTierManager;

@Mod.EventBusSubscriber(modid = Bullerproof_armor_system_mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TACZDeltaAmmoTierConfig {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Bullerproof_armor_system_mod.getLogger().info("正在为TaczDeltaAmmo模组弹药配置等级...");

            setAmmoTier("tacz_delta_ammo:9x19_2", 2);
            setAmmoTier("tacz_delta_ammo:9x19_3", 2);
            setAmmoTier("tacz_delta_ammo:9x19_4", 2);
            setAmmoTier("tacz_delta_ammo:9x19_5", 2);
        });
    }

    private static void setAmmoTier(String itemId, int tier) {
        AmmoTierManager.setAmmoTierInternal(itemId, tier);
        Bullerproof_armor_system_mod.getLogger().debug("配置弹药: {} -> {}级", itemId, tier);
    }
}