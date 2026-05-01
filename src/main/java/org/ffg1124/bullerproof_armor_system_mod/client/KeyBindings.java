package org.ffg1124.bullerproof_armor_system_mod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "bullerproof_armor_system_mod")
public class KeyBindings {

    public static final String KEY_CATEGORY = "key.category.bullerproof_armor_system_mod";
    public static final String KEY_OPEN_CONFIG = "key.bullerproof_armor_system_mod.open_config";

    public static KeyMapping openConfigKey = new KeyMapping(
            KEY_OPEN_CONFIG,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F8,
            KEY_CATEGORY
    );

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "bullerproof_armor_system_mod", bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class KeyRegistry {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(openConfigKey);
        }
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "bullerproof_armor_system_mod")
    public static class KeyHandler {
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event) {
            // 使用 isDown 而不是 consumeClick 来检测 F8
            if (openConfigKey.isDown()) {
                net.minecraft.client.Minecraft.getInstance().execute(() -> {
                    net.minecraft.client.Minecraft.getInstance().setScreen(new ModConfigScreen());
                });
            }
        }
    }
}