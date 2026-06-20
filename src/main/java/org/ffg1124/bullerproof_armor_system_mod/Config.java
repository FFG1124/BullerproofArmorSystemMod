package org.ffg1124.bullerproof_armor_system_mod;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = Bullerproof_armor_system_mod.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // ========== 甲弹对抗系统配置 ==========
    private static final ModConfigSpec.BooleanValue ENABLE_BALLISTIC_SYSTEM = BUILDER
            .comment("启用弹道护甲穿透系统")
            .define("enableBallisticSystem", true);

    private static final ModConfigSpec.BooleanValue ENABLE_TACZ_INTEGRATION = BUILDER
            .comment("启用TACZ（永恒枪械工坊）模组集成")
            .define("enableTaczIntegration", true);

    private static final ModConfigSpec.BooleanValue ENABLE_TIER_TOOLTIPS = BUILDER
            .comment("启用物品的等级提示信息")
            .define("enableTierTooltips", true);

    // ========== 护甲耐久配置 ==========
    private static final ModConfigSpec.IntValue ARMOR_TIER_1_DURABILITY = BUILDER
            .comment("1级护甲的最大耐久值")
            .defineInRange("armorTier1Durability", 200, 1, 10000);

    private static final ModConfigSpec.IntValue ARMOR_TIER_2_DURABILITY = BUILDER
            .comment("2级护甲的最大耐久值")
            .defineInRange("armorTier2Durability", 300, 1, 10000);

    private static final ModConfigSpec.IntValue ARMOR_TIER_3_DURABILITY = BUILDER
            .comment("3级护甲的最大耐久值")
            .defineInRange("armorTier3Durability", 400, 1, 10000);

    private static final ModConfigSpec.IntValue ARMOR_TIER_4_DURABILITY = BUILDER
            .comment("4级护甲的最大耐久值")
            .defineInRange("armorTier4Durability", 500, 1, 10000);

    private static final ModConfigSpec.IntValue ARMOR_TIER_5_DURABILITY = BUILDER
            .comment("5级护甲的最大耐久值")
            .defineInRange("armorTier5Durability", 700, 1, 10000);

    private static final ModConfigSpec.IntValue ARMOR_TIER_6_DURABILITY = BUILDER
            .comment("6级护甲的最大耐久值")
            .defineInRange("armorTier6Durability", 900, 1, 10000);

    private static final ModConfigSpec.DoubleValue DURABILITY_LOSS_MULTIPLIER = BUILDER
            .comment("耐久损耗倍率（1.0 = 1伤害 = 1耐久）")
            .defineInRange("durabilityLossMultiplier", 1.0, 0.0, 10.0);

    private static final ModConfigSpec.BooleanValue ENABLE_CUSTOM_DURABILITY = BUILDER
            .comment("启用自定义耐久系统")
            .define("enableCustomDurability", true);

    // ========== 维修包配置 ==========
    private static final ModConfigSpec.BooleanValue REPAIR_KIT_HOLD_TO_USE = BUILDER
            .comment("维修包使用方式: true=长按使用, false=点击使用")
            .define("repairKitHoldToUse", true);

    private static final ModConfigSpec.IntValue BASIC_REPAIR_TIME = BUILDER
            .comment("初级维修包长按时间（秒）")
            .defineInRange("basicRepairTime", 2, 1, 30);

    private static final ModConfigSpec.IntValue MEDIUM_REPAIR_TIME = BUILDER
            .comment("中级维修包长按时间（秒）")
            .defineInRange("mediumRepairTime", 3, 1, 30);

    private static final ModConfigSpec.IntValue ADVANCED_REPAIR_TIME = BUILDER
            .comment("高级维修包长按时间（秒）")
            .defineInRange("advancedRepairTime", 4, 1, 30);

    static final ModConfigSpec SPEC = BUILDER.build();

    // 默认值变量
    public static boolean enableBallisticSystem = true;
    public static boolean enableTaczIntegration = true;
    public static boolean enableTierTooltips = true;
    public static int[] armorTierDurability = new int[7];
    public static double durabilityLossMultiplier = 1.0;
    public static boolean enableCustomDurability = true;
    public static boolean repairKitHoldToUse = true;
    public static int basicRepairTime = 2;
    public static int mediumRepairTime = 3;
    public static int advancedRepairTime = 4;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        try {
            enableBallisticSystem = ENABLE_BALLISTIC_SYSTEM.get();
            enableTaczIntegration = ENABLE_TACZ_INTEGRATION.get();
            enableTierTooltips = ENABLE_TIER_TOOLTIPS.get();

            enableCustomDurability = ENABLE_CUSTOM_DURABILITY.get();
            durabilityLossMultiplier = DURABILITY_LOSS_MULTIPLIER.get();

            armorTierDurability[1] = ARMOR_TIER_1_DURABILITY.get();
            armorTierDurability[2] = ARMOR_TIER_2_DURABILITY.get();
            armorTierDurability[3] = ARMOR_TIER_3_DURABILITY.get();
            armorTierDurability[4] = ARMOR_TIER_4_DURABILITY.get();
            armorTierDurability[5] = ARMOR_TIER_5_DURABILITY.get();
            armorTierDurability[6] = ARMOR_TIER_6_DURABILITY.get();

            repairKitHoldToUse = REPAIR_KIT_HOLD_TO_USE.get();
            basicRepairTime = BASIC_REPAIR_TIME.get();
            mediumRepairTime = MEDIUM_REPAIR_TIME.get();
            advancedRepairTime = ADVANCED_REPAIR_TIME.get();

            Bullerproof_armor_system_mod.getLogger().info("护甲耐久配置加载完成:");
            for (int i = 1; i <= 6; i++) {
                Bullerproof_armor_system_mod.getLogger().info("  等级{}护甲: {}耐久", i, armorTierDurability[i]);
            }
            Bullerproof_armor_system_mod.getLogger().info("维修包使用方式: {}", repairKitHoldToUse ? "长按" : "点击");
            Bullerproof_armor_system_mod.getLogger().info("维修包时间配置: 初级={}秒, 中级={}秒, 高级={}秒",
                    basicRepairTime, mediumRepairTime, advancedRepairTime);

        } catch (Exception e) {
            Bullerproof_armor_system_mod.getLogger().error("加载配置文件失败: {}", e.getMessage());
        }
    }

    public static int getArmorDurabilityByTier(int armorTier) {
        if (armorTier < 1 || armorTier > 6) return 500;
        return armorTierDurability[armorTier];
    }

    public static float getDurabilityLossMultiplier() {
        return (float) durabilityLossMultiplier;
    }

    public static boolean isCustomDurabilityEnabled() {
        return enableCustomDurability;
    }
}