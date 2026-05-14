package com.chen1335.geneChip.network.util;

import com.chen1335.geneChip.API.object.RegisterTypes;
import com.chen1335.geneChip.chip.Chip;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ChipTypeSlot(Chip chip, int index) {
    public static final StreamCodec<RegistryFriendlyByteBuf, ChipTypeSlot> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(RegisterTypes.CHIP_KEY),
            ChipTypeSlot::chip,
            ByteBufCodecs.INT,
            ChipTypeSlot::index,
            ChipTypeSlot::new
    );
}
