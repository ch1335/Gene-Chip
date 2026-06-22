package com.chen1335.geneChip.mixins;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;
import com.chen1335.geneChip.chip.chips.survival.NutrientExtraction;
import com.chen1335.geneChip.compat.worldfactor.WorldFactorSynergy;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public abstract class FoodDataMixins {
    @Shadow
    public abstract void eat(int foodLevelModifier, float saturationLevelModifier);

    @Unique
    private Player gc$player;

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(Player player, CallbackInfo ci) {
        gc$player = player;
    }

    @Inject(method = "eat(Lnet/minecraft/world/food/FoodProperties;)V", at = @At("RETURN"))
    private void eat(FoodProperties foodProperties, CallbackInfo ci) {
        if (gc$player != null) {
            GeneChipAPI.getPlayerEquippedChip(gc$player, ChipTypes.NUTRIENT_EXTRACTION).ifPresent(chipInstance -> {
                NutrientExtraction chip = chipInstance.getChip();
                eat((int) chip.foodAdd.getValue(chipInstance.getLvl()), 0F);
                float healAmount = chip.heal.getValue(chipInstance.getLvl());
                // 饥荒前兆联动：额外恢复生命值+1
                if (WorldFactorSynergy.isSignsOfFamine()) {
                    healAmount += 1;
                }
                gc$player.heal(healAmount);
            });
        }
    }

    @Inject(method = "addExhaustion", at = @At("RETURN"))
    private void addExhaustion(float exhaustion, CallbackInfo ci, @Local(argsOnly = true) LocalFloatRef exhaustionRef) {
        if (gc$player != null) {
            GeneChipAPI.getPlayerEquippedChip(gc$player, ChipTypes.ENDURANCE).ifPresent(chipInstance -> {
                JsValueCalculator exhaustionReduce = chipInstance.getChip().exhaustionReduce;
                float value = exhaustionReduce.getValue(chipInstance.getLvl());
                exhaustionRef.set(exhaustionRef.get() * (1 - value));
            });
        }
    }
}
