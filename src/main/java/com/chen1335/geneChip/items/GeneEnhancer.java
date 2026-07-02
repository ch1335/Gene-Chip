package com.chen1335.geneChip.items;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.GCAttachmentTypes;
import com.chen1335.geneChip.API.object.RegisterTypes;
import com.chen1335.geneChip.attachmentData.PlayerChipData;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.client.animation.AnimationHandler;
import com.chen1335.geneChip.GeneChip;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class GeneEnhancer extends Item {
    public GeneEnhancer(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 40;
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        super.onStopUsing(stack, entity, count);
        if (entity instanceof Player player && player.isLocalPlayer()) {
            AnimationHandler.playAnimationAndDistribute(player, null);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!level.isClientSide && livingEntity instanceof ServerPlayer serverPlayer) {
            PlayerChipData data = serverPlayer.getData(GCAttachmentTypes.PLAYER_CHIP_DATA);
            List<Chip> pool = new ArrayList<>();

            for (Chip chip : RegisterTypes.CHIP) {
                Map<Chip, ChipInstance<?>> chips = data.getChipInfos().getChips().get(chip.getType());
                if (chips == null || !chips.containsKey(chip)) {
                    pool.add(chip);
                }
            }

            int candidateCount = Math.min(3, pool.size());
            List<ChipInstance<?>> candidates = new ArrayList<>();
            List<Chip> remaining = new ArrayList<>(pool);

            for (int c = 0; c < candidateCount; c++) {
                double totalWeight = 0;
                for (Chip chip : remaining) {
                    totalWeight += chip.getWeight(level);
                }
                if (totalWeight <= 0) {
                    candidates.add(remaining.getFirst().createInstance());
                    remaining.removeFirst();
                    continue;
                }

                double r = level.random.nextDouble() * totalWeight;
                double cumulative = 0;
                Chip selected = remaining.getLast();
                for (Chip chip : remaining) {
                    cumulative += chip.getWeight(level);
                    if (r < cumulative) {
                        selected = chip;
                        break;
                    }
                }
                candidates.add(selected.createInstance());
                remaining.remove(selected);
            }
            if (!candidates.isEmpty()) {
                GeneChipAPI.StartCardSelect(serverPlayer, candidates);
                stack.shrink(1);
            }
        }
        return stack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.isLocalPlayer() && !player.isUsingItem()) {
            AnimationHandler.playAnimationAndDistribute(player, GeneChip.id("gene_enhancer"));
        }
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.CUSTOM;
    }

}
