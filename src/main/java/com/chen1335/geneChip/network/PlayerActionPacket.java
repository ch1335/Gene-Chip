package com.chen1335.geneChip.network;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.API.object.GCAttachmentTypes;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.attachmentData.PlayerChipData;
import com.chen1335.geneChip.attachmentData.PlayerRunTimeData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
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
            }
        });
    }


    public enum ActionType {
        SLIDING_TACKLE//滑铲
    }
}
