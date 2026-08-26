package com.chen1335.geneChip.common;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.API.object.GCAttachmentTypes;
import com.chen1335.geneChip.API.object.RegisterTypes;
import com.chen1335.geneChip.attachmentData.PlayerChipData;
import com.chen1335.geneChip.attachmentData.PlayerRunTimeData;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.chips.mutation.AdrenalGlandBurst;
import com.chen1335.geneChip.chip.chips.mutation.GrowingFervor;
import com.chen1335.geneChip.network.CardFeedbackPacket;
import com.chen1335.geneChip.network.CardHudStatePacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Set;

public final class CardHudSyncService {
    private static final Set<Chip> HUD_COOLDOWNS = Set.of(
            ChipTypes.SLIDING_TACKLE.get(), ChipTypes.DOUBLE_JUMP.get(), ChipTypes.TACTICAL_ROLL.get(),
            ChipTypes.FLYING_KICK.get(), ChipTypes.ADRENAL_GLAND_BURST.get()
    );

    private CardHudSyncService() {
    }

    public static void tick(ServerPlayer player) {
        PlayerRunTimeData runtimeData = GeneChipAPI.getPlayerRunTimeData(player);
        if (++runtimeData.cardHudSyncTicker >= 5) {
            runtimeData.cardHudSyncTicker = 0;
            sync(player);
        }
    }

    public static void sync(ServerPlayer player) {
        PlayerChipData chipData = player.getData(GCAttachmentTypes.PLAYER_CHIP_DATA);
        PlayerRunTimeData runtimeData = GeneChipAPI.getPlayerRunTimeData(player);
        CompoundTag state = new CompoundTag();
        ListTag cooldowns = new ListTag();

        chipData.getSlotInfos().currentSlots.keySet().stream()
                .filter(HUD_COOLDOWNS::contains)
                .forEach(chip -> {
                    int remaining = chipData.getCoolDownInfos().getRemainingTicks(chip);
                    if (remaining <= 0) return;
                    CompoundTag entry = new CompoundTag();
                    entry.putString("chip", java.util.Objects.requireNonNull(RegisterTypes.CHIP.getKey(chip)).toString());
                    entry.putInt("remaining", remaining);
                    entry.putInt("total", Math.max(remaining, chipData.getCoolDownInfos().getTotalTicks(chip)));
                    cooldowns.add(entry);
                });

        state.put("cooldowns", cooldowns);
        state.putBoolean("canDoubleJump", runtimeData.canDoubleJump);
        state.putBoolean("tacticalRolling", runtimeData.tacticalRolling);
        state.putInt("photosynthesisStacks", runtimeData.photosynthesisStacks);
        state.putInt("photosynthesisTimer", runtimeData.photosynthesisTimer);
        state.putInt("photosynthesisInterval", runtimeData.photosynthesisInterval);
        state.putInt("photosynthesisMaxStacks", runtimeData.photosynthesisMaxStacks);
        state.putBoolean("photosynthesisCharging", runtimeData.photosynthesisCharging);
        state.putInt("counterStormTicks", runtimeData.counterStormTimer);
        float counterStormBonusDamage = GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.COUNTER_STORM)
                .map(instance -> runtimeData.counterStormAccumulatedDamage
                        * instance.getChip().damageReflectRatio.getValue(instance.getLvl()))
                .orElse(0.0F);
        state.putFloat("counterStormDamage", counterStormBonusDamage);
        state.putInt("comboCount", runtimeData.getComboCount());
        state.putInt("comboWindowTicks", runtimeData.comboWindowTicks);
        state.putInt("comboWindowDuration", runtimeData.comboWindowDuration);
        state.putInt("comboFeverTicks", runtimeData.comboFeverTicks);
        state.putBoolean("thickSkinnedActive", runtimeData.thickSkinnedActive);
        state.putBoolean("infectedInZone", runtimeData.infectedInZone);

        var adrenal = GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.ADRENAL_GLAND_BURST);
        state.putBoolean("adrenalEquipped", adrenal.isPresent());
        boolean adrenalReady = adrenal.map(instance -> {
            AdrenalGlandBurst chip = instance.getChip();
            return player.getHealth() <= player.getMaxHealth() * chip.threshold.getValue(instance.getLvl())
                    && !GeneChipAPI.isChipCooldown(player, chip);
        }).orElse(false);
        state.putBoolean("adrenalReady", adrenalReady);

        boolean growingFervorEquipped = GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.GROWING_FERVOR).isPresent();
        state.putBoolean("growingFervorEquipped", growingFervorEquipped);
        state.putInt("growingFervorStage", growingFervorEquipped
                ? GrowingFervor.stageForImmunity(GeneChipAPI.getImmunityValue(player)) : 0);
        PacketDistributor.sendToPlayer(player, new CardHudStatePacket(state));
    }

    public static void feedback(ServerPlayer player, CardFeedbackPacket.FeedbackType type, int value) {
        PacketDistributor.sendToPlayer(player, new CardFeedbackPacket(type, value));
    }
}
