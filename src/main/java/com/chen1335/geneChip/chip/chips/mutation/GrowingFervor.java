package com.chen1335.geneChip.chip.chips.mutation;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;
import net.minecraft.network.chat.Component;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.attachmentData.PlayerRunTimeData;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipExperience;
import com.chen1335.geneChip.common.CardHudSyncService;
import com.chen1335.geneChip.network.CardFeedbackPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.ChipType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class GrowingFervor extends Chip {
    public final JsValueCalculator stage1Exp = expValue("stage1_exp", "6");
    public final JsValueCalculator stage2Exp = expValue("stage2_exp", "10");
    public final JsValueCalculator stage3Exp = expValue("stage3_exp", "14");
    public final JsValueCalculator stage4Exp = expValue("stage4_exp", "18");

    private static final ResourceLocation ATTACK_SPEED = GeneChip.id("growing_fervor_attack_speed");
    private static final ResourceLocation MOVE_SPEED = GeneChip.id("growing_fervor_move_speed");

    public GrowingFervor() {
        super(makeTexture("growing_fervor"));
    }

    @Override
    public boolean updateable() {
        // 效果全部硬编码（没有可配置算法），不参与升级
        return false;
    }

    @Override
    public ChipType getType() {
        return ChipType.MUTATION;
    }

    @Override
    public void onEquipped(Player player, ChipInstance<?> instance) {
        if (player instanceof ServerPlayer) {
            onImmunityValueChanged(player, instance, GeneChipAPI.getImmunityValue(player));
        }
    }

    @Override
    public void onUnEquipped(Player player, ChipInstance<?> instance) {
        AttributeMap attributes = player.getAttributes();
        AttributeInstance attackSpeed = attributes.getInstance(Attributes.ATTACK_SPEED);
        AttributeInstance moveSpeed = attributes.getInstance(Attributes.MOVEMENT_SPEED);
        if (moveSpeed == null || attackSpeed == null) {
            return;
        }
        attackSpeed.removeModifier(ATTACK_SPEED);
        moveSpeed.removeModifier(MOVE_SPEED);
        GeneChipAPI.getPlayerRunTimeData(player).lastGrowingFervorStage = -1;
    }

    @Override
    public void onImmunityValueChanged(Player player, ChipInstance<?> instance, int immunityValue) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        AttributeMap attributes = player.getAttributes();
        AttributeInstance attackSpeed = attributes.getInstance(Attributes.ATTACK_SPEED);
        AttributeInstance moveSpeed = attributes.getInstance(Attributes.MOVEMENT_SPEED);
        if (moveSpeed == null || attackSpeed == null) {
            return;
        }
        attackSpeed.removeModifier(ATTACK_SPEED);
        moveSpeed.removeModifier(MOVE_SPEED);

        if (immunityValue < 25) {
            attackSpeed.addTransientModifier(new AttributeModifier(ATTACK_SPEED, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            moveSpeed.addTransientModifier(new AttributeModifier(MOVE_SPEED, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else if (immunityValue < 50) {
            attackSpeed.addTransientModifier(new AttributeModifier(ATTACK_SPEED, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            moveSpeed.addTransientModifier(new AttributeModifier(MOVE_SPEED, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else if (immunityValue < 75) {
            attackSpeed.addTransientModifier(new AttributeModifier(ATTACK_SPEED, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            moveSpeed.addTransientModifier(new AttributeModifier(MOVE_SPEED, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }

        int stage = stageForImmunity(immunityValue);
        PlayerRunTimeData runtimeData = GeneChipAPI.getPlayerRunTimeData(player);
        int previousStage = runtimeData.lastGrowingFervorStage;
        runtimeData.lastGrowingFervorStage = stage;
        if (previousStage >= 0 && previousStage != stage) {
            ServerLevel level = serverPlayer.serverLevel();
            level.sendParticles(ParticleTypes.FIREWORK, player.getX(), player.getY() + 1.0, player.getZ(),
                    10, 0.45, 0.55, 0.45, 0.04);
            CardHudSyncService.feedback(serverPlayer, CardFeedbackPacket.FeedbackType.GROWING_FERVOR_STAGE, stage);
        }
        CardHudSyncService.sync(serverPlayer);
    }

    public static int stageForImmunity(int immunityValue) {
        if (immunityValue >= 75) return 0;
        if (immunityValue >= 50) return 1;
        if (immunityValue >= 25) return 2;
        return 3;
    }

    @Override
    public void tick(Player player, ChipInstance<?> instance) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        // 愈发狂热经验：按免疫档位每分钟结算（免疫越低给得越多）
        int stage = stageForImmunity(GeneChipAPI.getImmunityValue(player));
        
        ChipExperience.grantPassive(serverPlayer, this, "stage" + (stage + 1) + "_exp");
    }

    @Override
    public Component detailDesc(int lvl) {
        return detailedDesc();
    }
}
