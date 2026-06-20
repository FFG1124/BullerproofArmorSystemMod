package org.ffg1124.bullerproof_armor_system_mod.data;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = Bullerproof_armor_system_mod.MODID)
public class PlayerGunDataManager {

    private static final Map<UUID, Map<String, Integer>> playerGunLastAmmoTier = new ConcurrentHashMap<>();

    public static int getLastAmmoTier(Player player, String gunId) {
        UUID uuid = player.getUUID();
        Map<String, Integer> gunMap = playerGunLastAmmoTier.get(uuid);
        if (gunMap == null) return 0;
        return gunMap.getOrDefault(gunId, 0);
    }

    public static void setLastAmmoTier(Player player, String gunId, int tier) {
        UUID uuid = player.getUUID();
        Map<String, Integer> gunMap = playerGunLastAmmoTier.computeIfAbsent(uuid, k -> new HashMap<>());
        gunMap.put(gunId, tier);
    }

    public static void clearPlayerData(Player player) {
        playerGunLastAmmoTier.remove(player.getUUID());
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            savePlayerData(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            loadPlayerData(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.getOriginal() instanceof ServerPlayer oldPlayer && event.getEntity() instanceof ServerPlayer newPlayer) {
            Map<String, Integer> oldData = playerGunLastAmmoTier.get(oldPlayer.getUUID());
            if (oldData != null) {
                playerGunLastAmmoTier.put(newPlayer.getUUID(), new HashMap<>(oldData));
            }
        }
    }

    private static void savePlayerData(ServerPlayer player) {
        // 可选实现
    }

    private static void loadPlayerData(ServerPlayer player) {
        // 可选实现
    }
}