package com.chen1335.geneChip.mixins;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.attachmentData.PlayerRunTimeData;
import com.chen1335.geneChip.chip.ChipExperience;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class FoodDataMixins {
    @Unique
    private Player gc$player;

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(Player player, CallbackInfo ci) {
        gc$player = player;
    }

    @Inject(method = "addExhaustion", at = @At("RETURN"))
    private void addExhaustion(float exhaustion, CallbackInfo ci, @Local(argsOnly = true) LocalFloatRef exhaustionRef) {
        if (gc$player != null) {
            GeneChipAPI.getPlayerEquippedChip(gc$player, ChipTypes.ENDURANCE).ifPresent(chipInstance -> {
                JsValueCalculator exhaustionReduce = chipInstance.getChip().exhaustionReduce;
                float value = exhaustionReduce.getValue(chipInstance.getLvl());
                exhaustionRef.set(exhaustionRef.get() * (1 - value));
                // 持久耐力经验：累计饥饿消耗，每 4.0 点（=1 点饥饿）+2
                if (gc$player instanceof ServerPlayer serverPlayer) {
                    PlayerRunTimeData runtimeData = GeneChipAPI.getPlayerRunTimeData(gc$player);
                    runtimeData.enduranceHungerAccum += exhaustionRef.get();
                    int hungerPoints = (int) (runtimeData.enduranceHungerAccum / 4.0F);
                    if (hungerPoints > 0) {
                        runtimeData.enduranceHungerAccum -= hungerPoints * 4.0F;
                        ChipExperience.grant(serverPlayer, ChipTypes.ENDURANCE.get(), "hunger_exp", hungerPoints);
                    }
                }
            });
        }
    }
}
