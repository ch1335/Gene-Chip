package com.chen1335.geneChip.mixins.thirst;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.ChipTypes;
import dev.ghen.thirst.content.purity.WaterPurity;
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
        });
    }
}
