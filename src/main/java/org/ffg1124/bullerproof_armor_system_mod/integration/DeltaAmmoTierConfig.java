package org.ffg1124.bullerproof_armor_system_mod.integration;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;
import org.ffg1124.bullerproof_armor_system_mod.command.AmmoTierManager;

@Mod.EventBusSubscriber(modid = Bullerproof_armor_system_mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DeltaAmmoTierConfig {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Bullerproof_armor_system_mod.getLogger().info("正在为Delta模组弹药配置等级...");

            // ==================== 9x19mm 手枪弹 ====================
            // 2级 (基础弹 - 对应TACZ 1级弹)
            setAmmoTier("delta:9x19_pst_gzh", 3);      // PST GZH
            setAmmoTier("delta:9x19_luger_cci", 2);    // Luger CCI

            // 3级 (中级弹)
            setAmmoTier("delta:9x19_ap_6.3", 4);       // AP 6.3

            // 4级 (高级弹)
            setAmmoTier("delta:9x19_7n31", 5);         // 7N31

            // ==================== .45 ACP 手枪弹 ====================
            // 2级 (基础弹)
            setAmmoTier("delta:45acp_fmj", 3);         // FMJ
            setAmmoTier("delta:45acp_hydrashok", 3);   // Hydra-Shok
            setAmmoTier("delta:45_rip", 2);            // RIP

            // 3级 (穿甲弹)
            setAmmoTier("delta:45acp_ap", 4);          // AP

            // ==================== 5.7x28mm 手枪弹 ====================
            // 2级 (基础弹)
            setAmmoTier("delta:57x28_ss197sr", 2);     // SS197SR
            setAmmoTier("delta:57x28_sb193", 3);       // SB193

            // 3级 (中级弹)
            setAmmoTier("delta:57x28_ss198lf", 3);     // SS198LF

            // 4级 (高级穿甲弹)
            setAmmoTier("delta:57x28_ss190", 4);       // SS190

            // ==================== 5.45x39mm 步枪弹 ====================
            // 2级 (基础弹)
            setAmmoTier("delta:545x39_ps", 3);         // PS
            setAmmoTier("delta:545x39_bt", 4);         // BT

            // 3级 (中级弹)
            setAmmoTier("delta:545x39_bs", 5);         // BS

            // 4级 (高级穿甲弹)
            setAmmoTier("delta:545x39_7n39", 6);       // 7N39

            // ==================== 5.56x45mm 步枪弹 ====================
            // 2级 (基础弹)
            setAmmoTier("delta:556x45_55fmj", 1);      // 55 FMJ
            setAmmoTier("delta:556x45_55hp", 2);       // 55 HP
            setAmmoTier("delta:556x45_m855", 3);       // M855
            setAmmoTier("delta:556x45_m856a1", 4);     // M856A1

            // 3级 (中级弹)
            setAmmoTier("delta:556x45_m855a1", 5);     // M855A1

            // 4级 (高级穿甲弹)
            setAmmoTier("delta:556x45_m995", 6);       // M995

            // ==================== 7.62x39mm 步枪弹 ====================
            // 2级 (基础弹)
            setAmmoTier("delta:762x39_ps", 4);         // PS
            setAmmoTier("delta:762x39_hp", 3);         // HP
            setAmmoTier("delta:762x39_us", 2);         // US
            setAmmoTier("delta:762x39_t45m", 1);       // T45M

            // 3级 (穿甲弹)
            setAmmoTier("delta:762x39_bp", 5);         // BP

            // ==================== 7.62x51mm (.308) 步枪弹 ====================
            // 2级 (基础弹)
            setAmmoTier("delta:308_bpz_fmj", 4);       // BPZ FMJ
            setAmmoTier("delta:308_m80", 4);           // M80
            setAmmoTier("delta:308_tpz_sp", 3);        // TPZ SP
            setAmmoTier("delta:308_ultra_nosler", 2);  // Ultra Nosler

            // 3级 (中级弹)
            setAmmoTier("delta:308_m62", 5);           // M62

            // 4级 (高级穿甲弹)
            setAmmoTier("delta:308_m61", 6);           // M61

            // ==================== 6.8x51mm 步枪弹 ====================
            // 2级 (基础弹)
            setAmmoTier("delta:68x51_sig_fmj", 4);     // SIG FMJ

            // 3级 (中级弹)
            setAmmoTier("delta:68x51_sig_hybrid", 5);  // SIG Hybrid

            // 4级 (穿甲弹)
            setAmmoTier("delta:68x51_ap", 6);          // AP

            // ==================== 12 gauge 霰弹 ====================
            // 2级 (基础弹)
            setAmmoTier("delta:12g_lead_slug", 3);      // Lead Slug
            setAmmoTier("delta:12g_flechette", 2);     // Flechette
            setAmmoTier("delta:12g_grizzly40", 4);     // Grizzly 40

            // 3级 (穿甲独头弹)
            setAmmoTier("delta:12g_ap20_slug", 5);     // AP-20 Slug

            // ==================== .50 AE 手枪弹 ====================
            // 2级 (基础弹)
            setAmmoTier("delta:50ae_fmj", 3);          // FMJ
            setAmmoTier("delta:50ae_jhp", 2);          // JHP
            setAmmoTier("delta:50ae_hawk_jsp", 4);     // Hawk JSP

            // 3级 (穿甲弹)
            setAmmoTier("delta:50ae_copper_solid", 4); // Copper Solid

            Bullerproof_armor_system_mod.getLogger().info("Delta模组弹药等级配置完成！");
        });
    }

    private static void setAmmoTier(String itemId, int tier) {
        AmmoTierManager.setAmmoTierInternal(itemId, tier);
        Bullerproof_armor_system_mod.getLogger().debug("配置弹药: {} -> {}级", itemId, tier);
    }
}