package com.chen1335.geneChip.chip;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public record ChipSlot(Optional<ChipInstance<?>> instance, int index) {
    public static final StreamCodec<RegistryFriendlyByteBuf, ChipSlot> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ChipInstance.STREAM_CODEC),
            ChipSlot::instance,
            ByteBufCodecs.INT,
            ChipSlot::index,
            ChipSlot::new
    );

    public boolean isEmpty() {
        return instance.isEmpty();
    }
}
