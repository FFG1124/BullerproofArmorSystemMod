package org.ffg1124.bullerproof_armor_system_mod.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;

public class ModCreativeModeTab {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Bullerproof_armor_system_mod.MODID);

    public static final RegistryObject<CreativeModeTab> BAS_TAB = CREATIVE_MODE_TABS.register("bas_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.ADVANCED_REPAIR_KIT.get()))
                    .title(Component.translatable("itemGroup.bullerproof_armor_system_mod"))
                    .displayItems((parameters, output) -> {
                        // 添加维修包
                        output.accept(ModItems.BASIC_REPAIR_KIT.get());
                        output.accept(ModItems.MEDIUM_REPAIR_KIT.get());
                        output.accept(ModItems.ADVANCED_REPAIR_KIT.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
        Bullerproof_armor_system_mod.getLogger().info("已注册创造模式标签页");
    }
}