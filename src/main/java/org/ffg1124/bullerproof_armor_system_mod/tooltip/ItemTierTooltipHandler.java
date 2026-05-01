package org.ffg1124.bullerproof_armor_system_mod.tooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;
import org.ffg1124.bullerproof_armor_system_mod.ballistic.BallisticUtils;
import org.ffg1124.bullerproof_armor_system_mod.command.AmmoTierManager;
import org.ffg1124.bullerproof_armor_system_mod.command.ArmorTierManager;
import org.ffg1124.bullerproof_armor_system_mod.command.WeaponTierManager;

import java.util.List;

@Mod.EventBusSubscriber(modid = Bullerproof_armor_system_mod.MODID)
public class ItemTierTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;

        List<Component> tooltip = event.getToolTip();
        ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemKey == null) return;
        String itemId = itemKey.toString();

        // ==================== 武器等级显示 ====================
        if (isWeapon(stack)) {
            int weaponTier = WeaponTierManager.getWeaponTier(itemId);

            if (weaponTier > 0) {
                float damageBonus = 1.0f + (weaponTier - 1) * 0.2f;
                float damageBonusPercent = (damageBonus - 1) * 100;

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("bullerproof_armor_system_mod.tooltip.weapon_tier", weaponTier)
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                tooltip.add(Component.translatable("bullerproof_armor_system_mod.tooltip.weapon_damage_bonus", String.format("%.0f", damageBonusPercent))
                        .withStyle(ChatFormatting.GOLD));
            }
            return;
        }

        // ==================== 弹药等级显示 ====================
        String ammoId = BallisticUtils.getAmmoIdFromNBT(stack);
        int ammoTier = 0;

        if (ammoId != null && !ammoId.isEmpty()) {
            ammoTier = AmmoTierManager.getAmmoTier(ammoId);
        }

        if (ammoTier == 0) {
            ammoTier = AmmoTierManager.getAmmoTier(itemId);
        }

        if (ammoTier > 0) {
            float damageBonus = 1.0f + (ammoTier - 1) * 0.2f;
            float damageBonusPercent = (damageBonus - 1) * 100;

            tooltip.add(Component.literal(""));
            tooltip.add(Component.translatable("bullerproof_armor_system_mod.tooltip.ammo_tier", ammoTier)
                    .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            tooltip.add(Component.translatable("bullerproof_armor_system_mod.tooltip.ammo_damage_bonus", String.format("%.0f", damageBonusPercent))
                    .withStyle(ChatFormatting.GOLD));
            return;
        }

        // ==================== 护甲等级显示 ====================
        if (stack.getItem() instanceof ArmorItem) {
            int armorTier = ArmorTierManager.getArmorTier(itemId);

            if (armorTier > 0) {
                float reduction = Math.min(armorTier * 0.1f, 0.6f);
                float reductionPercent = reduction * 100;

                tooltip.add(Component.literal(""));
                tooltip.add(Component.translatable("bullerproof_armor_system_mod.tooltip.armor_tier", armorTier)
                        .withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                tooltip.add(Component.translatable("bullerproof_armor_system_mod.tooltip.armor_protection", String.format("%.0f", reductionPercent))
                        .withStyle(ChatFormatting.GREEN));
            }
            return;
        }
    }

    private static boolean isWeapon(ItemStack stack) {
        return stack.getItem() instanceof SwordItem ||
                stack.getItem() instanceof AxeItem ||
                stack.getItem() instanceof BowItem ||
                stack.getItem() instanceof CrossbowItem ||
                stack.getItem() instanceof TridentItem;
    }
}