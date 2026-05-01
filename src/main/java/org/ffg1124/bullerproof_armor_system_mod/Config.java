package org.ffg1124.bullerproof_armor_system_mod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Bullerproof_armor_system_mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("是否在通用设置中记录泥土方块")
            .define("logDirtBlock", true);

    private static final ForgeConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("一个神奇的数字")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    public static final ForgeConfigSpec.ConfigValue<String> MAGIC_NUMBER_INTRODUCTION = BUILDER
            .comment("神奇数字的介绍信息")
            .define("magicNumberIntroduction", "神奇的数字是... ");

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ITEM_STRINGS = BUILDER
            .comment("需要在通用设置中记录的物品列表")
            .defineListAllowEmpty("items", List.of("minecraft:iron_ingot"), Config::validateItemName);

    // ========== 甲弹对抗系统配置 ==========
    private static final ForgeConfigSpec.BooleanValue ENABLE_BALLISTIC_SYSTEM = BUILDER
            .comment("启用弹道护甲穿透系统")
            .define("enableBallisticSystem", true);

    private static final ForgeConfigSpec.BooleanValue ENABLE_TACZ_INTEGRATION = BUILDER
            .comment("启用TACZ（永恒枪械工坊）模组集成")
            .define("enableTaczIntegration", true);

    private static final ForgeConfigSpec.ConfigValue<String> TACZ_AMMO_MAPPING = BUILDER
            .comment("TACZ弹药ID到等级的映射 (格式: 弹药ID:等级,弹药ID2:等级2)")
            .define("taczAmmoMapping", "");

    private static final ForgeConfigSpec.BooleanValue ENABLE_TIER_TOOLTIPS = BUILDER
            .comment("启用物品的等级提示信息")
            .define("enableTierTooltips", true);

    private static final ForgeConfigSpec.BooleanValue ENABLE_TIER_HUD = BUILDER
            .comment("启用屏幕上的等级HUD显示")
            .define("enableTierHud", true);

    private static final ForgeConfigSpec.IntValue HUD_POSITION_X = BUILDER
            .comment("HUD水平位置 (0-100, 0=左边, 100=右边)")
            .defineInRange("hudPositionX", 90, 0, 100);

    private static final ForgeConfigSpec.IntValue HUD_POSITION_Y = BUILDER
            .comment("HUD垂直位置 (0-100, 0=顶部, 100=底部)")
            .defineInRange("hudPositionY", 5, 0, 100);

    private static final ForgeConfigSpec.BooleanValue SHOW_DETAILED_INFO = BUILDER
            .comment("显示详细的数值信息")
            .define("showDetailedInfo", true);

    private static final ForgeConfigSpec.DoubleValue TIER_1_DAMAGE_MULTIPLIER = BUILDER
            .comment("1级弹药的穿透倍率")
            .defineInRange("tier1DamageMultiplier", 1.0, 0.5, 3.0);
    private static final ForgeConfigSpec.DoubleValue TIER_2_DAMAGE_MULTIPLIER = BUILDER
            .comment("2级弹药的穿透倍率")
            .defineInRange("tier2DamageMultiplier", 1.2, 0.5, 3.0);
    private static final ForgeConfigSpec.DoubleValue TIER_3_DAMAGE_MULTIPLIER = BUILDER
            .comment("3级弹药的穿透倍率")
            .defineInRange("tier3DamageMultiplier", 1.5, 0.5, 3.0);
    private static final ForgeConfigSpec.DoubleValue TIER_4_DAMAGE_MULTIPLIER = BUILDER
            .comment("4级弹药的穿透倍率")
            .defineInRange("tier4DamageMultiplier", 1.8, 0.5, 3.0);
    private static final ForgeConfigSpec.DoubleValue TIER_5_DAMAGE_MULTIPLIER = BUILDER
            .comment("5级弹药的穿透倍率")
            .defineInRange("tier5DamageMultiplier", 2.2, 0.5, 3.0);
    private static final ForgeConfigSpec.DoubleValue TIER_6_DAMAGE_MULTIPLIER = BUILDER
            .comment("6级弹药的穿透倍率")
            .defineInRange("tier6DamageMultiplier", 2.5, 0.5, 3.0);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    // 默认值
    public static boolean logDirtBlock = true;
    public static int magicNumber = 42;
    public static String magicNumberIntroduction = "神奇的数字是... ";
    public static Set<Item> items = Set.of();

    public static boolean enableBallisticSystem = true;
    public static boolean enableTaczIntegration = true;
    public static Map<String, Integer> taczAmmoMapping = new HashMap<>();
    public static float[] tierDamageMultipliers = new float[7];

    public static boolean enableTierTooltips = true;
    public static boolean enableTierHud = true;
    public static int hudPositionX = 90;
    public static int hudPositionY = 5;
    public static boolean showDetailedInfo = true;

    private static boolean validateItemName(final Object obj) {
        return obj instanceof final String itemName && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(itemName));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        try {
            logDirtBlock = LOG_DIRT_BLOCK.get();
            magicNumber = MAGIC_NUMBER.get();
            magicNumberIntroduction = MAGIC_NUMBER_INTRODUCTION.get();

            enableTierTooltips = ENABLE_TIER_TOOLTIPS.get();
            enableTierHud = ENABLE_TIER_HUD.get();
            hudPositionX = HUD_POSITION_X.get();
            hudPositionY = HUD_POSITION_Y.get();
            showDetailedInfo = SHOW_DETAILED_INFO.get();

            List<? extends String> itemStrings = ITEM_STRINGS.get();
            if (itemStrings != null) {
                items = itemStrings.stream()
                        .map(itemName -> ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemName)))
                        .collect(Collectors.toSet());
            }

            enableBallisticSystem = ENABLE_BALLISTIC_SYSTEM.get();
            enableTaczIntegration = ENABLE_TACZ_INTEGRATION.get();

            tierDamageMultipliers[1] = TIER_1_DAMAGE_MULTIPLIER.get().floatValue();
            tierDamageMultipliers[2] = TIER_2_DAMAGE_MULTIPLIER.get().floatValue();
            tierDamageMultipliers[3] = TIER_3_DAMAGE_MULTIPLIER.get().floatValue();
            tierDamageMultipliers[4] = TIER_4_DAMAGE_MULTIPLIER.get().floatValue();
            tierDamageMultipliers[5] = TIER_5_DAMAGE_MULTIPLIER.get().floatValue();
            tierDamageMultipliers[6] = TIER_6_DAMAGE_MULTIPLIER.get().floatValue();

            Map<String, Integer> newMapping = new HashMap<>();
            String mappingStr = TACZ_AMMO_MAPPING.get();
            if (mappingStr != null && !mappingStr.isEmpty()) {
                for (String pair : mappingStr.split(",")) {
                    String[] parts = pair.split(":");
                    if (parts.length == 2) {
                        try {
                            newMapping.put(parts[0], Integer.parseInt(parts[1]));
                        } catch (NumberFormatException e) {
                            Bullerproof_armor_system_mod.getLogger().error("TACZ映射中无效的等级: {}", pair);
                        }
                    }
                }
            }
            taczAmmoMapping = newMapping;
        } catch (Exception e) {
            Bullerproof_armor_system_mod.getLogger().error("加载配置文件失败: {}", e.getMessage());
        }
    }

    public static class ArmorConfig {
        public static ForgeConfigSpec.BooleanValue ENABLE_ARMOR_NO_BREAK;
        public static ForgeConfigSpec.DoubleValue DURABILITY_LOSS_MULTIPLIER;
        public static ForgeConfigSpec.BooleanValue SHOW_BROKEN_ARMOR_TOOLTIP;

        public static void init(ForgeConfigSpec.Builder builder) {
            builder.push("armor");

            ENABLE_ARMOR_NO_BREAK = builder
                    .comment("启用盔甲耐久耗尽后不消失功能")
                    .define("enableArmorNoBreak", true);

            DURABILITY_LOSS_MULTIPLIER = builder
                    .comment("盔甲耐久损耗系数 (0.0-1.0)")
                    .defineInRange("durabilityLossMultiplier", 0.1, 0.0, 1.0);

            SHOW_BROKEN_ARMOR_TOOLTIP = builder
                    .comment("显示损坏盔甲的工具提示")
                    .define("showBrokenArmorTooltip", true);

            builder.pop();
        }
    }
}