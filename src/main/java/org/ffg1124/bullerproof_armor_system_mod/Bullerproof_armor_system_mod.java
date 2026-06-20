package org.ffg1124.bullerproof_armor_system_mod;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.ffg1124.bullerproof_armor_system_mod.command.AmmoTierManager;
import org.ffg1124.bullerproof_armor_system_mod.command.ModCommands;
import org.ffg1124.bullerproof_armor_system_mod.item.ModCreativeModeTab;
import org.ffg1124.bullerproof_armor_system_mod.item.ModItems;

@Mod(Bullerproof_armor_system_mod.MODID)
public class Bullerproof_armor_system_mod {

    public static final String MODID = "bullerproof_armor_system_mod";

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MODID);

    public Bullerproof_armor_system_mod(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("{} 模组初始化中... (NeoForge)", MODID);

        ModItems.ITEMS.register(modEventBus);
        ModCreativeModeTab.CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        LOGGER.info("{} 模组已初始化", MODID);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("执行通用设置");
        event.enqueueWork(() -> {
            AmmoTierManager.init();
            LOGGER.info("弹药等级配置已初始化");
        });
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        ModCommands.register(event.getDispatcher());
        LOGGER.info("已注册甲弹系统命令");
    }

    public static org.slf4j.Logger getLogger() {
        return LOGGER;
    }
}