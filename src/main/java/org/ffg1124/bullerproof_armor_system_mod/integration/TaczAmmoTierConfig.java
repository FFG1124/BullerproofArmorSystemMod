package org.ffg1124.bullerproof_armor_system_mod.integration;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;
import org.ffg1124.bullerproof_armor_system_mod.command.AmmoTierManager;

@Mod.EventBusSubscriber(modid = Bullerproof_armor_system_mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TaczAmmoTierConfig {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Bullerproof_armor_system_mod.getLogger().info("正在为Tacz模组弹药配置等级...");

            setAmmoTier("tacz:9mm", 1);
            setAmmoTier("tacz:12g", 1);
            setAmmoTier("tacz:30_06", 1);
            setAmmoTier("tacz:40mm", 1);
            setAmmoTier("tacz:45_70", 1);
            setAmmoTier("tacz:45acp", 1);
            setAmmoTier("tacz:46x30", 1);
            setAmmoTier("tacz:50ae", 1);
            setAmmoTier("tacz:50bmg", 1);
            setAmmoTier("tacz:57x28",1);
            setAmmoTier("tacz:58x42", 1);
            setAmmoTier("tacz:68x51fury", 1);
            setAmmoTier("tacz:308", 1);
            setAmmoTier("tacz:338", 1);
            setAmmoTier("tacz:357mag", 1);
            setAmmoTier("tacz:545x39", 1);
            setAmmoTier("tacz:556x45", 1);
            setAmmoTier("tacz:762x25", 1);
            setAmmoTier("tacz:762x39", 1);
            setAmmoTier("tacz:762x54", 1);
            setAmmoTier("tacz:rpg_rocket", 1);

            Bullerproof_armor_system_mod.getLogger().info("Tacz模组盔甲等级配置完成！");
        });
    }

    private static void setAmmoTier(String AmmoId, int tier) {
        AmmoTierManager.setAmmoTierInternal(AmmoId, tier);
        Bullerproof_armor_system_mod.getLogger().debug("配置弹药: {} -> {}级", AmmoId, tier);
    }
}
