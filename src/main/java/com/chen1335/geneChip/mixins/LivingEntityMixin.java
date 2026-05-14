package com.chen1335.geneChip.mixins;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.chip.chips.survival.IronLung;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "decreaseAirSupply", at = @At("RETURN"), cancellable = true)
    private void decreaseAirSupply(int currentAir, CallbackInfoReturnable<Integer> cir) {
        if (LivingEntity.class.cast(this) instanceof Player player) {
            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.IRON_LUNG).ifPresent(chipInstance -> {
                IronLung chip = chipInstance.getChip();
                float value = chip.decreaseAirSupplyMul.getValue(chipInstance.getLvl());
                if (cir.getReturnValue() < currentAir && player.getRandom().nextDouble() < value) {
                    cir.setReturnValue(currentAir);
                }
            });
        }
    }
}
