package com.chen1335.geneChip.common;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.API.object.GCAttachmentTypes;
import com.chen1335.geneChip.API.object.RegisterTypes;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.attachmentData.PlayerChipData;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.ChipSlot;
import com.chen1335.geneChip.client.gui.screen.ChipConfigScreen;
import com.chen1335.geneChip.compat.coldsweat.tempModifiers.GeneChipTempModifier;
import com.chen1335.geneChip.lootConditions.WildHunterCondition;
import com.chen1335.geneChip.network.AddChipPacket;
import com.chen1335.geneChip.network.PlayerActionPacket;
import com.chen1335.geneChip.network.PlayerChipDataPacket;
import com.chen1335.geneChip.network.SetSlotChipPacket;
import com.chen1335.geneChip.network.util.ChipTypeSlot;
import com.momosoftworks.coldsweat.api.event.core.init.DefaultTempModifiersEvent;
import com.momosoftworks.coldsweat.api.event.core.registry.TempModifierRegisterEvent;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.api.util.placement.Matcher;
import com.momosoftworks.coldsweat.api.util.placement.Placement;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.NewRegistryEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            Map<String, List<ChipTypeSlot>> map = new HashMap<>();
            data.getSlotInfos().slotsByName.forEach((name, slots) -> {
                for (ChipSlot slot : slots) {
                    slot.instance().ifPresent(chipInstance -> map.computeIfAbsent(name, k -> new ArrayList<>()).add(new ChipTypeSlot(chipInstance.getChip(), slot.index())));
                }
            });
            PlayerChipDataPacket playerChipDataPacket = new PlayerChipDataPacket(data.getChipInfos(), data.maxChipSlots, map, data.getSlotInfos().getCurrentSlotsName());
            PacketDistributor.sendToPlayer(serverPlayer, playerChipDataPacket);
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


    public static final List<Runnable> LootModifiers = new ArrayList<>();

    @SubscribeEvent
    private static void LootTableLoadEvent(LootTableLoadEvent event) {
        LootModifiers.add(() -> {
            List<LootPoolEntryContainer.Builder<?>> lootItems = new ArrayList<>();
            for (LootPool pool : event.getTable().pools) {
                for (LootPoolEntryContainer entry : pool.entries) {
                    if (entry instanceof LootItem lootItem) {
                        if (lootItem.item.is(ItemTags.MEAT) || lootItem.item.is(Tags.Items.LEATHERS)) {
//                        if (event.getTable().getParamSet() == LootContextParamSets.ENTITY) {
//                            String replace = event.getTable().getLootTableId().toString().replace("entities/", "");
//                            BuiltInRegistries.ENTITY_TYPE.getHolder(ResourceLocation.parse( replace)).ifPresent(entityTypeReference -> {
//
//                            });
//                        }
                            lootItems.add(LootItem.lootTableItem(lootItem.item.value()).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))).when(WildHunterCondition.builder()));
                        }
                    }
                }
            }

            lootItems.forEach(lootPoolEntryContainer -> {
                event.getTable().pools.add(LootPool.lootPool().add(lootPoolEntryContainer).build());
            });
        });
    }

    @SubscribeEvent
    public static void ServerStartedEvent(ServerStartedEvent event) {
        LootModifiers.forEach(Runnable::run);
        LootModifiers.clear();
    }

    @SubscribeEvent
    public static void PlayerTickEvent(PlayerTickEvent.Pre event) {
        event.getEntity().getData(GCAttachmentTypes.PLAYER_CHIP_DATA).tick(event.getEntity());
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            GeneChipAPI.getPlayerRunTimeData(event.getEntity()).tick(serverPlayer);
            GeneChipAPI.onImmunityValueChanged(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void RegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1.0");
        registrar.playToClient(PlayerChipDataPacket.TYPE, PlayerChipDataPacket.STREAM_CODEC, PlayerChipDataPacket::handler);
        registrar.playToClient(AddChipPacket.TYPE, AddChipPacket.STREAM_CODEC, AddChipPacket::handler);
        registrar.playToServer(PlayerActionPacket.TYPE, PlayerActionPacket.STREAM_CODEC, PlayerActionPacket::handler);
        registrar.playBidirectional(SetSlotChipPacket.TYPE, SetSlotChipPacket.STREAM_CODEC, SetSlotChipPacket::handler);
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
    public static void RightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (event.getHand() == InteractionHand.MAIN_HAND && event.getItemStack().is(Items.STICK)) {
            if (player instanceof ServerPlayer serverPlayer) {
                PlayerChipData data = serverPlayer.getData(GCAttachmentTypes.PLAYER_CHIP_DATA);
                for (Chip chip : RegisterTypes.CHIP) {
                    data.addNewChip(player, new ChipInstance<>(chip, 0));
                }
            } else {
                Minecraft instance = Minecraft.getInstance();
                instance.setScreen(new ChipConfigScreen());
            }
        }
    }
}
