package org.ffg1124.bullerproof_armor_system_mod.handler;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;
import org.ffg1124.bullerproof_armor_system_mod.command.AmmoTierManager;
import org.ffg1124.bullerproof_armor_system_mod.command.GunTierManager;
import org.ffg1124.bullerproof_armor_system_mod.data.PlayerGunDataManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Bullerproof_armor_system_mod.MODID)
public class GunTierSyncHandler {

    private static final boolean DEBUG = true;

    // 缓存每个玩家当前使用的枪械和检测冷却（每20 tick检测一次）
    private static final Map<UUID, Integer> cooldownMap = new HashMap<>();
    private static final int COOLDOWN_TICKS = 20;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide) return; // 只在服务端执行

        Player player = event.player;
        UUID uuid = player.getUUID();

        // 冷却控制
        int cooldown = cooldownMap.getOrDefault(uuid, 0);
        if (cooldown > 0) {
            cooldownMap.put(uuid, cooldown - 1);
            return;
        }
        cooldownMap.put(uuid, COOLDOWN_TICKS);

        // 获取玩家当前主手物品
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.isEmpty()) return;

        String itemId = ForgeRegistries.ITEMS.getKey(mainHand.getItem()).toString();
        if (!itemId.startsWith("tacz:")) return; // 只处理TACZ枪械

        // 检测背包中最高等级的弹药
        int highestAmmoTier = getHighestAmmoTierInInventory(player);

        // 获取该枪械的上次弹药等级
        int lastAmmoTier = PlayerGunDataManager.getLastAmmoTier(player, itemId);

        int targetGunTier;

        if (highestAmmoTier > 0) {
            // 背包中有弹药：使用最高弹药等级
            targetGunTier = highestAmmoTier;
            // 记忆这个等级
            PlayerGunDataManager.setLastAmmoTier(player, itemId, highestAmmoTier);
            if (DEBUG) {
                Bullerproof_armor_system_mod.getLogger().info(
                        "玩家 {} 枪械 {} 背包检测到弹药等级 {}, 同步到枪械",
                        player.getName().getString(), itemId, highestAmmoTier
                );
            }
        } else {
            // 背包中无弹药：使用上次记忆的等级
            targetGunTier = lastAmmoTier;
            if (DEBUG && targetGunTier > 0) {
                Bullerproof_armor_system_mod.getLogger().info(
                        "玩家 {} 枪械 {} 背包无弹药，使用记忆等级 {}",
                        player.getName().getString(), itemId, targetGunTier
                );
            }
        }

        // 更新枪械等级（仅当等级变化时）
        int currentGunTier = GunTierManager.getGunTier(itemId);
        if (targetGunTier != currentGunTier) {
            if (targetGunTier > 0) {
                GunTierManager.setGunTier(itemId, targetGunTier);
                if (DEBUG) {
                    Bullerproof_armor_system_mod.getLogger().info(
                            "枪械 {} 等级更新: {} -> {}",
                            itemId, currentGunTier, targetGunTier
                    );
                }
            } else if (currentGunTier > 0 && targetGunTier == 0) {
                // 可选：当没有弹药也没有记忆时，清除枪械等级
                // GunTierManager.removeGunTier(itemId);
            }
        }
    }

    /**
     * 检测玩家背包中最高等级的弹药
     */
    private static int getHighestAmmoTierInInventory(Player player) {
        int highestTier = 0;

        // 检查主手
        int mainHandTier = getAmmoTierFromStack(player.getMainHandItem());
        if (mainHandTier > highestTier) highestTier = mainHandTier;

        // 检查副手
        int offHandTier = getAmmoTierFromStack(player.getOffhandItem());
        if (offHandTier > highestTier) highestTier = offHandTier;

        // 检查背包所有格子
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            int tier = getAmmoTierFromStack(player.getInventory().getItem(i));
            if (tier > highestTier) highestTier = tier;
        }

        // 检查盔甲栏（如果有弹药）
        for (ItemStack armor : player.getInventory().armor) {
            int tier = getAmmoTierFromStack(armor);
            if (tier > highestTier) highestTier = tier;
        }

        return highestTier;
    }

    /**
     * 从物品堆获取弹药等级
     */
    private static int getAmmoTierFromStack(ItemStack stack) {
        if (stack.isEmpty()) return 0;

        String itemId = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();

        // 优先检查 AmmoId NBT
        var tag = stack.getTag();
        if (tag != null && tag.contains("AmmoId")) {
            String ammoId = tag.getString("AmmoId");
            int tier = AmmoTierManager.getAmmoTier(ammoId);
            if (tier > 0) return tier;
        }

        // 检查物品本身的弹药等级
        return AmmoTierManager.getAmmoTier(itemId);
    }
}