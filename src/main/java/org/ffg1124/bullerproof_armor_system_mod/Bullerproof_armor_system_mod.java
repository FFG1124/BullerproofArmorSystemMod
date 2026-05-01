package org.ffg1124.bullerproof_armor_system_mod;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import org.ffg1124.bullerproof_armor_system_mod.command.AmmoTierManager;

@Mod(Bullerproof_armor_system_mod.MODID)
@SuppressWarnings("removal")
public class Bullerproof_armor_system_mod {

    public static final String MODID = "bullerproof_armor_system_mod";

    private static final org.apache.logging.log4j.Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger();

    public Bullerproof_armor_system_mod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);

        // 删除 HudConfig.register() 这行

        // 注册通用配置
        ModLoadingContext.get().registerConfig(
                net.minecraftforge.fml.config.ModConfig.Type.COMMON, Config.SPEC
        );

        LOGGER.info("{} 模组已初始化", MODID);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("执行通用设置");

        // 初始化弹药等级配置
        event.enqueueWork(() -> {
            AmmoTierManager.init();
            LOGGER.info("弹药等级配置已初始化");
        });
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("服务器启动，注册命令");
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        // 命令已在 ModCommands 中注册
        LOGGER.info("已注册甲弹系统命令");
    }

    public static org.apache.logging.log4j.Logger getLogger() {
        return LOGGER;
    }
}