package com.chen1335.geneChip.network;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.RegisterTypes;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.ChipSlot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public record SetSlotChipPacket(Optional<Chip> chipOptional, int slot) implements CustomPacketPayload {
    public static final Type<SetSlotChipPacket> TYPE = new Type<>(GeneChip.id("set_slot_chip"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetSlotChipPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.registry(RegisterTypes.CHIP_KEY)),
            SetSlotChipPacket::chipOptional,
            ByteBufCodecs.INT,
            SetSlotChipPacket::slot,
            SetSlotChipPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handler(IPayloadContext context) {
        Player player = context.player();
        Level level = player.level();
        if (level instanceof ServerLevel) {
            if (chipOptional.isPresent()) {
                ChipInstance<Chip> playerChip = GeneChipAPI.getPlayerChip(player, chipOptional.get());
                if (playerChip != null) {
                    GeneChipAPI.setSlotChip(player, new ChipSlot(Optional.of(playerChip), slot));
                }
            } else {
                GeneChipAPI.setSlotChip(player, new ChipSlot(Optional.empty(), slot));
            }
        }
    }
}
