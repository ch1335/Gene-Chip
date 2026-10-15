package com.chen1335.geneChip.chip.chips.tactics;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.attachmentData.PlayerRunTimeData;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;
import com.chen1335.geneChip.client.animation.SpiderClimbAnimation;
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

    /**
     * 爬墙主逻辑，客户端与服务端都会执行（{@code LivingEntity.travel} 的 mixin 是双端的）。
     * <p>
     * 服务端负责物理：垂直速度、坠落距离归零、{@code spiderClimbing} 状态；
     * 动画只能作用于客户端本地玩家，全部转交 {@link SpiderClimbAnimation}，并且这里也先判一次
     * {@code isLocalPlayer()} —— 服务端实体是 {@code ServerPlayer}，直接碰客户端动画 API 会崩。
     */
    public void handleClimb(ChipInstance<SpiderClimb> chipInstance, Player player) {
        PlayerRunTimeData playerRunTimeData = GeneChipAPI.getPlayerRunTimeData(player);

        if (player.horizontalCollision) {
            boolean isMovingUp = player.zza > 0;

            if (player.isLocalPlayer()) {
                SpiderClimbAnimation.onClimbing(player, isMovingUp);
            }

            if (isMovingUp) {
                float climbSpeed = chipInstance.getChip().climbSpeed.getValue(chipInstance.getLvl());
                player.setDeltaMovement(player.getDeltaMovement().x, climbSpeed, player.getDeltaMovement().z);
            }
            playerRunTimeData.spiderClimbing = true;
            player.fallDistance = 0;
        }

        boolean noCollision = player.level().noCollision(player, player.getBoundingBox().inflate(0.1, 0, 0.1));
        if (!noCollision && !player.onGround()) {
            if (playerRunTimeData.spiderClimbing) {
                if (player.isShiftKeyDown()) {
                    player.setDeltaMovement(player.getDeltaMovement().x, 0, player.getDeltaMovement().z);

                    if (player.isLocalPlayer()) {
                        SpiderClimbAnimation.setSpeed(player, 0.0F);
                    }
                } else if (player.zza == 0) {
                    player.setDeltaMovement(player.getDeltaMovement().x, -0.5, player.getDeltaMovement().z);
                    if (player.isLocalPlayer()) {
                        SpiderClimbAnimation.setSpeed(player, 1.0F);
                    }
                }
            }
            player.fallDistance = 0;
        } else if (playerRunTimeData.spiderClimbing) {
            playerRunTimeData.spiderClimbing = false;
            if (player.isLocalPlayer()) {
                SpiderClimbAnimation.stopClimbing(player);
            }
        }

    }

}
