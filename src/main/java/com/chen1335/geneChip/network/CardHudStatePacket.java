package com.chen1335.geneChip.network;

import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.client.ClientCardHudState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CardHudStatePacket(CompoundTag state) implements CustomPacketPayload {
    public static final Type<CardHudStatePacket> TYPE = new Type<>(GeneChip.id("card_hud_state"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CardHudStatePacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, CardHudStatePacket::state, CardHudStatePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handler(IPayloadContext context) {
        context.enqueueWork(() -> ClientCardHudState.applySnapshot(state));
    }
}
