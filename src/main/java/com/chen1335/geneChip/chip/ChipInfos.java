package com.chen1335.geneChip.chip;

import com.chen1335.geneChip.API.object.RegisterTypes;
import com.chen1335.geneChip.attachmentData.PlayerChipData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashMap;
import java.util.Map;

public class ChipInfos implements INBTSerializable<CompoundTag> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ChipInfos> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(
                    HashMap::new,
                    ChipType.STREAM_CODEC,
                    ByteBufCodecs.map(
                            HashMap::new,
                            ByteBufCodecs.registry(RegisterTypes.CHIP_KEY),
                            ChipInstance.STREAM_CODEC
                    )
            ),
            ChipInfos::getChips,
            ChipInfos::new
    );

    private final Map<ChipType, Map<Chip, ChipInstance<?>>> chips;

    public ChipInfos(Map<ChipType, Map<Chip, ChipInstance<?>>> chips) {
        this.chips = chips;
    }

    public ChipInfos() {
        this(new HashMap<>());
    }

    public Map<ChipType, Map<Chip, ChipInstance<?>>> getChips() {
        return chips;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag listTag = new ListTag();
        for (Map<Chip, ChipInstance<?>> value : chips.values()) {
            for (ChipInstance<?> chipInstance : value.values()) {
                listTag.add(chipInstance.serializeNBT(provider));
            }
        }

        tag.put("chips", listTag);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        ListTag chips = tag.getList("chips", Tag.TAG_COMPOUND);
        for (Tag chip : chips) {
            CompoundTag compoundTag = (CompoundTag) chip;
            ChipInstance<?> chipInstance = ChipInstance.deserialize(provider, compoundTag);
            if (chipInstance != null) {
                this.chips.computeIfAbsent(chipInstance.getChip().getType(), k -> new HashMap<>()).put(chipInstance.getChip(), chipInstance);
            }
        }
    }

    public void copyFrom(PlayerChipData playerChipData, ChipInfos chipInfos1) {
        chips.clear();
        chips.putAll(chipInfos1.getChips());
    }

}
