package org.ffg1124.bullerproof_armor_system_mod.handler;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
    private static final Map<UUID, Integer> cooldownMap = new HashMap<>();
    private static final int COOLDOWN_TICKS = 20;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (event.player.level().isClientSide) return;

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

        // ========== 修复：安全获取物品ID ==========
        var itemKey = ForgeRegistries.ITEMS.getKey(mainHand.getItem());
        if (itemKey == null) {
            if (DEBUG) {
                Bullerproof_armor_system_mod.getLogger().warn(
                        "无法获取物品注册名: {}", mainHand.getItem().getClass().getName()
                );
            }
            return;
        }

        String itemId = itemKey.toString();

        // ========== 修改：识别所有远程武器 ==========
        boolean isRangedWeapon = isRangedWeapon(mainHand);

        if (!isRangedWeapon) return;

        // 检测背包中最高等级的弹药
        int highestAmmoTier = getHighestAmmoTierInInventory(player);

        // 获取该武器的上次弹药等级
        int lastAmmoTier = PlayerGunDataManager.getLastAmmoTier(player, itemId);

        int targetWeaponTier;

        if (highestAmmoTier > 0) {
            // 背包中有弹药：使用最高弹药等级
            targetWeaponTier = highestAmmoTier;
            PlayerGunDataManager.setLastAmmoTier(player, itemId, highestAmmoTier);
            if (DEBUG) {
                Bullerproof_armor_system_mod.getLogger().info(
                        "玩家 {} 武器 {} 检测到弹药等级 {}, 同步到武器",
                        player.getName().getString(), itemId, highestAmmoTier
                );
            }
        } else {
            // 背包中无弹药：使用上次记忆的等级
            targetWeaponTier = lastAmmoTier;
            if (DEBUG && targetWeaponTier > 0) {
                Bullerproof_armor_system_mod.getLogger().info(
                        "玩家 {} 武器 {} 无弹药，使用记忆等级 {}",
                        player.getName().getString(), itemId, targetWeaponTier
                );
            }
        }

        // 更新武器等级
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

    /**
     * 判断是否为远程武器（可以发射弹射物）
     */
    private static boolean isRangedWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;

        // 原版弓和弩
        if (stack.getItem() instanceof BowItem) return true;
        if (stack.getItem() instanceof CrossbowItem) return true;

        // 三叉戟（可投掷）
        if (stack.is(Items.TRIDENT)) return true;

        // 雪球、鸡蛋、末影珍珠等投掷物（通过物品比较）
        if (stack.is(Items.SNOWBALL)) return true;
        if (stack.is(Items.EGG)) return true;
        if (stack.is(Items.ENDER_PEARL)) return true;

        // 钓鱼竿
        if (stack.is(Items.FISHING_ROD)) return true;

        // 所有模组的枪械（通过物品ID判断是否为远程武器）
        String itemId = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();

        // 常见枪械模组的前缀列表
        String[] gunModPrefixes = {
                "tacz:",           // 永恒枪械工坊
                "pillagers_gun:",  // 掠夺者的枪
                "pointblank:",     // Vic's Point Blank
                "pb:",             // Point Blank 缩写
                "mrw:",            // MrCrayfish's Gun Mod
                "cgm:",            // MrCrayfish's Gun Mod (新版)
                "techguns:",       // Techguns
                "flansmod:",       // Flan's Mod
                "modernwarfare:",  // Modern Warfare
                "mw:",             // Modern Warfare 缩写
                "vicguns:",        // Vic's Guns
                "scguns:",         // Scorpio's Gun Mod
                "gun:",            // 通用枪械
                "firearm:",        // 通用火器
                "weapon:",         // 通用武器
                "rifle:",          // 步枪
                "pistol:",         // 手枪
                "shotgun:",        // 霰弹枪
                "sniper:",         // 狙击枪
                "machine_gun:"     // 机枪
        };

        for (String prefix : gunModPrefixes) {
            if (itemId.startsWith(prefix)) {
                return true;
            }
        }

        return false;
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

        // 检查盔甲栏
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

        var itemKey = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemKey == null) return 0;
        String itemId = itemKey.toString();

        // 1. 检查 AmmoId NBT
        var tag = stack.getTag();
        if (tag != null && tag.contains("AmmoId")) {
            String ammoId = tag.getString("AmmoId");
            int tier = AmmoTierManager.getAmmoTier(ammoId);
            if (tier > 0) return tier;
        }

        // 2. 检查物品本身的弹药等级
        int tier = AmmoTierManager.getAmmoTier(itemId);
        if (tier > 0) return tier;

        // 3. 根据物品名称推断是否为弹药
        if (isAmmoItem(itemId)) {
            return 1; // 默认为1级
        }

        return 0;
    }

    /**
     * 判断是否为弹药物品
     */
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