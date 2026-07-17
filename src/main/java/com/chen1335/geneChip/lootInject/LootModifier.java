package com.chen1335.geneChip.lootInject;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;

public interface LootModifier {
    void modify(RegistryAccess registryAccess, Object2ObjectOpenHashMap<ResourceLocation, LootTable> lootTableMap);
}
