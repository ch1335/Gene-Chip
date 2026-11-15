package com.chen1335.geneChip.mixins.immunity;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.ChipTypes;
import com.immunity.ImmunityTags;
import com.immunity.event.ImmunityEventHandler;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 「腐肉耐受」：吃腐坏食物时整段跳过 Immunity 的进食结算。
 * <p>
 * 扣免疫力写在 Immunity 自己的 {@code LivingEntityUseItemEvent.Finish} 监听里
 * （{@code ImmunityEventHandler#onItemUseFinish}，按 {@code immunity:spoiled_food} 标签扣
 * {@code spoiledFoodDrain}，默认 15），和 fiahi 的 {@code ApplySpecialEatEffectEvent}
 * 是两条互不相干的链路，取消后者拦不住它。所以这里直接在 HEAD 掐掉这个监听。
 * <p>
 * 副作用：同一监听里 {@code healthy_food} 的回血也会被跳过，但腐坏食物不会同时是健康食物。
 */
@Mixin(ImmunityEventHandler.class)
public abstract class ImmunityEatMixin {
    @Inject(method = "onItemUseFinish", at = @At("HEAD"), cancellable = true)
    private static void geneChip$skipSpoiledFoodDrain(LivingEntityUseItemEvent.Finish event, CallbackInfo ci) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) return;
        if (!event.getItem().is(ImmunityTags.SPOILED_FOOD)) return;
        GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.ROTTEN_FLESH_TOLERANCE).ifPresent(chip -> ci.cancel());
    }
}
