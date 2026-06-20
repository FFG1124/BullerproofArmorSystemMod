package org.ffg1124.bullerproof_armor_system_mod.handler;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;
import org.ffg1124.bullerproof_armor_system_mod.command.AmmoTierManager;
import org.ffg1124.bullerproof_armor_system_mod.command.GunTierManager;
import org.ffg1124.bullerproof_armor_system_mod.data.PlayerGunDataManager;
import org.ffg1124.bullerproof_armor_system_mod.util.PlatformHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = Bullerproof_armor_system_mod.MODID)
public class GunTierSyncHandler {

    private static final boolean DEBUG = true;
    private static final Map<UUID, Integer> cooldownMap = new HashMap<>();
    private static final int COOLDOWN_TICKS = 20;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {

        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        UUID uuid = player.getUUID();
        int cooldown = cooldownMap.getOrDefault(uuid, 0);
        if (cooldown > 0) {
            cooldownMap.put(uuid, cooldown - 1);
            return;
        }
        cooldownMap.put(uuid, COOLDOWN_TICKS);

        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.isEmpty()) return;

        var itemKey = BuiltInRegistries.ITEM.getKey(mainHand.getItem());
        if (itemKey == null) {
            if (DEBUG) {
                Bullerproof_armor_system_mod.getLogger().warn(
                        "无法获取物品注册名: {}", mainHand.getItem().getClass().getName()
                );
            }
            return;
        }

        String itemId = itemKey.toString();
        boolean isRangedWeapon = isRangedWeapon(mainHand);
        if (!isRangedWeapon) return;

        int highestAmmoTier = getHighestAmmoTierInInventory(player);
        int lastAmmoTier = PlayerGunDataManager.getLastAmmoTier(player, itemId);

        int targetWeaponTier;

        if (highestAmmoTier > 0) {
            targetWeaponTier = highestAmmoTier;
            PlayerGunDataManager.setLastAmmoTier(player, itemId, highestAmmoTier);
            if (DEBUG) {
                Bullerproof_armor_system_mod.getLogger().info(
                        "玩家 {} 武器 {} 检测到弹药等级 {}, 同步到武器",
                        player.getName().getString(), itemId, highestAmmoTier
                );
            }
        } else {
            targetWeaponTier = lastAmmoTier;
            if (DEBUG && targetWeaponTier > 0) {
                Bullerproof_armor_system_mod.getLogger().info(
                        "玩家 {} 武器 {} 无弹药，使用记忆等级 {}",
                        player.getName().getString(), itemId, targetWeaponTier
                );
            }
        }

        int currentTier = GunTierManager.getGunTier(itemId);
        if (targetWeaponTier != currentTier) {
            if (targetWeaponTier > 0) {
                GunTierManager.setDynamicGunTier(itemId, targetWeaponTier);
                if (DEBUG) {
                    Bullerproof_armor_system_mod.getLogger().info(
                            "武器 {} 等级更新: {} -> {}",
                            itemId, currentTier, targetWeaponTier
                    );
                }
            }
        }
    }

    private static boolean isRangedWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;

        if (stack.getItem() instanceof BowItem) return true;
        if (stack.getItem() instanceof CrossbowItem) return true;

        if (stack.is(Items.TRIDENT)) return true;
        if (stack.is(Items.SNOWBALL)) return true;
        if (stack.is(Items.EGG)) return true;
        if (stack.is(Items.ENDER_PEARL)) return true;
        if (stack.is(Items.FISHING_ROD)) return true;

        String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

        String[] gunModPrefixes = {
                "tacz:", "pillagers_gun:", "pointblank:", "pb:", "mrw:", "cgm:",
                "techguns:", "flansmod:", "modernwarfare:", "mw:", "vicguns:",
                "scguns:", "gun:", "firearm:", "weapon:", "rifle:", "pistol:",
                "shotgun:", "sniper:", "machine_gun:"
        };

        for (String prefix : gunModPrefixes) {
            if (itemId.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    private static int getHighestAmmoTierInInventory(Player player) {
        int highestTier = 0;

        int mainHandTier = getAmmoTierFromStack(player.getMainHandItem());
        if (mainHandTier > highestTier) highestTier = mainHandTier;

        int offHandTier = getAmmoTierFromStack(player.getOffhandItem());
        if (offHandTier > highestTier) highestTier = offHandTier;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            int tier = getAmmoTierFromStack(player.getInventory().getItem(i));
            if (tier > highestTier) highestTier = tier;
        }

        for (ItemStack armor : player.getInventory().armor) {
            int tier = getAmmoTierFromStack(armor);
            if (tier > highestTier) highestTier = tier;
        }

        return highestTier;
    }

    private static int getAmmoTierFromStack(ItemStack stack) {
        if (stack.isEmpty()) return 0;

        var itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemKey == null) return 0;
        String itemId = itemKey.toString();

        var tag = PlatformHelper.getTag(stack);
        if (tag != null && tag.contains("AmmoId")) {
            String ammoId = tag.getString("AmmoId");
            int tier = AmmoTierManager.getAmmoTier(ammoId);
            if (tier > 0) return tier;
        }

        int tier = AmmoTierManager.getAmmoTier(itemId);
        if (tier > 0) return tier;

        if (isAmmoItem(itemId)) {
            return 1;
        }

        return 0;
    }

    private static boolean isAmmoItem(String itemId) {
        String[] ammoKeywords = {
                "bullet", "ammo", "cartridge", "round", "magazine",
                "clip", "shell", "ammunition", "9mm", "45acp", "556",
                "762", "308", "50bmg", "12g", "slug", "buckshot"
        };

        String lowerId = itemId.toLowerCase();
        for (String keyword : ammoKeywords) {
            if (lowerId.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}