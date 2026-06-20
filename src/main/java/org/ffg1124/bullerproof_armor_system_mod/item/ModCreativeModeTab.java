package org.ffg1124.bullerproof_armor_system_mod.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;

public class ModCreativeModeTab {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, Bullerproof_armor_system_mod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BAS_TAB =
            CREATIVE_MODE_TABS.register("bas_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup." + Bullerproof_armor_system_mod.MODID))
                    .icon(() -> new ItemStack(ModItems.ADVANCED_REPAIR_KIT.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.BASIC_REPAIR_KIT.get());
                        output.accept(ModItems.MEDIUM_REPAIR_KIT.get());
                        output.accept(ModItems.ADVANCED_REPAIR_KIT.get());
                    })
                    .build());
}