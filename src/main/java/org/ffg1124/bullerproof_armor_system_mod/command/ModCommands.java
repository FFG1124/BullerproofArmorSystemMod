package org.ffg1124.bullerproof_armor_system_mod.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;

@EventBusSubscriber(modid = Bullerproof_armor_system_mod.MODID)
public class ModCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        BasCommand.register(dispatcher);
        Bullerproof_armor_system_mod.getLogger().info("Registered BAS commands");
    }
}