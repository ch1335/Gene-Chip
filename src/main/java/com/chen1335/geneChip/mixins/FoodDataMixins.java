package com.chen1335.geneChip.mixins;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.attachmentData.PlayerRunTimeData;
import com.chen1335.geneChip.chip.ChipExperience;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;
import com.chen1335.geneChip.chip.chips.survival.NutrientExtraction;
import com.chen1335.geneChip.compat.worldfactor.WorldFactorSynergy;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.minecraft.server.level.ServerPlayer;
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
            // 养分萃取经验：每次进食 +3
            if (gc$player instanceof ServerPlayer serverPlayer) {
                ChipExperience.grant(serverPlayer, ChipTypes.NUTRIENT_EXTRACTION.get(), 3);
                // 大胃王经验：进食恢复饥饿每 4 点 +3
                int hungerUnits = Math.max(1, foodProperties.nutrition() / 4);
                ChipExperience.grant(serverPlayer, ChipTypes.BIG_EATER.get(), hungerUnits * 3);
            }
            GeneChipAPI.getPlayerEquippedChip(gc$player, ChipTypes.NUTRIENT_EXTRACTION).ifPresent(chipInstance -> {
                NutrientExtraction chip = chipInstance.getChip();
                eat((int) chip.foodAdd.getValue(chipInstance.getLvl()), 0F);
                float healAmount = chip.heal.getValue(chipInstance.getLvl());
                // 饥荒前兆联动：额外恢复生命值+1
                if (WorldFactorSynergy.isSignsOfFamine()) {
                    healAmount += 1;
                }
                gc$player.heal(healAmount);
                // 养分萃取经验：额外恢复实际生效 +2
                if (gc$player instanceof ServerPlayer serverPlayer) {
                    ChipExperience.grant(serverPlayer, ChipTypes.NUTRIENT_EXTRACTION.get(), 2);
                }
                // 进食收益提升时反馈：玩家附近白色烟花粒子 + HUD icon
                if (gc$player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    serverPlayer.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.FIREWORK,
                            gc$player.getX(), gc$player.getY() + 1.0, gc$player.getZ(), 8, 0.4, 0.5, 0.4, 0.04);
                    com.chen1335.geneChip.common.CardHudSyncService.feedback(serverPlayer,
                            com.chen1335.geneChip.network.CardFeedbackPacket.FeedbackType.NUTRIENT_EXTRACTED, 0);
                }
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
                // 持久耐力经验：累计饥饿消耗，每 4.0 点（=1 点饥饿）+2
                if (gc$player instanceof ServerPlayer serverPlayer) {
                    PlayerRunTimeData runtimeData = GeneChipAPI.getPlayerRunTimeData(gc$player);
                    runtimeData.enduranceHungerAccum += exhaustionRef.get();
                    int hungerPoints = (int) (runtimeData.enduranceHungerAccum / 4.0F);
                    if (hungerPoints > 0) {
                        runtimeData.enduranceHungerAccum -= hungerPoints * 4.0F;
                        ChipExperience.grant(serverPlayer, ChipTypes.ENDURANCE.get(), hungerPoints * 2);
                    }
                }
            });
        }
    }
}
