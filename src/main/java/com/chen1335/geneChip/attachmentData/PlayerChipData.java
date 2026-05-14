package com.chen1335.geneChip.attachmentData;

import com.chen1335.geneChip.chip.*;
import com.chen1335.geneChip.network.AddChipPacket;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashMap;
import java.util.Map;

public class PlayerChipData implements INBTSerializable<CompoundTag> {
    public int maxChipSlots = 2;

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
    }

    public void addCoolDown(Chip chip, int tick) {
        coolDownInfos.addCoolDown(chip, tick);
    }
}
