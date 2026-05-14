package com.chen1335.geneChip.API.object;

import com.chen1335.geneChip.GeneChip;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.registries.DeferredRegister;

public class LootItemConditions {
    public static final DeferredRegister<LootItemConditionType> LOOT_ITEM_CONDITION_TYPES = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, GeneChip.MODID);

}
