package com.chen1335.geneChip.client;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.chip.chips.tactics.SlidingTackle;
import com.chen1335.geneChip.network.PlayerActionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = GeneChip.MODID, value = Dist.CLIENT)
public class ClientEventHandler {
    @SubscribeEvent
    public static void ClientTickEvent(ClientTickEvent.Pre event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.SLIDING_TACKLE).ifPresent(chipInstance -> {
            boolean isSprinting = player.isSprinting();
            boolean isSneaking = player.isShiftKeyDown();

            if (isSprinting && isSneaking && !GeneChipAPI.isChipCooldown(player, ChipTypes.SLIDING_TACKLE.get())) {
                if (player.getFoodData().getFoodLevel() >= 2) {
                    SlidingTackle chip = chipInstance.getChip();
                    float slideDistance = chip.slideDistance.getValue(chipInstance.getLvl());
                    player.setDeltaMovement(player.getViewVector(0).scale(slideDistance));

                    PacketDistributor.sendToServer(new PlayerActionPacket(PlayerActionPacket.ActionType.SLIDING_TACKLE, new CompoundTag()));

                    int cooldown = (int) (chip.cooldown.getValue(chipInstance.getLvl()) * 20);
                    GeneChipAPI.addChipCooldown(player, ChipTypes.SLIDING_TACKLE.get(), cooldown);
                }
            }
        });
    }
}
