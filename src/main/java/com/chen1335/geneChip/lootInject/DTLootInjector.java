package com.chen1335.geneChip.lootInject;

import com.chen1335.geneChip.API.tags.EntityTypeTags;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.lootConditions.WildHunterCondition;
import com.chen1335.geneChip.lootFunction.FeedbackFunction;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EventBusSubscriber(modid = GeneChip.MODID)
public class DTLootInjector {
    public static final Set<LootModifier> LOOT_MODIFIERS = new HashSet<>();
    private static final Object2ObjectOpenHashMap<ResourceLocation, LootTable> LOOT_TABLES_BY_RL = new Object2ObjectOpenHashMap<>();

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void TagsUpdatedEvent(TagsUpdatedEvent event) {
        if (event.shouldUpdateStaticData()) {
            onAddModifiers();
            RegistryAccess registryAccess = event.getRegistryAccess();
            for (LootModifier lootModifier : LOOT_MODIFIERS) {
                lootModifier.modify(registryAccess, LOOT_TABLES_BY_RL);
            }
            LOOT_MODIFIERS.clear();
            LOOT_TABLES_BY_RL.clear();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void LootTableLoadEvent(LootTableLoadEvent event) {
        LOOT_TABLES_BY_RL.put(event.getName(), event.getTable());
    }

    public static void addModifier(LootModifier modifier) {
        LOOT_MODIFIERS.add(modifier);
    }

    public static void onAddModifiers() {
        addModifier((registryAccess, lootTableMap) -> {
            BuiltInRegistries.ENTITY_TYPE.forEach(entityType -> {
                if (entityType.is(EntityTypeTags.ANIMALS)) {
                    LootTable lootTable = lootTableMap.get(entityType.getDefaultLootTable().location());

                    List<LootPoolEntryContainer.Builder<?>> lootItems = new ArrayList<>();
                    for (LootPool pool : lootTable.pools) {
                        for (LootPoolEntryContainer entry : pool.entries) {
                            if (entry instanceof LootItem lootItem) {
                                if (lootItem.item.is(ItemTags.MEAT) || lootItem.item.is(Tags.Items.LEATHERS)) {
                                    lootItems.add(LootItem.lootTableItem(lootItem.item.value())
                                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2)))
                                            .apply(FeedbackFunction.builder())
                                            .when(WildHunterCondition.builder()));
                                }
                            }
                        }
                    }

                    lootItems.forEach(lootPoolEntryContainer -> {
                        lootTable.pools.add(LootPool.lootPool().add(lootPoolEntryContainer).build());
                    });
                }
            });

        });
    }
}
