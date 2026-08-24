package com.chen1335.geneChip.attachmentData;

import com.chen1335.geneChip.API.object.GCAttributes;
import com.chen1335.geneChip.chip.*;
import com.chen1335.geneChip.network.AddChipPacket;
import com.chen1335.geneChip.network.PlayerChipDataPacket;
import com.chen1335.geneChip.network.util.ChipTypeSlot;
import io.netty.util.collection.IntObjectMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.UnknownNullability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PlayerChipData implements INBTSerializable<CompoundTag> {
    public int maxChipSlots = (int) GCAttributes.MAX_CHIP_SLOT.get().getDefaultValue();

    private final ChipInfos chipInfos = new ChipInfos();

    private final SlotInfos slotInfos = new SlotInfos(this);

    private final CoolDownInfos coolDownInfos = new CoolDownInfos();

    public SlotInfos getSlotInfos() {
        return slotInfos;
    }

    public ChipInfos getChipInfos() {
        return chipInfos;
    }

    public CoolDownInfos getCoolDownInfos() {
        return coolDownInfos;
    }

    public void addNewChip(Player player, ChipInstance<?> instance) {
        Map<Chip, ChipInstance<?>> chips = chipInfos.getChips().computeIfAbsent(instance.getChip().getType(), k -> new HashMap<>());
        if (!chips.containsKey(instance.getChip())) {
            chips.put(instance.getChip(), instance);
            if (player instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new AddChipPacket(instance));
            }
        }
    }

    public void removeChip(Player player, Chip chip) {
        Map<Chip, ChipInstance<?>> chips = chipInfos.getChips().get(chip.getType());
        if (chips == null || !chips.containsKey(chip)) return;
        chips.remove(chip);

        IntObjectMap<ChipSlot> slots = slotInfos.getSlots();
        slots.values().stream().filter(slot -> {
            Optional<ChipInstance<?>> inst = slot.instance();
            return inst.isPresent() && inst.get().getChip() == chip;
        }).forEach(slot -> slots.put(slot.index(), new ChipSlot(Optional.empty(), slot.index())));
        slotInfos.bakeCurrent();

        if (player instanceof ServerPlayer serverPlayer) {
            syncToClient(serverPlayer);
        }
    }

    public boolean addChipExperience(ServerPlayer player, Chip chip, int amount) {
        ChipInstance<?> instance = chipInfos.getChips().getOrDefault(chip.getType(), Map.of()).get(chip);
        if (instance == null || amount <= 0) return false;
        ChipProgression.addExperience(instance, amount);
        syncToClient(player);
        return true;
    }

    public void syncToClient(ServerPlayer serverPlayer) {
        Map<String, List<ChipTypeSlot>> map = new HashMap<>();
        slotInfos.slotsByName.forEach((name, slots) -> {
            List<ChipTypeSlot> list = new ArrayList<>();
            for (ChipSlot slot : slots.values()) {
                slot.instance().ifPresent(chipInstance -> list.add(new ChipTypeSlot(chipInstance.getChip(), slot.index())));
            }
            map.put(name, list);
        });
        PacketDistributor.sendToPlayer(serverPlayer,
                new PlayerChipDataPacket(chipInfos, maxChipSlots, map, slotInfos.getCurrentSlotsName()));
    }

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        tag.put("chipInfos", chipInfos.serializeNBT(provider));
        tag.put("slotInfos", slotInfos.serializeNBT(provider));
        tag.put("coolDownInfos", coolDownInfos.serializeNBT(provider));
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        chipInfos.deserializeNBT(provider, tag.getCompound("chipInfos"));
        slotInfos.deserializeNBT(provider, tag.getCompound("slotInfos"));
        coolDownInfos.deserializeNBT(provider, tag.getCompound("coolDownInfos"));
    }

    public void tick(Player entity) {
        coolDownInfos.tick();
        slotInfos.tick(entity);
        int value = (int) entity.getAttributeValue(GCAttributes.MAX_CHIP_SLOT);
        if (maxChipSlots != value) {
            maxChipSlots = value;
            resizeSlots(entity);
        }

    }

    public void resizeSlots(Player entity) {
        slotInfos.resizeSlots(entity);
    }


    public void addCoolDown(Chip chip, int tick) {
        coolDownInfos.addCoolDown(chip, tick);
    }
}
