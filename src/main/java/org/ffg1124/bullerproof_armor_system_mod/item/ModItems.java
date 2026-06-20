package org.ffg1124.bullerproof_armor_system_mod.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, Bullerproof_armor_system_mod.MODID);

    public static final DeferredHolder<Item, ArmorRepairKitItem> BASIC_REPAIR_KIT =
            ITEMS.register("basic_repair_kit",
                    () -> new ArmorRepairKitItem(ArmorRepairKitItem.RepairKitTier.BASIC));

    public static final DeferredHolder<Item, ArmorRepairKitItem> MEDIUM_REPAIR_KIT =
            ITEMS.register("medium_repair_kit",
                    () -> new ArmorRepairKitItem(ArmorRepairKitItem.RepairKitTier.MEDIUM));

    public static final DeferredHolder<Item, ArmorRepairKitItem> ADVANCED_REPAIR_KIT =
            ITEMS.register("advanced_repair_kit",
                    () -> new ArmorRepairKitItem(ArmorRepairKitItem.RepairKitTier.ADVANCED));
}