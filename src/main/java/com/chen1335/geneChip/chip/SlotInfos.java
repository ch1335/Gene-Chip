package com.chen1335.geneChip.chip;

import com.chen1335.geneChip.API.object.RegisterTypes;
import com.chen1335.geneChip.attachmentData.PlayerChipData;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class SlotInfos implements INBTSerializable<CompoundTag> {
    public final Map<String, NonNullList<ChipSlot>> slotsByName = new HashMap<>();
    public final Map<Chip, ChipInstance<?>> currentSlots = new HashMap<>();
    private final PlayerChipData playerChipData;
    private String currentSlotsName = "main";

    public SlotInfos(PlayerChipData playerChipData) {
        this.playerChipData = playerChipData;
    }

    public void bakeCurrent() {
        currentSlots.clear();
        for (ChipSlot chipSlot : slotsByName.getOrDefault(currentSlotsName, NonNullList.create())) {
            chipSlot.instance().ifPresent(chipInstance -> {
                currentSlots.put(chipInstance.getChip(), chipInstance);
            });
        }
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        CompoundTag slotsByNameTag = new CompoundTag();
        slotsByName.forEach((name, slots) -> {
            ListTag listTag = new ListTag();
            for (ChipSlot slot : slots) {
                Optional<ChipInstance<?>> instance = slot.instance();
                if (instance.isPresent()) {
                    CompoundTag unit = new CompoundTag();
                    unit.putInt("index", slot.index());
                    ResourceLocation key = RegisterTypes.CHIP.getKey(instance.get().getChip());
                    if (key != null) {
                        unit.putString("chip", key.toString());
                        listTag.add(unit);
                    }
                }
            }
            slotsByNameTag.put(name, listTag);
        });
        tag.put("slotsByName", slotsByNameTag);
        tag.putString("currentSlotsName", currentSlotsName);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        CompoundTag slotsByNameTag = nbt.getCompound("slotsByName");
        for (String key : slotsByNameTag.getAllKeys()) {
            ListTag list = slotsByNameTag.getList(key, Tag.TAG_COMPOUND);
            slotsByName.put(key, newEmptySlots(playerChipData.maxChipSlots));
            for (Tag tag : list) {
                CompoundTag unit = (CompoundTag) tag;
                if (unit.contains("chip")) {
                    Chip chip = RegisterTypes.CHIP.get(ResourceLocation.parse(unit.getString("chip")));
                    if (chip != null) {
                        slotsByName.get(key).set(unit.getInt("index"), new ChipSlot(Optional.ofNullable(playerChipData.getChipInfos().getChips().getOrDefault(chip.getType(), Map.of()).get(chip)), unit.getInt("index")));
                    }
                }
            }
        }
        currentSlotsName = nbt.getString("currentSlotsName");
        bakeCurrent();
    }

    public NonNullList<ChipSlot> newEmptySlots(int size) {
        NonNullList<ChipSlot> list = NonNullList.create();
        for (int i = 0; i < size; i++) {
            list.add(new ChipSlot(Optional.empty(), i));
        }
        return list;
    }


    public NonNullList<ChipSlot> getSlots() {
        return slotsByName.computeIfAbsent(currentSlotsName, k -> newEmptySlots(playerChipData.maxChipSlots));
    }

    public String getCurrentSlotsName() {
        return currentSlotsName;
    }

    public void setCurrentSlotsName(String currentSlotsName) {
        this.currentSlotsName = currentSlotsName;
    }

    public Map<Chip, ChipInstance<?>> getCurrent() {
        return currentSlots;
    }

    public void tick(Player entity) {
        currentSlots.forEach((chip, chipInstance) -> chip.tick(entity, chipInstance));
    }
}
