package com.chen1335.geneChip.mixins;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.attachmentData.PlayerRunTimeData;
import com.chen1335.geneChip.chip.chips.tactics.SpiderClimb;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class SpiderClimbMixin extends Entity {
    public SpiderClimbMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "travel", at = @At("HEAD"))
    private void onTravel(Vec3 travelVector, CallbackInfo ci) {
        if (LivingEntity.class.cast(this) instanceof Player player) {
            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.SPIDER_CLIMB).ifPresent(chipInstance -> {
                PlayerRunTimeData playerRunTimeData = GeneChipAPI.getPlayerRunTimeData(player);
                if (player.horizontalCollision) {
                    SpiderClimb chip = chipInstance.getChip();
                    float climbSpeed = chip.climbSpeed.getValue(chipInstance.getLvl());

                    boolean isMovingUp = player.zza > 0;

                    if (isMovingUp) {
                        player.setDeltaMovement(player.getDeltaMovement().x, climbSpeed, player.getDeltaMovement().z);
                    } else {
                        player.setDeltaMovement(player.getDeltaMovement().x, 0, player.getDeltaMovement().z);
                    }
                    playerRunTimeData.spiderClimbing = true;
                    player.fallDistance = 0;
                } else {
                    playerRunTimeData.spiderClimbing = false;
                }
            });
        }
    }
}
