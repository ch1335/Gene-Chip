package com.chen1335.geneChip.chip;

import com.chen1335.geneChip.API.object.RegisterTypes;
import com.chen1335.geneChip.attachmentData.PlayerChipData;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

@ParametersAreNonnullByDefault
public class SlotInfos implements INBTSerializable<CompoundTag> {
    public final Map<String, IntObjectMap<ChipSlot>> slotsByName = new HashMap<>();
    public final Map<Chip, ChipInstance<?>> currentSlots = new HashMap<>();
    private final PlayerChipData playerChipData;
    private String currentSlotsName = "main";

    public SlotInfos(PlayerChipData playerChipData) {
        this.playerChipData = playerChipData;
    }

    public void bakeCurrent() {
        currentSlots.clear();
        for (ChipSlot chipSlot : slotsByName.getOrDefault(currentSlotsName, new IntObjectHashMap<>()).values()) {
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
            for (ChipSlot slot : slots.values()) {
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
                        slotsByName.get(key).put(unit.getInt("index"), new ChipSlot(Optional.ofNullable(playerChipData.getChipInfos().getChips().getOrDefault(chip.getType(), Map.of()).get(chip)), unit.getInt("index")));
                    }
                }
            }
        }
        currentSlotsName = nbt.getString("currentSlotsName");
        bakeCurrent();
    }

    public IntObjectMap<ChipSlot> newEmptySlots(int size) {
        IntObjectHashMap<ChipSlot> map = new IntObjectHashMap<>();
        for (int i = 0; i < size; i++) {
            map.put(i, new ChipSlot(Optional.empty(), i));
        }
        return map;
    }


    public IntObjectMap<ChipSlot> getSlots() {
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

    public void resizeSlots(Player entity) {
        for (IntObjectMap<ChipSlot> value : slotsByName.values()) {
            int size = value.size();
            int maxChipSlots = playerChipData.maxChipSlots;
            if (size > maxChipSlots) {
                Iterator<IntObjectMap.PrimitiveEntry<ChipSlot>> iterator = value.entries().iterator();
                while (iterator.hasNext()) {
                    IntObjectMap.PrimitiveEntry<ChipSlot> next = iterator.next();
                    if (next.key() >= maxChipSlots) {
                        iterator.remove();
                    }
                }
            }

            for (int i = 0; i < maxChipSlots; i++) {
                value.computeIfAbsent(i, index -> new ChipSlot(Optional.empty(), index));
            }
        }
        bakeCurrent();
    }
}
