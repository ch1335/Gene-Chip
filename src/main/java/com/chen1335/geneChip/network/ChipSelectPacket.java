package com.chen1335.geneChip.network;

import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.client.gui.screen.ChipSelectScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record ChipSelectPacket(List<ChipInstance<?>> candidates, int refreshMask) implements CustomPacketPayload {
    public static final Type<ChipSelectPacket> TYPE = new Type<>(GeneChip.id("chip_select"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChipSelectPacket> STREAM_CODEC = StreamCodec.composite(
            ChipInstance.STREAM_CODEC.apply(ByteBufCodecs.list()),
            ChipSelectPacket::candidates,
            ByteBufCodecs.INT,
            ChipSelectPacket::refreshMask,
            ChipSelectPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handler(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player().isLocalPlayer()) {
                Minecraft.getInstance().setScreen(new ChipSelectScreen(candidates, refreshMask));
            }
        });
    }
}
