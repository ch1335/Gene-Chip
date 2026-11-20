package com.chen1335.geneChip.mixins.thirst;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.chip.ChipExperience;
import cn.mlus.thirst.content.purity.WaterPurity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WaterPurity.class)
public class WaterPurityMixin {
    @Inject(method = "givePurityEffects(Lnet/minecraft/world/entity/player/Player;I)Z", at = @At("HEAD"), cancellable = true)
    private static void givePurityEffects(Player player, int purity, CallbackInfoReturnable<Boolean> cir) {
        GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.SEWAGE_PURIFICATION_PACK).ifPresent(chipInstance -> {
            cir.setReturnValue(true);
            // 仅在净化实际生效（脏水被净化）时反馈：玩家附近白色烟花粒子 + HUD icon
            if (purity < 2 && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                // 污水净化包经验：饮用不洁净水被净化 +5
                ChipExperience.grant(serverPlayer, ChipTypes.SEWAGE_PURIFICATION_PACK.get(), 5);
                serverPlayer.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.FIREWORK,
                        player.getX(), player.getY() + 1.0, player.getZ(), 8, 0.4, 0.5, 0.4, 0.04);
                com.chen1335.geneChip.common.CardHudSyncService.feedback(serverPlayer,
                        com.chen1335.geneChip.network.CardFeedbackPacket.FeedbackType.SEWAGE_PURIFIED, 0);
            }
        });
    }
}
