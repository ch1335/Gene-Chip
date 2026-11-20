package com.chen1335.geneChip.network;

import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.API.object.RegisterTypes;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.client.ClientCardHudState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 芯片升级提示：服务端在 {@link com.chen1335.geneChip.chip.ChipExperience} 检测到等级提升时发送，
 * 客户端在 HUD 上显示一行「<芯片名> Lv.n」。
 */
public record ChipLevelUpPacket(Chip chip, int level) implements CustomPacketPayload {
    public static final Type<ChipLevelUpPacket> TYPE = new Type<>(GeneChip.id("chip_level_up"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChipLevelUpPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(RegisterTypes.CHIP_KEY), ChipLevelUpPacket::chip,
            ByteBufCodecs.INT, ChipLevelUpPacket::level,
            ChipLevelUpPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handler(IPayloadContext context) {
        context.enqueueWork(() -> ClientCardHudState.onChipLevelUp(chip, level));
    }
}
