package org.ffg1124.bullerproof_armor_system_mod.integration;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;
import org.ffg1124.bullerproof_armor_system_mod.command.ArmorTierManager;

@Mod.EventBusSubscriber(modid = Bullerproof_armor_system_mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class TarkovArmorTierConfig {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Bullerproof_armor_system_mod.getLogger().info("正在为Tarkov模组盔甲配置等级...");

            // ==================== 胸甲等级配置 ====================
            // 1级胸甲
            setArmorTier("ywzj_tarkov:k1_3m", 1);
            setArmorTier("ywzj_tarkov:k1_paca", 1);

            // 2级胸甲
            setArmorTier("ywzj_tarkov:k2_press", 2);
            setArmorTier("ywzj_tarkov:k2_untar", 2);

            // 3级胸甲
            setArmorTier("ywzj_tarkov:k3_highcom", 3);
            setArmorTier("ywzj_tarkov:k3_6b13m", 3);
            setArmorTier("ywzj_tarkov:k3_hvk", 3);
            setArmorTier("ywzj_tarkov:k3_standard", 3);
            setArmorTier("ywzj_tarkov:k3_tgh", 3);

            // 4级胸甲
            setArmorTier("ywzj_tarkov:k4_511_hexgrid", 4);
            setArmorTier("ywzj_tarkov:k4_gzhel", 4);
            setArmorTier("ywzj_tarkov:k4_dt_avs", 4);
            setArmorTier("ywzj_tarkov:k4_hmp", 4);
            setArmorTier("ywzj_tarkov:k4_mk2", 4);

            // 5级胸甲
            setArmorTier("ywzj_tarkov:k5_zhuk_6a", 5);
            setArmorTier("ywzj_tarkov:k5_6b43", 5);
            setArmorTier("ywzj_tarkov:k5_fs_composite", 5);
            setArmorTier("ywzj_tarkov:k5_heavy_assault", 5);
            setArmorTier("ywzj_tarkov:k5_hvk2", 5);

            // 6级胸甲
            setArmorTier("ywzj_tarkov:k6_titan", 6);

            // ==================== 头盔等级配置 ====================
            // 1级头盔
            setArmorTier("ywzj_tarkov:h1_fast", 1);
            setArmorTier("ywzj_tarkov:h1_boonie", 1);

            // 2级头盔
            setArmorTier("ywzj_tarkov:h2_kopak", 2);
            setArmorTier("ywzj_tarkov:h2_motor", 2);

            // 3级头盔
            setArmorTier("ywzj_tarkov:h3_hpp_kiass", 3);
            setArmorTier("ywzj_tarkov:h3_untar", 3);
            setArmorTier("ywzj_tarkov:h3_das", 3);
            setArmorTier("ywzj_tarkov:h3_h07_tactical", 3);
            setArmorTier("ywzj_tarkov:h3_riot", 3);

            // 4级头盔
            setArmorTier("ywzj_tarkov:h4_zsh12m", 4);
            setArmorTier("ywzj_tarkov:h4_d6", 4);
            setArmorTier("ywzj_tarkov:h4_dich", 4);

            // 5级头盔
            setArmorTier("ywzj_tarkov:h5_maska", 5);
            setArmorTier("ywzj_tarkov:h5_dich_1", 5);
            setArmorTier("ywzj_tarkov:h5_h09", 5);

            // 6级头盔
            setArmorTier("ywzj_tarkov:h6_h70_elite", 6);

            // ==================== 安全箱等级配置 ====================
            setArmorTier("ywzj_tarkov:safety_box_tier_1", 1);
            setArmorTier("ywzj_tarkov:safety_box_tier_2", 2);
            setArmorTier("ywzj_tarkov:safety_box_tier_3", 3);
            setArmorTier("ywzj_tarkov:safety_box_tier_4", 4);

            Bullerproof_armor_system_mod.getLogger().info("Tarkov模组盔甲等级配置完成！");
        });
    }

    private static void setArmorTier(String itemId, int tier) {
        ArmorTierManager.setArmorTierInternal(itemId, tier);
        Bullerproof_armor_system_mod.getLogger().debug("配置盔甲: {} -> {}级", itemId, tier);
    }
}