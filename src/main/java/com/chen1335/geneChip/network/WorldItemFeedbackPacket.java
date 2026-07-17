package com.chen1335.geneChip.network;

import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.client.WorldItemFeedbackRenderer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 服务端在卡牌产生额外掉落 / 战利品时发送，告知客户端在指定世界坐标处生成一个物品 icon 飘浮特效。
 * 用于野外猎手（肉 / 皮革）与废品收集者（额外战利品）等绑定世界对象位置的反馈。
 */
public record WorldItemFeedbackPacket(double x, double y, double z, ItemStack stack) implements CustomPacketPayload {
    public static final Type<WorldItemFeedbackPacket> TYPE = new Type<>(GeneChip.id("world_item_feedback"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WorldItemFeedbackPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, WorldItemFeedbackPacket::x,
            ByteBufCodecs.DOUBLE, WorldItemFeedbackPacket::y,
            ByteBufCodecs.DOUBLE, WorldItemFeedbackPacket::z,
            ItemStack.STREAM_CODEC, WorldItemFeedbackPacket::stack,
            WorldItemFeedbackPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handler(IPayloadContext context) {
        context.enqueueWork(() -> WorldItemFeedbackRenderer.add(x, y, z, stack));
    }
}
