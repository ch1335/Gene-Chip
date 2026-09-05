package com.chen1335.geneChip.network;

import com.chen1335.geneChip.API.object.GCAttachmentTypes;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.attachmentData.PlayerChipData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ChipSelectedPacket(int candidateIndex) implements CustomPacketPayload {
    public static final Type<ChipSelectedPacket> TYPE = new Type<>(GeneChip.id("chip_selected"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChipSelectedPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            ChipSelectedPacket::candidateIndex,
            ChipSelectedPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handler(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                PlayerChipData data = serverPlayer.getData(GCAttachmentTypes.PLAYER_CHIP_DATA);
                if (!data.selectPendingChip(serverPlayer, candidateIndex)) {
                    data.sendPendingChipDraw(serverPlayer);
                }
            }
        });
    }
}
