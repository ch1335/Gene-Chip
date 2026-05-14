package com.chen1335.geneChip;

import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.API.object.GCAttachmentTypes;
import com.chen1335.geneChip.API.object.LootItemConditions;
import com.chen1335.geneChip.config.ClothConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

@Mod(GeneChip.MODID)
public class GeneChip {
    public static final String MODID = "gene_chip";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final ScriptEngine JS_ENGINE = new ScriptEngineManager().getEngineByName("javascript");

    public GeneChip(IEventBus modEventBus, ModContainer modContainer) {
        GCAttachmentTypes.ATTACHMENT_TYPES.register(modEventBus);
        ChipTypes.CHIPS.register(modEventBus);
        LootItemConditions.LOOT_ITEM_CONDITION_TYPES.register(modEventBus);
        ClothConfig.build(modContainer);
    }

    public static ResourceLocation id(String id) {
        return ResourceLocation.fromNamespaceAndPath(MODID, id);
    }
}
