package com.chen1335.geneChip.network;

import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.client.GeneChipClient;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AnimationPack(int entityId, ResourceLocation animationLocation) implements CustomPacketPayload {
    public static final Type<AnimationPack> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(GeneChip.MODID, "animation_pack"));
    public static ResourceLocation EMPTY_ANIMATION = GeneChip.id("empty_animation");

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<ByteBuf, AnimationPack> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            AnimationPack::entityId,
            ResourceLocation.STREAM_CODEC,
            AnimationPack::animationLocation,
            AnimationPack::new
    );


    public void handler(IPayloadContext iPayloadContext) {
        Player player = iPayloadContext.player();
        if (player.isLocalPlayer()) {
            GeneChipClient.handlePlayerAnimation(entityId, animationLocation, iPayloadContext);
        } else {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            PacketDistributor.sendToPlayersTrackingEntity(serverPlayer, new AnimationPack(entityId, animationLocation));
        }

    }
}
