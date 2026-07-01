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
        if (LivingEntity.class.cast(this) instanceof Player player && player.isLocalPlayer()) {
            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.SPIDER_CLIMB).ifPresent(chipInstance -> {
                chipInstance.getChip().handleClimb(chipInstance,player);
            });
        }
    }
}
