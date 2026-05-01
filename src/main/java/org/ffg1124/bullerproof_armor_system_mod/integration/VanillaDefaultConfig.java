package org.ffg1124.bullerproof_armor_system_mod.integration;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;
import org.ffg1124.bullerproof_armor_system_mod.command.ArmorTierManager;
import org.ffg1124.bullerproof_armor_system_mod.command.WeaponTierManager;

@Mod.EventBusSubscriber(modid = Bullerproof_armor_system_mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class VanillaDefaultConfig {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Bullerproof_armor_system_mod.getLogger().info("正在为原版物品配置默认等级...");

            // ==================== 原版护甲等级配置 ====================
            // 皮革套 - 1级
            setArmorTier("minecraft:leather_helmet", 1);
            setArmorTier("minecraft:leather_chestplate", 1);
            setArmorTier("minecraft:leather_leggings", 1);
            setArmorTier("minecraft:leather_boots", 1);

            // 锁链套 - 2级
            setArmorTier("minecraft:chainmail_helmet", 2);
            setArmorTier("minecraft:chainmail_chestplate", 2);
            setArmorTier("minecraft:chainmail_leggings", 2);
            setArmorTier("minecraft:chainmail_boots", 2);

            // 铁套 - 3级
            setArmorTier("minecraft:iron_helmet", 4);
            setArmorTier("minecraft:iron_chestplate", 4);
            setArmorTier("minecraft:iron_leggings", 4);
            setArmorTier("minecraft:iron_boots", 4);

            // 金套 - 2级 (金质较软)
            setArmorTier("minecraft:golden_helmet", 3);
            setArmorTier("minecraft:golden_chestplate", 3);
            setArmorTier("minecraft:golden_leggings", 3);
            setArmorTier("minecraft:golden_boots", 3);

            // 钻石套 - 4级
            setArmorTier("minecraft:diamond_helmet", 5);
            setArmorTier("minecraft:diamond_chestplate", 5);
            setArmorTier("minecraft:diamond_leggings", 5);
            setArmorTier("minecraft:diamond_boots", 5);

            // 下界合金套 - 5级
            setArmorTier("minecraft:netherite_helmet", 6);
            setArmorTier("minecraft:netherite_chestplate", 6);
            setArmorTier("minecraft:netherite_leggings", 6);
            setArmorTier("minecraft:netherite_boots", 6);

            // 乌龟壳 - 3级
            setArmorTier("minecraft:turtle_helmet", 3);

            // ==================== 原版武器等级配置 ====================
            // 木质武器 - 1级
            setWeaponTier("minecraft:wooden_sword", 1);
            setWeaponTier("minecraft:wooden_axe", 1);
            setWeaponTier("minecraft:wooden_pickaxe", 1);
            setWeaponTier("minecraft:wooden_shovel", 1);
            setWeaponTier("minecraft:wooden_hoe", 1);

            // 石质武器 - 2级
            setWeaponTier("minecraft:stone_sword", 2);
            setWeaponTier("minecraft:stone_axe", 2);
            setWeaponTier("minecraft:stone_pickaxe", 2);
            setWeaponTier("minecraft:stone_shovel", 2);
            setWeaponTier("minecraft:stone_hoe", 2);

            // 金质武器 - 2级 (速度快但伤害低)
            setWeaponTier("minecraft:golden_sword", 3);
            setWeaponTier("minecraft:golden_axe", 3);
            setWeaponTier("minecraft:golden_pickaxe", 3);
            setWeaponTier("minecraft:golden_shovel", 3);
            setWeaponTier("minecraft:golden_hoe", 3);

            // 铁质武器 - 3级
            setWeaponTier("minecraft:iron_sword", 4);
            setWeaponTier("minecraft:iron_axe", 4);
            setWeaponTier("minecraft:iron_pickaxe", 4);
            setWeaponTier("minecraft:iron_shovel", 4);
            setWeaponTier("minecraft:iron_hoe", 4);

            // 钻石武器 - 4级
            setWeaponTier("minecraft:diamond_sword", 5);
            setWeaponTier("minecraft:diamond_axe", 5);
            setWeaponTier("minecraft:diamond_pickaxe", 5);
            setWeaponTier("minecraft:diamond_shovel", 5);
            setWeaponTier("minecraft:diamond_hoe", 5);

            // 下界合金武器 - 5级
            setWeaponTier("minecraft:netherite_sword", 6);
            setWeaponTier("minecraft:netherite_axe", 6);
            setWeaponTier("minecraft:netherite_pickaxe", 6);
            setWeaponTier("minecraft:netherite_shovel", 6);
            setWeaponTier("minecraft:netherite_hoe", 6);

            // 弓和弩 - 3级
            setWeaponTier("minecraft:bow", 3);
            setWeaponTier("minecraft:crossbow", 3);

            // 三叉戟 - 4级
            setWeaponTier("minecraft:trident", 4);

            // 盾牌 - 特殊（不是武器，但添加等级用于减伤）
            setWeaponTier("minecraft:shield", 2);

            Bullerproof_armor_system_mod.getLogger().info("原版物品默认等级配置完成！");
        });
    }

    private static void setArmorTier(String itemId, int tier) {
        ArmorTierManager.setArmorTierInternal(itemId, tier);
        Bullerproof_armor_system_mod.getLogger().debug("配置护甲: {} -> {}级", itemId, tier);
    }

    private static void setWeaponTier(String itemId, int tier) {
        WeaponTierManager.setWeaponTierInternal(itemId, tier);
        Bullerproof_armor_system_mod.getLogger().debug("配置武器: {} -> {}级", itemId, tier);
    }
}