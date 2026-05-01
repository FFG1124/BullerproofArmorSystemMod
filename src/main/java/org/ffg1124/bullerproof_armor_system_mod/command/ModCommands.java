package org.ffg1124.bullerproof_armor_system_mod.command;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;

@Mod.EventBusSubscriber(modid = Bullerproof_armor_system_mod.MODID)
public class ModCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        BasCommand.register(event.getDispatcher());
        Bullerproof_armor_system_mod.getLogger().info("Registered BAS commands");
    }
}