package com.chen1335.geneChip.common;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.GCAttachmentTypes;
import com.chen1335.geneChip.API.object.GCAttributes;
import com.chen1335.geneChip.API.object.RegisterTypes;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.attachmentData.PlayerChipData;
import com.chen1335.geneChip.command.ChipCommand;
import com.chen1335.geneChip.compat.coldsweat.tempModifiers.GeneChipTempModifier;
import com.chen1335.geneChip.network.*;
import com.momosoftworks.coldsweat.api.event.core.init.DefaultTempModifiersEvent;
import com.momosoftworks.coldsweat.api.event.core.registry.TempModifierRegisterEvent;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.api.util.placement.Matcher;
import com.momosoftworks.coldsweat.api.util.placement.Placement;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.NewRegistryEvent;

@EventBusSubscriber(modid = GeneChip.MODID)
public class EventHandler {
    @SubscribeEvent
    public static void registerRegistries(NewRegistryEvent event) {
        event.register(RegisterTypes.CHIP);
    }

    @SubscribeEvent
    public static void PlayerLoggedInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerChipData data = serverPlayer.getData(GCAttachmentTypes.PLAYER_CHIP_DATA);
            data.syncToClient(serverPlayer);
            CardHudSyncService.sync(serverPlayer);
            data.getSlotInfos().currentSlots.forEach((chip, instance) -> {
                chip.onEquipped(player, instance);
            });
        }
    }


    @SubscribeEvent
    public static void PlayerLoggedOutEvent(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.getData(GCAttachmentTypes.PLAYER_CHIP_DATA).getSlotInfos().currentSlots.forEach((chip, instance) -> {
                chip.onUnEquipped(player, instance);
            });
        }
    }

    @SubscribeEvent
    public static void PlayerTickEvent(PlayerTickEvent.Pre event) {
        event.getEntity().getData(GCAttachmentTypes.PLAYER_CHIP_DATA).tick(event.getEntity());
        GeneChipAPI.getPlayerRunTimeData(event.getEntity()).tick(event.getEntity());
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            CardHudSyncService.tick(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void RegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1.0");
        registrar.playToClient(PlayerChipDataPacket.TYPE, PlayerChipDataPacket.STREAM_CODEC, PlayerChipDataPacket::handler);
        registrar.playToClient(AddChipPacket.TYPE, AddChipPacket.STREAM_CODEC, AddChipPacket::handler);
        registrar.playToServer(PlayerActionPacket.TYPE, PlayerActionPacket.STREAM_CODEC, PlayerActionPacket::handler);
        registrar.playBidirectional(SetSlotChipPacket.TYPE, SetSlotChipPacket.STREAM_CODEC, SetSlotChipPacket::handler);
        registrar.playToClient(ChipSelectPacket.TYPE, ChipSelectPacket.STREAM_CODEC, ChipSelectPacket::handler);
        registrar.playToServer(ChipSelectedPacket.TYPE, ChipSelectedPacket.STREAM_CODEC, ChipSelectedPacket::handler);

        registrar.playBidirectional(AnimationPack.TYPE, AnimationPack.STREAM_CODEC, AnimationPack::handler);

        registrar.playToClient(HeadShotIconPacket.TYPE, HeadShotIconPacket.STREAM_CODEC, HeadShotIconPacket::handler);
        registrar.playToClient(CardHudStatePacket.TYPE, CardHudStatePacket.STREAM_CODEC, CardHudStatePacket::handler);
        registrar.playToClient(CardFeedbackPacket.TYPE, CardFeedbackPacket.STREAM_CODEC, CardFeedbackPacket::handler);
    }

    @SubscribeEvent
    public static void RegisterCommandsEvent(RegisterCommandsEvent event) {
        ChipCommand.register(event.getDispatcher());
    }

    @SubscribeEvent()
    public static void registerTempModifiers(TempModifierRegisterEvent event) {
        event.register(GeneChip.id("chip"), GeneChipTempModifier::new);
    }

    @SubscribeEvent()
    public static void DefaultTempModifiersEvent(DefaultTempModifiersEvent event) {
        event.addModifier(Temperature.Trait.CORE, new GeneChipTempModifier(),
                Placement.LAST.noDuplicates(Matcher.SAME_CLASS));
    }

    @SubscribeEvent
    public static void PlayerCloneEvent(PlayerEvent.Clone event) {
        Player original = event.getOriginal();
        PlayerChipData data = original.getData(GCAttachmentTypes.PLAYER_CHIP_DATA);
        event.getEntity().setData(GCAttachmentTypes.PLAYER_CHIP_DATA, data);
    }

    @SubscribeEvent
    public static void EntityAttributeModificationEvent(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, GCAttributes.MAX_CHIP_SLOT);
    }
}
