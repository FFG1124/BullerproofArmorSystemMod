package org.ffg1124.bullerproof_armor_system_mod.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.ffg1124.bullerproof_armor_system_mod.Bullerproof_armor_system_mod;
import org.ffg1124.bullerproof_armor_system_mod.ballistic.BallisticUtils;

public class BasCommand {

    private static final String MOD_ID = Bullerproof_armor_system_mod.MODID;

    private static Component getMessage(String key, Object... args) {
        return Component.translatable(MOD_ID + "." + key, args);
    }

    private static String getTierName(String type, int tier) {
        return getMessage(type + ".tier.name." + tier).getString();
    }

    private static String getItemName(ItemStack stack) {
        return stack.getDisplayName().getString();
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bas")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("help")
                        .executes(BasCommand::showHelp))

                // ==================== 护甲命令 ====================
                .then(Commands.literal("armor")
                        .then(Commands.literal("hand")
                                .executes(BasCommand::armorHand))
                        .then(Commands.literal("set")
                                .then(Commands.argument("等级", IntegerArgumentType.integer(1, 6))
                                        .executes(BasCommand::armorSetHand)))
                        .then(Commands.literal("setId")
                                .then(Commands.argument("护甲ID", StringArgumentType.string())
                                        .then(Commands.argument("等级", IntegerArgumentType.integer(1, 6))
                                                .executes(BasCommand::armorSetId))))
                        .then(Commands.literal("get")
                                .then(Commands.argument("护甲ID", StringArgumentType.string())
                                        .executes(BasCommand::armorGet)))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("护甲ID", StringArgumentType.string())
                                        .executes(BasCommand::armorRemove)))
                        .then(Commands.literal("list")
                                .executes(BasCommand::armorList))
                        .then(Commands.literal("clear")
                                .executes(BasCommand::armorClear))
                        .then(Commands.literal("reset")
                                .executes(BasCommand::armorReset))
                        .then(Commands.literal("lock")
                                .then(Commands.argument("护甲ID", StringArgumentType.string())
                                        .executes(BasCommand::armorLock)))
                        .then(Commands.literal("unlock")
                                .then(Commands.argument("护甲ID", StringArgumentType.string())
                                        .executes(BasCommand::armorUnlock))))

                // ==================== 弹药命令 ====================
                .then(Commands.literal("ammo")
                        .then(Commands.literal("hand")
                                .executes(BasCommand::ammoHand))
                        .then(Commands.literal("set")
                                .then(Commands.argument("等级", IntegerArgumentType.integer(1, 6))
                                        .executes(BasCommand::ammoSetHand)))
                        .then(Commands.literal("setId")
                                .then(Commands.argument("弹药ID", StringArgumentType.string())
                                        .then(Commands.argument("等级", IntegerArgumentType.integer(1, 6))
                                                .executes(BasCommand::ammoSetId))))
                        .then(Commands.literal("get")
                                .then(Commands.argument("弹药ID", StringArgumentType.string())
                                        .executes(BasCommand::ammoGet)))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("弹药ID", StringArgumentType.string())
                                        .executes(BasCommand::ammoRemove)))
                        .then(Commands.literal("list")
                                .executes(BasCommand::ammoList))
                        .then(Commands.literal("clear")
                                .executes(BasCommand::ammoClear))
                        .then(Commands.literal("reset")
                                .executes(BasCommand::ammoReset))
                        .then(Commands.literal("lock")
                                .then(Commands.argument("弹药ID", StringArgumentType.string())
                                        .executes(BasCommand::ammoLock)))
                        .then(Commands.literal("unlock")
                                .then(Commands.argument("弹药ID", StringArgumentType.string())
                                        .executes(BasCommand::ammoUnlock))))

                // ==================== 武器命令 ====================
                .then(Commands.literal("weapon")
                        .then(Commands.literal("hand")
                                .executes(BasCommand::weaponHand))
                        .then(Commands.literal("set")
                                .then(Commands.argument("等级", IntegerArgumentType.integer(1, 6))
                                        .executes(BasCommand::weaponSetHand)))
                        .then(Commands.literal("setId")
                                .then(Commands.argument("武器ID", StringArgumentType.string())
                                        .then(Commands.argument("等级", IntegerArgumentType.integer(1, 6))
                                                .executes(BasCommand::weaponSetId))))
                        .then(Commands.literal("get")
                                .then(Commands.argument("武器ID", StringArgumentType.string())
                                        .executes(BasCommand::weaponGet)))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("武器ID", StringArgumentType.string())
                                        .executes(BasCommand::weaponRemove)))
                        .then(Commands.literal("list")
                                .executes(BasCommand::weaponList))
                        .then(Commands.literal("clear")
                                .executes(BasCommand::weaponClear))
                        .then(Commands.literal("reset")
                                .executes(BasCommand::weaponReset))
                        .then(Commands.literal("lock")
                                .then(Commands.argument("武器ID", StringArgumentType.string())
                                        .executes(BasCommand::weaponLock)))
                        .then(Commands.literal("unlock")
                                .then(Commands.argument("武器ID", StringArgumentType.string())
                                        .executes(BasCommand::weaponUnlock))))

                // ==================== 调试命令 ====================
                .then(Commands.literal("debug")
                        .executes(BasCommand::debugSelf)
                        .then(Commands.argument("目标", EntityArgument.entity())
                                .executes(BasCommand::debugTarget)))

                // ==================== 配置管理 ====================
                .then(Commands.literal("reload")
                        .executes(BasCommand::reloadAll))
                .then(Commands.literal("save")
                        .executes(BasCommand::saveAll))
                .then(Commands.literal("path")
                        .executes(BasCommand::showPaths))
        );
    }

    // ==================== 护甲命令实现 ====================

    private static int armorHand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Player player = ctx.getSource().getPlayerOrException();
        ItemStack handItem = player.getMainHandItem();

        if (handItem.isEmpty()) {
            ctx.getSource().sendFailure(getMessage("armortier.command.hand.empty"));
            return 0;
        }

        String itemId = ForgeRegistries.ITEMS.getKey(handItem.getItem()).toString();
        int tier = ArmorTierManager.getArmorTier(itemId);
        boolean isLocked = ArmorTierManager.isLocked(itemId);

        final int finalTier = tier;
        final boolean finalIsLocked = isLocked;
        final String finalItemId = itemId;
        final String finalItemName = handItem.getDisplayName().getString();

        ctx.getSource().sendSuccess(() -> Component.literal("§6=== 护甲信息 ==="), false);
        ctx.getSource().sendSuccess(() -> Component.literal("§7名称: §e" + finalItemName), false);
        ctx.getSource().sendSuccess(() -> Component.literal("§7ID: §8" + finalItemId), false);

        if (finalTier > 0) {
            ctx.getSource().sendSuccess(() -> Component.literal("§7等级: §aLv" + finalTier + " §7(" + getTierName("armortier", finalTier) + ")" + (finalIsLocked ? " §c[锁定]" : "")), false);
            ctx.getSource().sendSuccess(() -> Component.literal("§7减伤: §a" + (finalTier * 10) + "%"), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal("§7等级: §c未配置"), false);
        }

        ctx.getSource().sendSuccess(() -> Component.literal("§7设置: §e/bas armor set <等级>"), false);
        return 1;
    }

    private static int armorSetHand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Player player = ctx.getSource().getPlayerOrException();
        ItemStack handItem = player.getMainHandItem();
        int tier = IntegerArgumentType.getInteger(ctx, "等级");

        if (handItem.isEmpty()) {
            ctx.getSource().sendFailure(getMessage("armortier.command.hand.empty"));
            return 0;
        }

        String itemId = ForgeRegistries.ITEMS.getKey(handItem.getItem()).toString();
        final int finalTier = tier;
        final String finalItemName = handItem.getDisplayName().getString();
        final String finalTierName = getTierName("armortier", tier);

        if (ArmorTierManager.setArmorTier(itemId, tier)) {
            ctx.getSource().sendSuccess(() -> Component.literal("§a已设置 " + finalItemName + " 为 Lv" + finalTier + " (" + finalTierName + ")"), true);
        } else {
            ctx.getSource().sendFailure(getMessage("armortier.command.set.failed"));
        }
        return 1;
    }

    private static int armorSetId(CommandContext<CommandSourceStack> ctx) {
        String armorId = StringArgumentType.getString(ctx, "护甲ID");
        int tier = IntegerArgumentType.getInteger(ctx, "等级");

        if (ArmorTierManager.setArmorTier(armorId, tier)) {
            ctx.getSource().sendSuccess(() -> Component.literal("§a已设置 " + armorId + " 为 Lv" + tier), true);
        } else {
            ctx.getSource().sendFailure(getMessage("armortier.command.set.failed"));
        }
        return 1;
    }

    private static int armorGet(CommandContext<CommandSourceStack> ctx) {
        String armorId = StringArgumentType.getString(ctx, "护甲ID");
        int tier = ArmorTierManager.getArmorTier(armorId);
        boolean isLocked = ArmorTierManager.isLocked(armorId);

        ctx.getSource().sendSuccess(() -> Component.literal("§7" + armorId + " 等级: " + (tier > 0 ? "§aLv" + tier : "§c未配置") + (isLocked ? " §c[锁定]" : "")), false);
        return 1;
    }

    private static int armorRemove(CommandContext<CommandSourceStack> ctx) {
        String armorId = StringArgumentType.getString(ctx, "护甲ID");

        if (ArmorTierManager.removeArmorTier(armorId)) {
            ctx.getSource().sendSuccess(() -> Component.literal("§a已移除 " + armorId + " 的配置"), true);
        } else {
            ctx.getSource().sendFailure(getMessage("armortier.command.remove.failed"));
        }
        return 1;
    }

    private static int armorList(CommandContext<CommandSourceStack> ctx) {
        var configured = ArmorTierManager.getAllConfigured();

        ctx.getSource().sendSuccess(() -> Component.literal("§6=== 已配置护甲列表 ==="), false);

        if (configured.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("§7暂无配置"), false);
        } else {
            for (var entry : configured.entrySet()) {
                ctx.getSource().sendSuccess(() -> Component.literal("§e" + entry.getKey() + " §7-> §aLv" + entry.getValue()), false);
            }
            ctx.getSource().sendSuccess(() -> Component.literal("§7共 " + configured.size() + " 个配置"), false);
        }
        return 1;
    }

    private static int armorClear(CommandContext<CommandSourceStack> ctx) {
        ArmorTierManager.clearAll();
        ctx.getSource().sendSuccess(() -> Component.literal("§a已清空所有护甲配置"), true);
        return 1;
    }

    private static int armorReset(CommandContext<CommandSourceStack> ctx) {
        ArmorTierManager.resetAll();
        ctx.getSource().sendSuccess(() -> Component.literal("§a已重置所有护甲配置"), true);
        return 1;
    }

    private static int armorLock(CommandContext<CommandSourceStack> ctx) {
        String armorId = StringArgumentType.getString(ctx, "护甲ID");
        ArmorTierManager.lock(armorId);
        ctx.getSource().sendSuccess(() -> Component.literal("§a已锁定 " + armorId), true);
        return 1;
    }

    private static int armorUnlock(CommandContext<CommandSourceStack> ctx) {
        String armorId = StringArgumentType.getString(ctx, "护甲ID");
        ArmorTierManager.unlock(armorId);
        ctx.getSource().sendSuccess(() -> Component.literal("§a已解锁 " + armorId), true);
        return 1;
    }

    // ==================== 弹药命令实现 ====================

    private static int ammoHand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Player player = ctx.getSource().getPlayerOrException();
        ItemStack handItem = player.getMainHandItem();

        if (handItem.isEmpty()) {
            ctx.getSource().sendFailure(getMessage("ammotier.command.hand.empty"));
            return 0;
        }

        String ammoId = BallisticUtils.getAmmoIdFromNBT(handItem);
        String itemId = ForgeRegistries.ITEMS.getKey(handItem.getItem()).toString();
        int tier = 0;

        if (ammoId != null && !ammoId.isEmpty()) {
            tier = AmmoTierManager.getAmmoTier(ammoId);
        }
        if (tier == 0) {
            tier = AmmoTierManager.getAmmoTier(itemId);
        }

        boolean isLocked = AmmoTierManager.isLocked(ammoId != null ? ammoId : itemId);

        final int finalTier = tier;
        final boolean finalIsLocked = isLocked;
        final String finalItemId = itemId;
        final String finalItemName = handItem.getDisplayName().getString();
        final String finalAmmoId = ammoId;

        ctx.getSource().sendSuccess(() -> Component.literal("§6=== 弹药信息 ==="), false);
        ctx.getSource().sendSuccess(() -> Component.literal("§7名称: §e" + finalItemName), false);
        ctx.getSource().sendSuccess(() -> Component.literal("§7ID: §8" + finalItemId), false);
        if (finalAmmoId != null) {
            ctx.getSource().sendSuccess(() -> Component.literal("§7AmmoId: §d" + finalAmmoId), false);
        }

        if (finalTier > 0) {
            float bonus = (finalTier - 1) * 20;
            ctx.getSource().sendSuccess(() -> Component.literal("§7等级: §aLv" + finalTier + " §7(" + getTierName("ammotier", finalTier) + ")" + (finalIsLocked ? " §c[锁定]" : "")), false);
            ctx.getSource().sendSuccess(() -> Component.literal("§7伤害加成: §6+" + (int)bonus + "%"), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal("§7等级: §c未配置"), false);
        }

        ctx.getSource().sendSuccess(() -> Component.literal("§7设置: §e/bas ammo set <等级>"), false);
        return 1;
    }

    private static int ammoSetHand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Player player = ctx.getSource().getPlayerOrException();
        ItemStack handItem = player.getMainHandItem();
        int tier = IntegerArgumentType.getInteger(ctx, "等级");

        if (handItem.isEmpty()) {
            ctx.getSource().sendFailure(getMessage("ammotier.command.hand.empty"));
            return 0;
        }

        String ammoId = BallisticUtils.getAmmoIdFromNBT(handItem);
        String configKey = (ammoId != null && !ammoId.isEmpty()) ? ammoId : ForgeRegistries.ITEMS.getKey(handItem.getItem()).toString();

        final int finalTier = tier;
        final String finalItemName = handItem.getDisplayName().getString();

        if (AmmoTierManager.setAmmoTier(configKey, tier)) {
            ctx.getSource().sendSuccess(() -> Component.literal("§a已设置 " + finalItemName + " 为 Lv" + finalTier), true);
        } else {
            ctx.getSource().sendFailure(getMessage("ammotier.command.set.failed"));
        }
        return 1;
    }

    private static int ammoSetId(CommandContext<CommandSourceStack> ctx) {
        String ammoId = StringArgumentType.getString(ctx, "弹药ID");
        int tier = IntegerArgumentType.getInteger(ctx, "等级");

        if (AmmoTierManager.setAmmoTier(ammoId, tier)) {
            ctx.getSource().sendSuccess(() -> Component.literal("§a已设置 " + ammoId + " 为 Lv" + tier), true);
        } else {
            ctx.getSource().sendFailure(getMessage("ammotier.command.set.failed"));
        }
        return 1;
    }

    private static int ammoGet(CommandContext<CommandSourceStack> ctx) {
        String ammoId = StringArgumentType.getString(ctx, "弹药ID");
        int tier = AmmoTierManager.getAmmoTier(ammoId);
        boolean isLocked = AmmoTierManager.isLocked(ammoId);

        ctx.getSource().sendSuccess(() -> Component.literal("§7" + ammoId + " 等级: " + (tier > 0 ? "§aLv" + tier : "§c未配置") + (isLocked ? " §c[锁定]" : "")), false);
        return 1;
    }

    private static int ammoRemove(CommandContext<CommandSourceStack> ctx) {
        String ammoId = StringArgumentType.getString(ctx, "弹药ID");

        if (AmmoTierManager.removeAmmoTier(ammoId)) {
            ctx.getSource().sendSuccess(() -> Component.literal("§a已移除 " + ammoId + " 的配置"), true);
        } else {
            ctx.getSource().sendFailure(getMessage("ammotier.command.remove.failed"));
        }
        return 1;
    }

    private static int ammoList(CommandContext<CommandSourceStack> ctx) {
        var configured = AmmoTierManager.getAllConfigured();

        ctx.getSource().sendSuccess(() -> Component.literal("§6=== 已配置弹药列表 ==="), false);

        if (configured.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("§7暂无配置"), false);
        } else {
            for (var entry : configured.entrySet()) {
                ctx.getSource().sendSuccess(() -> Component.literal("§e" + entry.getKey() + " §7-> §aLv" + entry.getValue()), false);
            }
            ctx.getSource().sendSuccess(() -> Component.literal("§7共 " + configured.size() + " 个配置"), false);
        }
        return 1;
    }

    private static int ammoClear(CommandContext<CommandSourceStack> ctx) {
        AmmoTierManager.clearAll();
        ctx.getSource().sendSuccess(() -> Component.literal("§a已清空所有弹药配置"), true);
        return 1;
    }

    private static int ammoReset(CommandContext<CommandSourceStack> ctx) {
        AmmoTierManager.resetAll();
        ctx.getSource().sendSuccess(() -> Component.literal("§a已重置所有弹药配置"), true);
        return 1;
    }

    private static int ammoLock(CommandContext<CommandSourceStack> ctx) {
        String ammoId = StringArgumentType.getString(ctx, "弹药ID");
        AmmoTierManager.lock(ammoId);
        ctx.getSource().sendSuccess(() -> Component.literal("§a已锁定 " + ammoId), true);
        return 1;
    }

    private static int ammoUnlock(CommandContext<CommandSourceStack> ctx) {
        String ammoId = StringArgumentType.getString(ctx, "弹药ID");
        AmmoTierManager.unlock(ammoId);
        ctx.getSource().sendSuccess(() -> Component.literal("§a已解锁 " + ammoId), true);
        return 1;
    }

    // ==================== 武器命令实现 ====================

    private static int weaponHand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Player player = ctx.getSource().getPlayerOrException();
        ItemStack handItem = player.getMainHandItem();

        if (handItem.isEmpty()) {
            ctx.getSource().sendFailure(getMessage("weapontier.command.hand.empty"));
            return 0;
        }

        String itemId = ForgeRegistries.ITEMS.getKey(handItem.getItem()).toString();
        int tier = WeaponTierManager.getWeaponTier(itemId);
        boolean isLocked = WeaponTierManager.isLocked(itemId);

        final int finalTier = tier;
        final boolean finalIsLocked = isLocked;
        final String finalItemId = itemId;
        final String finalItemName = handItem.getDisplayName().getString();

        ctx.getSource().sendSuccess(() -> Component.literal("§6=== 武器信息 ==="), false);
        ctx.getSource().sendSuccess(() -> Component.literal("§7名称: §e" + finalItemName), false);
        ctx.getSource().sendSuccess(() -> Component.literal("§7ID: §8" + finalItemId), false);

        if (finalTier > 0) {
            float bonus = (finalTier - 1) * 20;
            ctx.getSource().sendSuccess(() -> Component.literal("§7等级: §aLv" + finalTier + " §7(" + getTierName("weapontier", finalTier) + ")" + (finalIsLocked ? " §c[锁定]" : "")), false);
            ctx.getSource().sendSuccess(() -> Component.literal("§7伤害加成: §6+" + (int)bonus + "%"), false);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal("§7等级: §c未配置"), false);
        }

        ctx.getSource().sendSuccess(() -> Component.literal("§7设置: §e/bas weapon set <等级>"), false);
        return 1;
    }

    private static int weaponSetHand(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        Player player = ctx.getSource().getPlayerOrException();
        ItemStack handItem = player.getMainHandItem();
        int tier = IntegerArgumentType.getInteger(ctx, "等级");

        if (handItem.isEmpty()) {
            ctx.getSource().sendFailure(getMessage("weapontier.command.hand.empty"));
            return 0;
        }

        String itemId = ForgeRegistries.ITEMS.getKey(handItem.getItem()).toString();

        final int finalTier = tier;
        final String finalItemName = handItem.getDisplayName().getString();

        if (WeaponTierManager.setWeaponTier(itemId, tier)) {
            ctx.getSource().sendSuccess(() -> Component.literal("§a已设置 " + finalItemName + " 为 Lv" + finalTier), true);
        } else {
            ctx.getSource().sendFailure(getMessage("weapontier.command.set.failed"));
        }
        return 1;
    }

    private static int weaponSetId(CommandContext<CommandSourceStack> ctx) {
        String weaponId = StringArgumentType.getString(ctx, "武器ID");
        int tier = IntegerArgumentType.getInteger(ctx, "等级");

        if (WeaponTierManager.setWeaponTier(weaponId, tier)) {
            ctx.getSource().sendSuccess(() -> Component.literal("§a已设置 " + weaponId + " 为 Lv" + tier), true);
        } else {
            ctx.getSource().sendFailure(getMessage("weapontier.command.set.failed"));
        }
        return 1;
    }

    private static int weaponGet(CommandContext<CommandSourceStack> ctx) {
        String weaponId = StringArgumentType.getString(ctx, "武器ID");
        int tier = WeaponTierManager.getWeaponTier(weaponId);
        boolean isLocked = WeaponTierManager.isLocked(weaponId);

        ctx.getSource().sendSuccess(() -> Component.literal("§7" + weaponId + " 等级: " + (tier > 0 ? "§aLv" + tier : "§c未配置") + (isLocked ? " §c[锁定]" : "")), false);
        return 1;
    }

    private static int weaponRemove(CommandContext<CommandSourceStack> ctx) {
        String weaponId = StringArgumentType.getString(ctx, "武器ID");

        if (WeaponTierManager.removeWeaponTier(weaponId)) {
            ctx.getSource().sendSuccess(() -> Component.literal("§a已移除 " + weaponId + " 的配置"), true);
        } else {
            ctx.getSource().sendFailure(getMessage("weapontier.command.remove.failed"));
        }
        return 1;
    }

    private static int weaponList(CommandContext<CommandSourceStack> ctx) {
        var configured = WeaponTierManager.getAllConfigured();

        ctx.getSource().sendSuccess(() -> Component.literal("§6=== 已配置武器列表 ==="), false);

        if (configured.isEmpty()) {
            ctx.getSource().sendSuccess(() -> Component.literal("§7暂无配置"), false);
        } else {
            for (var entry : configured.entrySet()) {
                ctx.getSource().sendSuccess(() -> Component.literal("§e" + entry.getKey() + " §7-> §aLv" + entry.getValue()), false);
            }
            ctx.getSource().sendSuccess(() -> Component.literal("§7共 " + configured.size() + " 个配置"), false);
        }
        return 1;
    }

    private static int weaponClear(CommandContext<CommandSourceStack> ctx) {
        WeaponTierManager.clearAll();
        ctx.getSource().sendSuccess(() -> Component.literal("§a已清空所有武器配置"), true);
        return 1;
    }

    private static int weaponReset(CommandContext<CommandSourceStack> ctx) {
        WeaponTierManager.resetAll();
        ctx.getSource().sendSuccess(() -> Component.literal("§a已重置所有武器配置"), true);
        return 1;
    }

    private static int weaponLock(CommandContext<CommandSourceStack> ctx) {
        String weaponId = StringArgumentType.getString(ctx, "武器ID");
        WeaponTierManager.lock(weaponId);
        ctx.getSource().sendSuccess(() -> Component.literal("§a已锁定 " + weaponId), true);
        return 1;
    }

    private static int weaponUnlock(CommandContext<CommandSourceStack> ctx) {
        String weaponId = StringArgumentType.getString(ctx, "武器ID");
        WeaponTierManager.unlock(weaponId);
        ctx.getSource().sendSuccess(() -> Component.literal("§a已解锁 " + weaponId), true);
        return 1;
    }

    // ==================== 调试命令实现 ====================

    private static int debugSelf(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();

        if (!(source.getEntity() instanceof LivingEntity entity)) {
            source.sendFailure(Component.literal("只有实体可以使用此命令"));
            return 0;
        }

        debugEntityArmor(entity, source);
        return 1;
    }

    private static int debugTarget(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        Entity targetEntity = EntityArgument.getEntity(ctx, "目标");

        if (!(targetEntity instanceof LivingEntity target)) {
            source.sendFailure(Component.literal("目标不是生物"));
            return 0;
        }

        debugEntityArmor(target, source);
        return 1;
    }

    private static void debugEntityArmor(LivingEntity entity, CommandSourceStack source) {
        final String entityName = entity.getName().getString();

        StringBuilder result = new StringBuilder();
        result.append("§6§l=== 甲弹系统调试 ===\n");
        result.append("§e目标: §f").append(entityName).append("\n");

        String[] slotNames = {"头部", "胸部", "腿部", "脚部"};
        net.minecraft.world.entity.EquipmentSlot[] slots = {
                net.minecraft.world.entity.EquipmentSlot.HEAD,
                net.minecraft.world.entity.EquipmentSlot.CHEST,
                net.minecraft.world.entity.EquipmentSlot.LEGS,
                net.minecraft.world.entity.EquipmentSlot.FEET
        };

        for (int i = 0; i < slots.length; i++) {
            final int index = i;
            ItemStack armor = entity.getItemBySlot(slots[i]);
            ResourceLocation armorKey = ForgeRegistries.ITEMS.getKey(armor.getItem());
            int armorTier = 0;
            if (armorKey != null) {
                armorTier = ArmorTierManager.getArmorTier(armorKey.toString());
            }
            final int finalArmorTier = armorTier;
            final String finalSlotName = slotNames[index];

            if (finalArmorTier <= 0) {
                result.append(String.format("§e%s: ", finalSlotName));
                result.append("§c未穿戴护甲\n");
            } else {
                float reduction = finalArmorTier * 0.1f;
                result.append(String.format("§e%s: §a等级%d护甲 §7(减伤: %.0f%%)\n", finalSlotName, finalArmorTier, reduction * 100));
            }
        }

        source.sendSuccess(() -> Component.literal(result.toString()), false);
    }

    // ==================== 配置管理实现 ====================

    private static int reloadAll(CommandContext<CommandSourceStack> ctx) {
        boolean armorOk = ArmorTierManager.reloadFromFile();
        boolean ammoOk = AmmoTierManager.reloadFromFile();
        boolean weaponOk = WeaponTierManager.reloadFromFile();

        if (armorOk && ammoOk && weaponOk) {
            ctx.getSource().sendSuccess(() -> Component.literal("§a已重载所有配置"), true);
        } else {
            ctx.getSource().sendSuccess(() -> Component.literal("§e配置重载完成（部分文件可能不存在）"), true);
        }
        return 1;
    }

    private static int saveAll(CommandContext<CommandSourceStack> ctx) {
        ArmorTierManager.saveToFile();
        AmmoTierManager.saveToFile();
        WeaponTierManager.saveToFile();
        ctx.getSource().sendSuccess(() -> Component.literal("§a已保存所有配置"), true);
        return 1;
    }

    private static int showPaths(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal("§6=== 配置文件路径 ==="), false);
        ctx.getSource().sendSuccess(() -> Component.literal("§7护甲: §e" + ArmorTierManager.getConfigPath()), false);
        ctx.getSource().sendSuccess(() -> Component.literal("§7弹药: §e" + AmmoTierManager.getConfigPath()), false);
        ctx.getSource().sendSuccess(() -> Component.literal("§7武器: §e" + WeaponTierManager.getConfigPath()), false);
        return 1;
    }

    // ==================== 帮助命令 ====================

    private static int showHelp(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(() -> Component.literal("§6§l=== 甲弹系统命令帮助 ==="), false);
        ctx.getSource().sendSuccess(() -> Component.literal(""), false);

        ctx.getSource().sendSuccess(() -> Component.literal("§6【护甲命令】"), false);
        ctx.getSource().sendSuccess(() -> Component.literal("  §e/bas armor hand §7- 查看手持护甲信息"), false);
        ctx.getSource().sendSuccess(() -> Component.literal("  §e/bas armor set <等级> §7- 设置手持护甲等级"), false);
        ctx.getSource().sendSuccess(() -> Component.literal("  §e/bas armor setId <护甲ID> <等级> §7- 设置指定护甲"), false);
        ctx.getSource().sendSuccess(() -> Component.literal("  §e/bas armor list §7- 列出所有已配置护甲"), false);
        ctx.getSource().sendSuccess(() -> Component.literal("  §e/bas armor remove <护甲ID> §7- 移除护甲配置"), false);

        ctx.getSource().sendSuccess(() -> Component.literal(""), false);
        ctx.getSource().sendSuccess(() -> Component.literal("§6【弹药命令】"), false);
        ctx.getSource().sendSuccess(() -> Component.literal("  §e/bas ammo hand §7- 查看手持弹药信息"), false);
        ctx.getSource().sendSuccess(() -> Component.literal("  §e/bas ammo set <等级> §7- 设置手持弹药等级"), false);
        ctx.getSource().sendSuccess(() -> Component.literal("  §e/bas ammo setId <弹药ID> <等级> §7- 设置指定弹药"), false);
        ctx.getSource().sendSuccess(() -> Component.literal("  §e/bas ammo list §7- 列出所有已配置弹药"), false);

        ctx.getSource().sendSuccess(() -> Component.literal(""), false);
        ctx.getSource().sendSuccess(() -> Component.literal("§6【武器命令】"), false);
        ctx.getSource().sendSuccess(() -> Component.literal("  §e/bas weapon hand §7- 查看手持武器信息"), false);
        ctx.getSource().sendSuccess(() -> Component.literal("  §e/bas weapon set <等级> §7- 设置手持武器等级"), false);
        ctx.getSource().sendSuccess(() -> Component.literal("  §e/bas weapon setId <武器ID> <等级> §7- 设置指定武器"), false);
        ctx.getSource().sendSuccess(() -> Component.literal("  §e/bas weapon list §7- 列出所有已配置武器"), false);

        ctx.getSource().sendSuccess(() -> Component.literal(""), false);
        ctx.getSource().sendSuccess(() -> Component.literal("§6【调试命令】"), false);
        ctx.getSource().sendSuccess(() -> Component.literal("  §e/bas debug §7- 调试自身护甲状态"), false);
        ctx.getSource().sendSuccess(() -> Component.literal("  §e/bas debug <目标> §7- 调试目标生物护甲"), false);

        ctx.getSource().sendSuccess(() -> Component.literal(""), false);
        ctx.getSource().sendSuccess(() -> Component.literal("§6【配置管理】"), false);
        ctx.getSource().sendSuccess(() -> Component.literal("  §e/bas reload §7- 重载所有配置"), false);
        ctx.getSource().sendSuccess(() -> Component.literal("  §e/bas save §7- 保存所有配置"), false);
        ctx.getSource().sendSuccess(() -> Component.literal("  §e/bas path §7- 显示配置文件路径"), false);

        return 1;
    }
}