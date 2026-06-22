package com.chen1335.geneChip.API;

import com.chen1335.geneChip.API.object.GCAttachmentTypes;
import com.chen1335.geneChip.attachmentData.PlayerChipData;
import com.chen1335.geneChip.attachmentData.PlayerRunTimeData;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.ChipSlot;
import com.immunity.util.ImmunityServerUtil;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.util.Cast;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class GeneChipAPI {

    public static int getImmunityValue(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return ImmunityServerUtil.getImmunity(serverPlayer);
        }
        return 100;
    }

    public static void onImmunityValueChanged(Player player) {
        int immunityValue = getImmunityValue(player);
        PlayerChipData data = player.getData(GCAttachmentTypes.PLAYER_CHIP_DATA);
        data.getSlotInfos().currentSlots.forEach((chip, instance) -> {
            chip.onImmunityValueChanged(player, instance, immunityValue);
        });
    }

    public static <T extends Chip> ChipInstance<T> getPlayerChip(Player player, T chip) {
        PlayerChipData data = player.getData(GCAttachmentTypes.PLAYER_CHIP_DATA);
        return Cast.cast(data.getChipInfos().getChips().getOrDefault(chip.getType(), Map.of()).get(chip));
    }

    public static <T extends Chip> ChipInstance<T> getPlayerEquippedChip(Player player, T chip) {
        PlayerChipData data = player.getData(GCAttachmentTypes.PLAYER_CHIP_DATA);
        return Cast.cast(data.getSlotInfos().getCurrent().get(chip));
    }


    public static <T extends Chip> Optional<ChipInstance<T>> getPlayerEquippedChip(Player player, Supplier<T> chip) {
        PlayerChipData data = player.getData(GCAttachmentTypes.PLAYER_CHIP_DATA);
        return Optional.ofNullable(Cast.cast(data.getSlotInfos().getCurrent().get(chip.get())));
    }

    public static PlayerRunTimeData getPlayerRunTimeData(Player player) {
        return player.getData(GCAttachmentTypes.PLAYER_RUN_TIME_DATA);
    }

    public static void addChipCooldown(Player player, Chip chip, int tick) {
        PlayerChipData data = player.getData(GCAttachmentTypes.PLAYER_CHIP_DATA);
        data.addCoolDown(chip, tick);
    }

    public static boolean isChipCooldown(Player player, Chip chip) {
        return player.getData(GCAttachmentTypes.PLAYER_CHIP_DATA).getCoolDownInfos().isCoolDown(chip);
    }

    public static void setSlotChip(Player player, ChipSlot slot) {
        PlayerChipData data = player.getData(GCAttachmentTypes.PLAYER_CHIP_DATA);
        IntObjectMap<ChipSlot> slots = data.getSlotInfos().getSlots();
        Map<Chip, ChipInstance<?>> currentSlots = data.getSlotInfos().currentSlots;
        Optional<ChipInstance<?>> optional = slot.instance();
        if (optional.isPresent()) {
            ChipInstance<?> instance = optional.get();
            if (!currentSlots.containsKey(instance.getChip())) {
                if (slot.index() < slots.size()) {
                    ChipSlot oldSlot = slots.get(slot.index());
                    oldSlot.instance().ifPresent(oldInstance -> oldInstance.getChip().onUnEquipped(player, oldInstance));
                    slots.put(slot.index(), slot);
                    instance.getChip().onEquipped(player, instance);
                }
                data.getSlotInfos().bakeCurrent();
            }
        } else {
            if (slot.index() < slots.size()) {
                ChipSlot oldSlot = slots.get(slot.index());
                oldSlot.instance().ifPresent(oldInstance -> oldInstance.getChip().onUnEquipped(player, oldInstance));
                slots.put(slot.index(), slot);
            }
            data.getSlotInfos().bakeCurrent();
        }
    }
}
