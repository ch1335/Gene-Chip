package com.chen1335.geneChip.network;

import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.client.ClientCardHudState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CardFeedbackPacket(FeedbackType feedbackType, int value) implements CustomPacketPayload {
    public static final Type<CardFeedbackPacket> TYPE = new Type<>(GeneChip.id("card_feedback"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CardFeedbackPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, packet -> packet.feedbackType.ordinal(),
            ByteBufCodecs.INT, CardFeedbackPacket::value,
            (type, value) -> new CardFeedbackPacket(FeedbackType.values()[type], value)
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handler(IPayloadContext context) {
        context.enqueueWork(() -> ClientCardHudState.onFeedback(feedbackType, value));
    }

    public enum FeedbackType {
        ACTION_ACCEPTED,
        ACTION_REJECTED,
        HEADSHOT,
        AMMO_RECYCLED,
        COUNTER_RELEASED,
        COMBO_PROGRESS,
        COMBO_TRIGGERED,
        ADRENAL_TRIGGERED,
        GROWING_FERVOR_STAGE,
        INFECTED_ITEM_BLOCKED,
        LOCKSMITH_BONUS_LOOT,
        SEWAGE_PURIFIED,
        NUTRIENT_EXTRACTED,
        DAWN_AWAKENING
    }
}
