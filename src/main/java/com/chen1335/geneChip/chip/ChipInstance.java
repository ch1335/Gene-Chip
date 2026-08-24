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
            ByteBufCodecs.INT,
            ChipInstance::getExp,
            ByteBufCodecs.INT,
            ChipInstance::getLvl,
            ChipInstance::new
    );

    private T chip;

    private int exp;

    private int lvl;

    public ChipInstance(T chip, int exp, int lvl) {
        this.chip = chip;
        this.exp = Math.max(0, exp);
        this.lvl = Math.max(1, lvl);
    }

    public ChipInstance(T chip, int exp) {
        this(chip, exp, 1);
    }

    public int getLvl() {
        return lvl;
    }

    public T getChip() {
        return chip;
    }

    public int getExp() {
        return exp;
    }

    void setProgress(int lvl, int exp) {
        this.lvl = Math.max(1, lvl);
        this.exp = Math.max(0, exp);
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
        int exp = tag.contains("exp") ? (int) Math.max(0, tag.getDouble("exp")) : 0;
        int lvl = tag.contains("lvl") ? Math.max(1, tag.getInt("lvl")) : 1;
        ChipInstance<?> instance = new ChipInstance<>(chip, exp, lvl);
        ChipProgression.normalize(instance);
        return instance;
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        CompoundTag tag = new CompoundTag();
        RegisterTypes.CHIP.getResourceKey(chip).ifPresent(resourceKey -> tag.putString("chip", resourceKey.location().toString()));
        tag.putInt("data_version", 1);
        tag.putInt("exp", exp);
        tag.putInt("lvl", lvl);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag tag) {
        if (tag.contains("chip")) {
            chip = Cast.cast(RegisterTypes.CHIP.get(ResourceLocation.tryParse(tag.getString("chip"))));
        }
        exp = tag.contains("exp") ? (int) Math.max(0, tag.getDouble("exp")) : 0;
        lvl = tag.contains("lvl") ? Math.max(1, tag.getInt("lvl")) : 1;
        ChipProgression.normalize(this);
    }
}
