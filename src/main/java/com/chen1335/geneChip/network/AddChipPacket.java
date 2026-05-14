package com.chen1335.geneChip.network;

import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.client.GeneChipClient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AddChipPacket(ChipInstance<?> chipInstance) implements CustomPacketPayload {
    public static final Type<AddChipPacket> TYPE = new Type<>(GeneChip.id("add_chip"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AddChipPacket> STREAM_CODEC = StreamCodec.composite(
            ChipInstance.STREAM_CODEC,
            AddChipPacket::chipInstance,
            AddChipPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handler(IPayloadContext context) {
        context.enqueueWork(() -> {
            GeneChipClient.getPlayerChipData().addNewChip(context.player(), chipInstance);
        });
    }
}
