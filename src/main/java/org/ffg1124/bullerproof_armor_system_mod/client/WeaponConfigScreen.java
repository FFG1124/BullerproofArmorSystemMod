package org.ffg1124.bullerproof_armor_system_mod.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.ffg1124.bullerproof_armor_system_mod.command.WeaponTierManager;

@OnlyIn(Dist.CLIENT)
public class WeaponConfigScreen extends Screen {

    private final Screen parent;
    private EditBox tierInput;
    private Button setButton;
    private Button removeButton;
    private Button listButton;
    private Button backButton;

    private String currentItemInfo = "";
    private String statusMessage = "";
    private int statusTimer = 0;

    public WeaponConfigScreen(Screen parent) {
        super(Component.translatable("bullerproof_armor_system_mod.config.weapon.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int startY = this.height / 2 - 60;

        tierInput = new EditBox(font, centerX - 50, startY, 100, 20, Component.translatable("bullerproof_armor_system_mod.config.input_hint"));
        tierInput.setMaxLength(2);
        tierInput.setFilter(s -> s.matches("[1-6]?"));
        tierInput.setHint(Component.translatable("bullerproof_armor_system_mod.config.input_hint"));
        addRenderableWidget(tierInput);

        setButton = addRenderableWidget(Button.builder(
                Component.translatable("bullerproof_armor_system_mod.config.set"),
                button -> setWeaponTier()
        ).bounds(centerX - 100, startY + 30, 90, 20).build());

        removeButton = addRenderableWidget(Button.builder(
                Component.translatable("bullerproof_armor_system_mod.config.remove"),
                button -> removeWeaponTier()
        ).bounds(centerX + 10, startY + 30, 90, 20).build());

        listButton = addRenderableWidget(Button.builder(
                Component.translatable("bullerproof_armor_system_mod.config.list"),
                button -> listWeaponTiers()
        ).bounds(centerX - 100, startY + 60, 200, 20).build());

        backButton = addRenderableWidget(Button.builder(
                Component.translatable("gui.back"),
                button -> onClose()
        ).bounds(centerX - 50, this.height - 30, 100, 20).build());

        updateCurrentItemInfo();
    }

    private void updateCurrentItemInfo() {
        ItemStack handItem = Minecraft.getInstance().player.getMainHandItem();
        if (handItem.isEmpty()) {
            currentItemInfo = "§7" + Component.translatable("bullerproof_armor_system_mod.config.holding").getString() + ": §c" +
                    Component.translatable("bullerproof_armor_system_mod.config.no_item").getString();
        } else {
            String itemName = handItem.getDisplayName().getString();
            String itemId = ForgeRegistries.ITEMS.getKey(handItem.getItem()).toString();
            int currentTier = WeaponTierManager.getWeaponTier(itemId);
            String tierText = currentTier > 0 ? "§aLv" + currentTier : "§c" +
                    Component.translatable("bullerproof_armor_system_mod.config.not_configured").getString();
            currentItemInfo = "§7" + Component.translatable("bullerproof_armor_system_mod.config.holding").getString() +
                    ": §e" + itemName + "§7 (ID: §8" + itemId + "§7) " +
                    Component.translatable("bullerproof_armor_system_mod.config.current_tier").getString() + ": " + tierText;
        }
    }

    private void setWeaponTier() {
        String input = tierInput.getValue();
        if (input.isEmpty()) {
            statusMessage = "§c" + Component.translatable("bullerproof_armor_system_mod.config.input_error_empty").getString();
            statusTimer = 60;
            return;
        }

        int tier;
        try {
            tier = Integer.parseInt(input);
            if (tier < 1 || tier > 6) {
                statusMessage = "§c" + Component.translatable("bullerproof_armor_system_mod.config.input_error_range").getString();
                statusTimer = 60;
                return;
            }
        } catch (NumberFormatException e) {
            statusMessage = "§c" + Component.translatable("bullerproof_armor_system_mod.config.input_error_number").getString();
            statusTimer = 60;
            return;
        }

        ItemStack handItem = Minecraft.getInstance().player.getMainHandItem();
        if (handItem.isEmpty()) {
            statusMessage = "§c" + Component.translatable("bullerproof_armor_system_mod.config.hold_weapon").getString();
            statusTimer = 60;
            return;
        }

        String itemId = ForgeRegistries.ITEMS.getKey(handItem.getItem()).toString();
        if (WeaponTierManager.setWeaponTier(itemId, tier)) {
            statusMessage = "§a" + String.format(Component.translatable("bullerproof_armor_system_mod.config.set_success").getString(),
                    handItem.getDisplayName().getString(), tier);
            statusTimer = 60;
            tierInput.setValue("");
            updateCurrentItemInfo();
        } else {
            statusMessage = "§c" + Component.translatable("bullerproof_armor_system_mod.config.set_failed").getString();
            statusTimer = 60;
        }
    }

    private void removeWeaponTier() {
        ItemStack handItem = Minecraft.getInstance().player.getMainHandItem();
        if (handItem.isEmpty()) {
            statusMessage = "§c" + Component.translatable("bullerproof_armor_system_mod.config.hold_weapon").getString();
            statusTimer = 60;
            return;
        }

        String itemId = ForgeRegistries.ITEMS.getKey(handItem.getItem()).toString();
        if (WeaponTierManager.removeWeaponTier(itemId)) {
            statusMessage = "§a" + String.format(Component.translatable("bullerproof_armor_system_mod.config.remove_success").getString(),
                    handItem.getDisplayName().getString());
            statusTimer = 60;
            updateCurrentItemInfo();
        } else {
            statusMessage = "§c" + Component.translatable("bullerproof_armor_system_mod.config.remove_failed").getString();
            statusTimer = 60;
        }
    }

    private void listWeaponTiers() {
        var configured = WeaponTierManager.getAllConfigured();
        if (configured.isEmpty()) {
            statusMessage = "§7" + Component.translatable("bullerproof_armor_system_mod.config.list_empty").getString();
            statusTimer = 60;
            return;
        }

        StringBuilder sb = new StringBuilder("§6" + Component.translatable("bullerproof_armor_system_mod.config.list_title").getString() + ": ");
        int count = 0;
        for (var entry : configured.entrySet()) {
            if (count > 0) sb.append(", ");
            sb.append(entry.getKey()).append(": Lv").append(entry.getValue());
            count++;
            if (count >= 5) {
                sb.append("... 共").append(configured.size()).append("个");
                break;
            }
        }
        statusMessage = sb.toString();
        statusTimer = 120;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics);

        guiGraphics.drawCenteredString(font,
                Component.translatable("bullerproof_armor_system_mod.config.weapon.title").getString(),
                width / 2, 20, 0xFFFFFF);

        updateCurrentItemInfo();
        guiGraphics.drawString(font, currentItemInfo, width / 2 - 100, height / 2 - 90, 0xAAAAAA, false);

        guiGraphics.drawString(font,
                Component.translatable("bullerproof_armor_system_mod.config.input_tier").getString() + ":",
                width / 2 - 50, height / 2 - 55, 0xAAAAAA, false);

        if (statusTimer > 0) {
            guiGraphics.drawCenteredString(font, statusMessage, width / 2, height / 2 + 100, 0xFFFF55);
            statusTimer--;
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}