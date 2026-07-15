package com.chen1335.geneChip.chip;

import com.chen1335.geneChip.API.object.RegisterTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashMap;
import java.util.Map;

public class CoolDownInfos implements INBTSerializable<CompoundTag> {
    private final Map<Chip, CooldownTicker> chipCooldowns = new HashMap<>();

    public void tick() {
        chipCooldowns.values().forEach(CooldownTicker::tick);
        chipCooldowns.values().removeIf(CooldownTicker::isFinished);

    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        chipCooldowns.forEach((chip, cooldownTicker) -> {
            RegisterTypes.CHIP.getResourceKey(chip).ifPresent(chipResourceKey -> {
                tag.putInt(chipResourceKey.location().toString(), cooldownTicker.tick);
            });
        });
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        for (String allKey : nbt.getAllKeys()) {
            RegisterTypes.CHIP.getHolder(ResourceLocation.parse(allKey)).ifPresent(chipReference -> {
                chipCooldowns.put(chipReference.value(), new CooldownTicker(nbt.getInt(allKey)));
            });
        }
    }

    public void addCoolDown(Chip chip, int tick) {
        chipCooldowns.put(chip, new CooldownTicker(tick));
    }

    public boolean isCoolDown(Chip chip) {
        return chipCooldowns.containsKey(chip);
    }

    public int getRemainingTicks(Chip chip) {
        CooldownTicker ticker = chipCooldowns.get(chip);
        return ticker == null ? 0 : Math.max(0, ticker.tick);
    }

    public int getTotalTicks(Chip chip) {
        CooldownTicker ticker = chipCooldowns.get(chip);
        return ticker == null ? 0 : Math.max(0, ticker.totalTick);
    }

    public Map<Chip, CooldownTicker> getCooldowns() {
        return Map.copyOf(chipCooldowns);
    }

    public static class CooldownTicker {
        public int tick;
        public final int totalTick;

        public CooldownTicker(int tick) {
            this.tick = tick;
            this.totalTick = tick;
        }

        public void tick() {
            this.tick--;
        }

        public boolean isFinished() {
            return tick <= 0;
        }
    }
}
