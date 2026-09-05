package com.chen1335.geneChip.attachmentData;

import com.chen1335.geneChip.API.object.GCAttributes;
import com.chen1335.geneChip.API.object.RegisterTypes;
import com.chen1335.geneChip.chip.*;
import com.chen1335.geneChip.network.AddChipPacket;
import com.chen1335.geneChip.network.ChipSelectPacket;
import com.chen1335.geneChip.network.PlayerChipDataPacket;
import com.chen1335.geneChip.network.util.ChipTypeSlot;
import io.netty.util.collection.IntObjectMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
    private final List<ChipInstance<?>> pendingChipCandidates = new ArrayList<>();
    private int pendingChipDrawRefreshMask;
    private boolean hasCompletedChipDraw;
    private boolean nextChipDrawRefreshOverride;
    private boolean nextChipDrawRefreshOverrideSet;

    public SlotInfos getSlotInfos() {
        return slotInfos;
    }

    public ChipInfos getChipInfos() {
        return chipInfos;
    }

    public CoolDownInfos getCoolDownInfos() {
        return coolDownInfos;
    }

    /**
     * 覆盖玩家下一次抽卡是否可以刷新。该设置仅消耗一次，优先级高于首抽规则。
     */
    public void setNextChipDrawCanRefresh(boolean canRefresh) {
        nextChipDrawRefreshOverride = canRefresh;
        nextChipDrawRefreshOverrideSet = true;
    }

    public boolean hasPendingChipDraw() {
        return !pendingChipCandidates.isEmpty();
    }

    public List<ChipInstance<?>> getPendingChipCandidates() {
        return List.copyOf(pendingChipCandidates);
    }

    public void startChipDraw(List<ChipInstance<?>> candidates) {
        pendingChipCandidates.clear();
        pendingChipCandidates.addAll(candidates);
        boolean canRefresh = nextChipDrawRefreshOverrideSet
                ? nextChipDrawRefreshOverride
                : !hasCompletedChipDraw;
        pendingChipDrawRefreshMask = canRefresh ? allCandidateRefreshMask() : 0;
        nextChipDrawRefreshOverrideSet = false;
        hasCompletedChipDraw = true;
    }

    public int getPendingChipDrawRefreshMask() {
        return hasAlternativeCandidate() ? pendingChipDrawRefreshMask & allCandidateRefreshMask() : 0;
    }

    public boolean isPendingChipCandidateRefreshable(int candidateIndex) {
        if (candidateIndex < 0 || candidateIndex >= pendingChipCandidates.size()) {
            return false;
        }
        return (getPendingChipDrawRefreshMask() & 1 << candidateIndex) != 0;
    }

    private int allCandidateRefreshMask() {
        return (1 << pendingChipCandidates.size()) - 1;
    }

    private boolean hasAlternativeCandidate() {
        if (!hasPendingChipDraw()) {
            return false;
        }

        for (Chip chip : RegisterTypes.CHIP) {
            boolean owned = chipInfos.getChips().getOrDefault(chip.getType(), Map.of()).containsKey(chip);
            boolean currentlyOffered = pendingChipCandidates.stream()
                    .anyMatch(candidate -> candidate.getChip() == chip);
            if (!owned && !currentlyOffered) {
                return true;
            }
        }
        return false;
    }

    /** 使用新卡牌替换指定候选；每个候选位置的刷新资格只能使用一次。 */
    public boolean refreshPendingChipDraw(int candidateIndex, ChipInstance<?> replacement) {
        if (!isPendingChipCandidateRefreshable(candidateIndex)) {
            return false;
        }

        Chip replacementChip = replacement.getChip();
        if (chipInfos.getChips().getOrDefault(replacementChip.getType(), Map.of()).containsKey(replacementChip)) {
            return false;
        }
        for (int index = 0; index < pendingChipCandidates.size(); index++) {
            if (index != candidateIndex && pendingChipCandidates.get(index).getChip() == replacementChip) {
                return false;
            }
        }
        if (pendingChipCandidates.get(candidateIndex).getChip() == replacementChip) {
            return false;
        }

        pendingChipCandidates.set(candidateIndex, replacement);
        pendingChipDrawRefreshMask &= ~(1 << candidateIndex);
        return true;
    }

    public boolean selectPendingChip(ServerPlayer player, int candidateIndex) {
        if (candidateIndex < 0 || candidateIndex >= pendingChipCandidates.size()) {
            return false;
        }

        ChipInstance<?> selected = pendingChipCandidates.get(candidateIndex);
        if (chipInfos.getChips().getOrDefault(selected.getChip().getType(), Map.of()).containsKey(selected.getChip())) {
            return false;
        }

        pendingChipCandidates.clear();
        pendingChipDrawRefreshMask = 0;
        addNewChip(player, selected);
        return true;
    }

    public void sendPendingChipDraw(ServerPlayer player) {
        if (hasPendingChipDraw()) {
            PacketDistributor.sendToPlayer(player,
                    new ChipSelectPacket(getPendingChipCandidates(), getPendingChipDrawRefreshMask()));
        }
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

        ListTag pendingCandidates = new ListTag();
        for (ChipInstance<?> candidate : pendingChipCandidates) {
            pendingCandidates.add(candidate.serializeNBT(provider));
        }
        tag.put("pendingChipCandidates", pendingCandidates);
        tag.putInt("pendingChipDrawRefreshMask", pendingChipDrawRefreshMask);
        tag.putBoolean("hasCompletedChipDraw", hasCompletedChipDraw);
        tag.putBoolean("nextChipDrawRefreshOverride", nextChipDrawRefreshOverride);
        tag.putBoolean("nextChipDrawRefreshOverrideSet", nextChipDrawRefreshOverrideSet);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        chipInfos.deserializeNBT(provider, tag.getCompound("chipInfos"));
        slotInfos.deserializeNBT(provider, tag.getCompound("slotInfos"));
        coolDownInfos.deserializeNBT(provider, tag.getCompound("coolDownInfos"));

        pendingChipCandidates.clear();
        for (Tag candidateTag : tag.getList("pendingChipCandidates", Tag.TAG_COMPOUND)) {
            ChipInstance<?> candidate = ChipInstance.deserialize(provider, (CompoundTag) candidateTag);
            if (candidate != null) {
                pendingChipCandidates.add(candidate);
            }
        }
        if (tag.contains("pendingChipDrawRefreshMask")) {
            pendingChipDrawRefreshMask = tag.getInt("pendingChipDrawRefreshMask") & allCandidateRefreshMask();
        } else {
            // 兼容旧存档：原来的单次整组刷新资格转换为每张候选各一次。
            pendingChipDrawRefreshMask = tag.getBoolean("pendingChipDrawCanRefresh") ? allCandidateRefreshMask() : 0;
        }
        hasCompletedChipDraw = tag.getBoolean("hasCompletedChipDraw");
        nextChipDrawRefreshOverride = tag.getBoolean("nextChipDrawRefreshOverride");
        nextChipDrawRefreshOverrideSet = tag.getBoolean("nextChipDrawRefreshOverrideSet");
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
