package com.chen1335.geneChip.chip;

import com.chen1335.geneChip.API.object.RegisterTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.apache.logging.log4j.util.Cast;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

public class ChipInstance<T extends Chip> implements INBTSerializable<CompoundTag> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ChipInstance<?>> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(RegisterTypes.CHIP_KEY),
            ChipInstance::getChip,
            ByteBufCodecs.FLOAT,
            ChipInstance::getExp,
            ByteBufCodecs.INT,
            ChipInstance::getLvl,
            ChipInstance::new
    );

    private T chip;

    private float exp;

    private int lvl;

    public ChipInstance(T chip, float exp, int lvl) {
        this.chip = chip;
        this.exp = exp;
        this.lvl = lvl;
    }

    public ChipInstance(T chip, float exp) {
        this(chip, exp, 1);
    }

    public int getLvl() {
        return lvl;
    }

    public T getChip() {
        return chip;
    }

    public float getExp() {
        return exp;
    }

    @Nullable
    public static ChipInstance<?> deserialize(HolderLookup.Provider provider, CompoundTag tag) {
        Chip chip = null;
        if (tag.contains("chip")) {
            chip = RegisterTypes.CHIP.get(ResourceLocation.tryParse(tag.getString("chip")));
        }
        if (chip == null) {
            return null;
        }
        float exp = tag.getFloat("exp");
        return new ChipInstance<>(chip, exp);
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        CompoundTag tag = new CompoundTag();
        RegisterTypes.CHIP.getResourceKey(chip).ifPresent(resourceKey -> tag.putString("chip", resourceKey.location().toString()));
        tag.putFloat("exp", exp);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag tag) {
        if (tag.contains("chip")) {
            chip = Cast.cast(RegisterTypes.CHIP.get(ResourceLocation.tryParse(tag.getString("chip"))));
        }
        exp = tag.getFloat("exp");
    }
}
