package com.chen1335.geneChip.chip.chips.tactics;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.attachmentData.PlayerRunTimeData;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;
import com.chen1335.geneChip.client.animation.AnimationHandler;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;

public class SpiderClimb extends Chip {
    public final JsValueCalculator climbSpeed = new JsValueCalculator("0.3");
    public final JsValueCalculator fallDamageReduction = new JsValueCalculator("0.8");

    public SpiderClimb() {
        super(makeTexture("spider_climb"));
        registerConfigValue("climb_speed", climbSpeed);
        registerConfigValue("fall_damage_reduction", fallDamageReduction);
    }

    @Override
    public ChipType getType() {
        return ChipType.TACTICS;
    }

    public void handleClimb(ChipInstance<SpiderClimb> chipInstance, Player player) {
        PlayerRunTimeData playerRunTimeData = GeneChipAPI.getPlayerRunTimeData(player);
        if (player.horizontalCollision) {
            ModifierLayer<IAnimation> animation = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData((AbstractClientPlayer) player).get(AnimationHandler.ANIMATION_RESOURCE);
            if (!animation.isActive() && !player.onGround()) {
                AnimationHandler.playAnimationAndDistribute(player, GeneChip.id("climb"));
            }
            SpiderClimb chip = chipInstance.getChip();
            float climbSpeed = chip.climbSpeed.getValue(chipInstance.getLvl());
            boolean isMovingUp = player.zza > 0;

            if (isMovingUp) {
                AnimationHandler.getSpeedModifier(player).speed = 1;
                player.setDeltaMovement(player.getDeltaMovement().x, climbSpeed, player.getDeltaMovement().z);
            }
            playerRunTimeData.spiderClimbing = true;
            player.fallDistance = 0;

        }else {

        }

        boolean noCollision = player.level().noCollision(player, player.getBoundingBox().inflate(0.1, 0, 0.1));
        if (!noCollision && !player.onGround()) {
            if (playerRunTimeData.spiderClimbing) {
                if (player.isShiftKeyDown()) {
                    player.setDeltaMovement(player.getDeltaMovement().x, 0, player.getDeltaMovement().z);
                    AnimationHandler.getSpeedModifier(player).speed = 0;
                } else if (player.zza == 0 && !player.onGround()) {
                    player.setDeltaMovement(player.getDeltaMovement().x, -0.5, player.getDeltaMovement().z);
                    AnimationHandler.getSpeedModifier(player).speed = 1;
                }
            }
            player.fallDistance = 0;
        } else if (playerRunTimeData.spiderClimbing) {
            AnimationHandler.getSpeedModifier(player).speed = 1;
            playerRunTimeData.spiderClimbing = false;
            AnimationHandler.playAnimationAndDistribute(player, null);
        }

    }

}
