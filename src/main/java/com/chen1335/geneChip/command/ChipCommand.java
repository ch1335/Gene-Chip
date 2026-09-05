package com.chen1335.geneChip.command;

import com.chen1335.geneChip.API.object.GCAttachmentTypes;
import com.chen1335.geneChip.API.object.RegisterTypes;
import com.chen1335.geneChip.attachmentData.PlayerChipData;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.config.GameplayConfig;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

public class ChipCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("genechip")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("give")
                                .then(Commands.argument("target", EntityArgument.player())
                                        .then(Commands.argument("chip", ResourceKeyArgument.key(RegisterTypes.CHIP_KEY))
                                                .executes(ctx -> give(ctx, 1))
                                                .then(Commands.argument("level", IntegerArgumentType.integer(1))
                                                        .executes(ctx -> give(ctx, IntegerArgumentType.getInteger(ctx, "level")))
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("add_exp")
                                .then(Commands.argument("target", EntityArgument.player())
                                        .then(Commands.argument("chip", ResourceKeyArgument.key(RegisterTypes.CHIP_KEY))
                                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                        .executes(ChipCommand::addExperience)
                                                )
                                        )
                                )
                        )
                        .then(Commands.literal("remove")
                                .then(Commands.argument("target", EntityArgument.player())
                                        .then(Commands.argument("chip", ResourceKeyArgument.key(RegisterTypes.CHIP_KEY))
                                                .executes(ChipCommand::remove)
                                        )
                                )
                        )
                        .then(Commands.literal("remove_all")
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ChipCommand::removeAll)
                                )
                        )
                        .then(Commands.literal("set_next_draw_refresh")
                                .then(Commands.argument("target", EntityArgument.player())
                                        .then(Commands.argument("can_refresh", BoolArgumentType.bool())
                                                .executes(ChipCommand::setNextDrawRefresh)
                                        )
                                )
                        )
        );
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Chip resolveChip(CommandContext<CommandSourceStack> ctx) {
        ResourceKey key = ctx.getArgument("chip", ResourceKey.class);
        Chip chip = RegisterTypes.CHIP.get((ResourceKey<Chip>) key);
        if (chip == null) {
            ctx.getSource().sendFailure(Component.translatable("gene_chip.command.chip_not_found", key.location().toString()));
            return null;
        }
        return chip;
    }

    private static int give(CommandContext<CommandSourceStack> ctx, int level) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        Chip chip = resolveChip(ctx);
        if (chip == null) return 0;
        if (level > GameplayConfig.getMaxLevel()) {
            ctx.getSource().sendFailure(Component.translatable("gene_chip.command.level_too_high", GameplayConfig.getMaxLevel()));
            return 0;
        }
        PlayerChipData data = target.getData(GCAttachmentTypes.PLAYER_CHIP_DATA);
        data.addNewChip(target, new ChipInstance<>(chip, 0, level));

        Component name = chip.getDisplayName();
        ctx.getSource().sendSuccess(() ->
                Component.translatable("gene_chip.command.give.success", target.getDisplayName(), name, level), true);
        return 1;
    }

    private static int addExperience(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        Chip chip = resolveChip(ctx);
        if (chip == null) return 0;
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        if (!target.getData(GCAttachmentTypes.PLAYER_CHIP_DATA).addChipExperience(target, chip, amount)) {
            ctx.getSource().sendFailure(Component.translatable("gene_chip.command.add_exp.not_owned", chip.getDisplayName()));
            return 0;
        }
        ChipInstance<?> instance = target.getData(GCAttachmentTypes.PLAYER_CHIP_DATA)
                .getChipInfos().getChips().get(chip.getType()).get(chip);
        ctx.getSource().sendSuccess(() -> Component.translatable("gene_chip.command.add_exp.success",
                target.getDisplayName(), chip.getDisplayName(), amount, instance.getLvl(), instance.getExp()), true);
        return 1;
    }

    private static int remove(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        Chip chip = resolveChip(ctx);
        if (chip == null) return 0;
        PlayerChipData data = target.getData(GCAttachmentTypes.PLAYER_CHIP_DATA);
        boolean had = data.getChipInfos().getChips()
                .getOrDefault(chip.getType(), Map.of())
                .containsKey(chip);
        if (had) {
            data.removeChip(target, chip);
        }
        Component name = chip.getDisplayName();
        if (had) {
            ctx.getSource().sendSuccess(() ->
                    Component.translatable("gene_chip.command.remove.success", target.getDisplayName(), name), true);
        } else {
            ctx.getSource().sendSuccess(() ->
                    Component.translatable("gene_chip.command.remove.not_owned", target.getDisplayName(), name), false);
        }
        return had ? 1 : 0;
    }

    private static int setNextDrawRefresh(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        boolean canRefresh = BoolArgumentType.getBool(ctx, "can_refresh");
        target.getData(GCAttachmentTypes.PLAYER_CHIP_DATA).setNextChipDrawCanRefresh(canRefresh);
        ctx.getSource().sendSuccess(() -> Component.translatable(
                "gene_chip.command.set_next_draw_refresh.success", target.getDisplayName(), canRefresh), true);
        return 1;
    }

    private static int removeAll(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
        PlayerChipData data = target.getData(GCAttachmentTypes.PLAYER_CHIP_DATA);
        int count = 0;
        for (Map<Chip, ChipInstance<?>> chips : data.getChipInfos().getChips().values()) {
            count += chips.size();
        }
        data.getChipInfos().getChips().clear();
        data.syncToClient(target);
        int finalCount = count;
        ctx.getSource().sendSuccess(() ->
                Component.translatable("gene_chip.command.remove_all.success", target.getDisplayName(), finalCount), true);
        return 1;
    }
}
