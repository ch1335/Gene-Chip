package com.chen1335.geneChip.network;

import com.chen1335.geneChip.API.object.GCAttachmentTypes;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.attachmentData.PlayerChipData;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.items.GeneEnhancer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** 客户端请求刷新当前待选择抽卡中的一个候选。 */
public record ChipRefreshPacket(int candidateIndex) implements CustomPacketPayload {
    public static final Type<ChipRefreshPacket> TYPE = new Type<>(GeneChip.id("chip_refresh"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChipRefreshPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,
            ChipRefreshPacket::candidateIndex,
            ChipRefreshPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handler(IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                PlayerChipData data = serverPlayer.getData(GCAttachmentTypes.PLAYER_CHIP_DATA);
                if (!data.isPendingChipCandidateRefreshable(candidateIndex)) {
                    data.sendPendingChipDraw(serverPlayer);
                    return;
                }

                Set<Chip> excludedChips = data.getPendingChipCandidates().stream()
                        .map(ChipInstance::getChip)
                        .collect(Collectors.toSet());
                List<ChipInstance<?>> candidates = GeneEnhancer.createChipDrawCandidates(serverPlayer, excludedChips, 1);
                if (candidates.isEmpty()
                        || !data.refreshPendingChipDraw(candidateIndex, candidates.getFirst())) {
                    data.sendPendingChipDraw(serverPlayer);
                    return;
                }
                data.sendPendingChipDraw(serverPlayer);
            }
        });
    }
}
