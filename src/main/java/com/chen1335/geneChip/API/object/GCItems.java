package com.chen1335.geneChip.API.object;

import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.items.GeneEnhancer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GCItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, GeneChip.MODID);

    public static final DeferredHolder<Item, GeneEnhancer> GENE_ENHANCER = ITEMS.register("gene_enhancer", () -> new GeneEnhancer(new Item.Properties().stacksTo(64).rarity(Rarity.EPIC)));
}
