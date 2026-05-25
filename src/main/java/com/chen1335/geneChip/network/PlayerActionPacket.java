package com.chen1335.geneChip.network;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.API.object.GCAttachmentTypes;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.attachmentData.PlayerChipData;
import com.chen1335.geneChip.attachmentData.PlayerRunTimeData;
import com.chen1335.geneChip.chip.chips.tactics.DoubleJump;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record PlayerActionPacket(ActionType action, CompoundTag compoundTag) implements CustomPacketPayload {
    public static final Type<PlayerActionPacket> TYPE = new Type<>(GeneChip.id("player_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerActionPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            playerActionPacket -> playerActionPacket.action().ordinal(),
            ByteBufCodecs.COMPOUND_TAG,
            PlayerActionPacket::compoundTag,
            (actionId, data) -> new PlayerActionPacket(ActionType.values()[actionId], data)
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handler(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (action == ActionType.SLIDING_TACKLE && context.player() instanceof ServerPlayer player) {
                GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.SLIDING_TACKLE).ifPresent(chipInstance -> {
                    PlayerChipData playerChipData = player.getData(GCAttachmentTypes.PLAYER_CHIP_DATA);
                    if (playerChipData.getCoolDownInfos().isCoolDown(chipInstance.getChip())) {
                        return;
                    }

                    if (player.getFoodData().getFoodLevel() < 2) {
                        return;
                    }

                    player.getFoodData().eat(-2, 0);

                    PlayerRunTimeData playerRunTimeData = GeneChipAPI.getPlayerRunTimeData(player);
                    playerRunTimeData.slidingTackleActive = true;
                    playerRunTimeData.slidingTackleTimer = (int) (chipInstance.getChip().slideTime.getValue(chipInstance.getLvl()));

                    playerChipData.addCoolDown(chipInstance.getChip(), (int) (chipInstance.getChip().cooldown.getValue(chipInstance.getLvl()) * 20));
                });
            } else if (action == ActionType.DOUBLE_JUMP && context.player() instanceof ServerPlayer player) {
                GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.DOUBLE_JUMP).ifPresent(chipInstance -> {
                    PlayerChipData playerChipData = player.getData(GCAttachmentTypes.PLAYER_CHIP_DATA);
                    if (playerChipData.getCoolDownInfos().isCoolDown(chipInstance.getChip())) {
                        return;
                    }

                    DoubleJump chip = chipInstance.getChip();
                    float saturationCost = chip.saturationCost.getValue(chipInstance.getLvl());

                    if (player.getFoodData().getFoodLevel() < saturationCost) {
                        return;
                    }

                    // 消耗饱和度
                    player.getFoodData().eat((int) -saturationCost, 0);

                    // 添加“体力透支”效果：缓慢I，持续2秒
                    float duration = chip.exhaustionDuration.getValue(chipInstance.getLvl());
                    player.addEffect(new MobEffectInstance(
                            MobEffects.MOVEMENT_SLOWDOWN,
                            (int) (20 * duration),
                            0,
                            false,
                            false
                    ));
                    int cooldown = (int) (chip.cooldown.getValue(chipInstance.getLvl()) * 20);
                    GeneChipAPI.addChipCooldown(player, ChipTypes.DOUBLE_JUMP.get(), cooldown);
                });
            }
        });
    }


    public enum ActionType {
        SLIDING_TACKLE,//滑铲
        DOUBLE_JUMP//二段跳
    }
}
