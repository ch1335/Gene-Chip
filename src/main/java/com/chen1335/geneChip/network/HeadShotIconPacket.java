package com.chen1335.geneChip.network;

import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.client.HeadShotIconRenderer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 服务端在爆头猎手触发秒杀时发送，告知客户端在指定世界坐标处生成一个爆头 icon 飘浮特效。
 */
public record HeadShotIconPacket(double x, double y, double z) implements CustomPacketPayload {
    public static final Type<HeadShotIconPacket> TYPE = new Type<>(GeneChip.id("head_shot_icon"));

    public static final StreamCodec<RegistryFriendlyByteBuf, HeadShotIconPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, HeadShotIconPacket::x,
            ByteBufCodecs.DOUBLE, HeadShotIconPacket::y,
            ByteBufCodecs.DOUBLE, HeadShotIconPacket::z,
            HeadShotIconPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handler(IPayloadContext context) {
        context.enqueueWork(() -> HeadShotIconRenderer.add(x, y, z));
    }
}
