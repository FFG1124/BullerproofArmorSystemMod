package org.ffg1124.bullerproof_armor_system_mod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = "bullerproof_armor_system_mod", bus = EventBusSubscriber.Bus.MOD)
public class KeyBindings {

    public static final String KEY_CATEGORY = "key.category.bullerproof_armor_system_mod";
    public static final String KEY_OPEN_CONFIG = "key.bullerproof_armor_system_mod.open_config";

    public static KeyMapping openConfigKey = new KeyMapping(
            KEY_OPEN_CONFIG,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F8,
            KEY_CATEGORY
    );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(openConfigKey);
    }

    @EventBusSubscriber(modid = "bullerproof_armor_system_mod")
    public static class KeyHandler {
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            if (openConfigKey.isDown()) {
                net.minecraft.client.Minecraft.getInstance().execute(() -> {
                    net.minecraft.client.Minecraft.getInstance().setScreen(new ModConfigScreen());
                });
            }
        }
    }
}