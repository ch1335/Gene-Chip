package com.chen1335.geneChip.API.object;

import com.chen1335.geneChip.GeneChip;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GCAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTE_DEFERRED_REGISTER = DeferredRegister.create(Registries.ATTRIBUTE, GeneChip.MODID);

    public static final DeferredHolder<Attribute, Attribute> MAX_CHIP_SLOT = ATTRIBUTE_DEFERRED_REGISTER.register("max_chip_slot", () -> new RangedAttribute("attribute.gene_chip.max_chip_slot", 2, 0, 8).setSentiment(Attribute.Sentiment.NEUTRAL).setSyncable(true));
}
