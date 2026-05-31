package org.ffg1124.bullerproof_armor_system_mod.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Bullerproof_armor_system_mod.MODID);

    // 初级护甲维修包 - 200耐久
    public static final RegistryObject<Item> BASIC_REPAIR_KIT = ITEMS.register("basic_repair_kit",
            () -> new ArmorRepairKitItem(ArmorRepairKitItem.RepairKitTier.BASIC));

    // 中级护甲维修包 - 500耐久
    public static final RegistryObject<Item> MEDIUM_REPAIR_KIT = ITEMS.register("medium_repair_kit",
            () -> new ArmorRepairKitItem(ArmorRepairKitItem.RepairKitTier.MEDIUM));

    // 高级护甲维修包 - 1000耐久
    public static final RegistryObject<Item> ADVANCED_REPAIR_KIT = ITEMS.register("advanced_repair_kit",
            () -> new ArmorRepairKitItem(ArmorRepairKitItem.RepairKitTier.ADVANCED));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        Bullerproof_armor_system_mod.getLogger().info("已注册维修包物品");
    }
}