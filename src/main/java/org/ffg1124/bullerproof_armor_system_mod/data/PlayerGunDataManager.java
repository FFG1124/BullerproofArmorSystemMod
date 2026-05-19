package org.ffg1124.bullerproof_armor_system_mod.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = Bullerproof_armor_system_mod.MODID)
public class PlayerGunDataManager {

    // 玩家数据存储：UUID -> (GunId -> 上次弹药等级)
    private static final Map<UUID, Map<String, Integer>> playerGunLastAmmoTier = new ConcurrentHashMap<>();

    /**
     * 获取玩家某把枪械的上次弹药等级（记忆值）
     */
    public static int getLastAmmoTier(Player player, String gunId) {
        UUID uuid = player.getUUID();
        Map<String, Integer> gunMap = playerGunLastAmmoTier.get(uuid);
        if (gunMap == null) return 0;
        return gunMap.getOrDefault(gunId, 0);
    }

    /**
     * 设置玩家某把枪械的上次弹药等级
     */
    public static void setLastAmmoTier(Player player, String gunId, int tier) {
        UUID uuid = player.getUUID();
        Map<String, Integer> gunMap = playerGunLastAmmoTier.computeIfAbsent(uuid, k -> new HashMap<>());
        gunMap.put(gunId, tier);
    }

    /**
     * 清除玩家所有数据
     */
    public static void clearPlayerData(Player player) {
        playerGunLastAmmoTier.remove(player.getUUID());
    }

    // ==================== 数据持久化 ====================

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        // 玩家退出时保存数据到 NBT（可选：保存到文件）
        if (event.getEntity() instanceof ServerPlayer player) {
            savePlayerData(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        // 玩家登录时加载数据
        if (event.getEntity() instanceof ServerPlayer player) {
            loadPlayerData(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // 玩家重生时转移数据
        if (event.getOriginal() instanceof ServerPlayer oldPlayer && event.getEntity() instanceof ServerPlayer newPlayer) {
            Map<String, Integer> oldData = playerGunLastAmmoTier.get(oldPlayer.getUUID());
            if (oldData != null) {
                playerGunLastAmmoTier.put(newPlayer.getUUID(), new HashMap<>(oldData));
            }
        }
    }

    private static void savePlayerData(ServerPlayer player) {
        // 可选：保存到服务器的 JSON 文件
        // 这里简单实现，实际可使用 Capability
    }

    private static void loadPlayerData(ServerPlayer player) {
        // 可选：从文件加载
    }
}