package com.chen1335.geneChip.network;

import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.attachmentData.PlayerChipData;
import com.chen1335.geneChip.chip.ChipInfos;
import com.chen1335.geneChip.chip.ChipSlot;
import com.chen1335.geneChip.chip.SlotInfos;
import com.chen1335.geneChip.client.GeneChipClient;
import com.chen1335.geneChip.network.util.ChipTypeSlot;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record PlayerChipDataPacket(
        ChipInfos chipInfos,
        int maxChipSlots,
        Map<String, List<ChipTypeSlot>> slots,
        String currentSlotsName) implements CustomPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerChipDataPacket> STREAM_CODEC = StreamCodec.composite(
            ChipInfos.STREAM_CODEC,
            PlayerChipDataPacket::chipInfos,
            ByteBufCodecs.INT,
            PlayerChipDataPacket::maxChipSlots,
            ByteBufCodecs.map(
                    HashMap::new,
                    ByteBufCodecs.STRING_UTF8,
                    ChipTypeSlot.STREAM_CODEC.apply(ByteBufCodecs.list())
            ),
            PlayerChipDataPacket::slots,
            ByteBufCodecs.STRING_UTF8,
            PlayerChipDataPacket::currentSlotsName,
            PlayerChipDataPacket::new
    );

    public static final Type<PlayerChipDataPacket> TYPE = new Type<>(GeneChip.id("player_chip_data"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handler(IPayloadContext context) {
        context.enqueueWork(() -> {
            PlayerChipData playerChipData = GeneChipClient.getPlayerChipData();
            ChipInfos chipInfos1 = playerChipData.getChipInfos();
            SlotInfos slotInfos = playerChipData.getSlotInfos();
            playerChipData.getSlotInfos().setCurrentSlotsName(currentSlotsName);
            playerChipData.maxChipSlots = maxChipSlots;
            chipInfos1.copyFrom(playerChipData, chipInfos);

            Map<String, NonNullList<ChipSlot>> slotsByName = slotInfos.slotsByName;
            slotsByName.clear();
            slots.forEach((name, slots) -> {
                NonNullList<ChipSlot> slots1 = playerChipData.getSlotInfos().newEmptySlots(maxChipSlots);
                for (ChipTypeSlot slot : slots) {
                    slots1.set(slot.index(), new ChipSlot(Optional.ofNullable(chipInfos1.getChips().getOrDefault(slot.chip().getType(), Map.of()).get(slot.chip())), slot.index()));
                }
                slotsByName.put(name, slots1);
            });
            slotInfos.bakeCurrent();
        });
    }
}
