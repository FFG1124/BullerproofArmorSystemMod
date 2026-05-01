package org.ffg1124.bullerproof_armor_system_mod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ModConfigScreen extends Screen {

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;

    public ModConfigScreen() {
        super(Component.translatable("bullerproof_armor_system_mod.config.title"));
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int startY = this.height / 2 - 40;

        addRenderableWidget(Button.builder(
                Component.translatable("bullerproof_armor_system_mod.config.armor"),
                button -> openArmorConfig()
        ).bounds(centerX - BUTTON_WIDTH / 2, startY, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        addRenderableWidget(Button.builder(
                Component.translatable("bullerproof_armor_system_mod.config.ammo"),
                button -> openAmmoConfig()
        ).bounds(centerX - BUTTON_WIDTH / 2, startY + 30, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        addRenderableWidget(Button.builder(
                Component.translatable("bullerproof_armor_system_mod.config.weapon"),
                button -> openWeaponConfig()
        ).bounds(centerX - BUTTON_WIDTH / 2, startY + 60, BUTTON_WIDTH, BUTTON_HEIGHT).build());

        addRenderableWidget(Button.builder(
                Component.translatable("gui.done"),
                button -> onClose()
        ).bounds(centerX - 50, this.height - 30, 100, 20).build());
    }

    private void openArmorConfig() {
        Minecraft.getInstance().setScreen(new ArmorConfigScreen(this));
    }

    private void openAmmoConfig() {
        Minecraft.getInstance().setScreen(new AmmoConfigScreen(this));
    }

    private void openWeaponConfig() {
        Minecraft.getInstance().setScreen(new WeaponConfigScreen(this));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        guiGraphics.drawCenteredString(font,
                Component.translatable("bullerproof_armor_system_mod.config.title").getString(),
                width / 2, height / 2 - 80, 0xFFFFFF);

        guiGraphics.drawCenteredString(font,
                "§7" + Component.translatable("bullerproof_armor_system_mod.config.shortcut").getString() + ": §eF8",
                width / 2, height / 2 + 100, 0x888888);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}