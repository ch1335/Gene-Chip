package com.chen1335.geneChip.mixins;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.chip.chips.survival.IronLung;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

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


    @Inject(method = "causeFallDamage", at = @At("HEAD"))
    private void beforeCauseFallDamage(CallbackInfoReturnable<Integer> cir) {
        if (LivingEntity.class.cast(this) instanceof Player player) {
            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.SILENT_WALKER).ifPresent(chipInstance -> {
                this.setSilent(true);
            });
        }
    }

    @Inject(method = "causeFallDamage", at = @At("RETURN"))
    private void afterCauseFallDamage(CallbackInfoReturnable<Integer> cir) {
        if (LivingEntity.class.cast(this) instanceof Player player) {
            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.SILENT_WALKER).ifPresent(chipInstance -> {
                this.setSilent(false);
            });
        }
    }
}
