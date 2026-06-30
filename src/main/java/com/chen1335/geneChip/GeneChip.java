package com.chen1335.geneChip;

import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.API.object.GCAttachmentTypes;
import com.chen1335.geneChip.API.object.GCAttributes;
import com.chen1335.geneChip.API.object.LootItemConditions;
import com.chen1335.geneChip.chip.chipConfig.ChipConfig;
import com.chen1335.geneChip.client.GeneChipClient;
import com.chen1335.geneChip.config.ClothConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.nio.file.Path;

@Mod(GeneChip.MODID)
public class GeneChip {
    public static final String MODID = "gene_chip";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final ScriptEngine JS_ENGINE = new ScriptEngineManager().getEngineByName("javascript");

    public static final Path CHIP_CONFIG = FMLPaths.CONFIGDIR.get().resolve("gene_chip").resolve("chip_config.json");

    public GeneChip(IEventBus modEventBus, ModContainer modContainer) throws NoSuchFieldException, IllegalAccessException {
        GCAttachmentTypes.ATTACHMENT_TYPES.register(modEventBus);
        ChipTypes.CHIPS.register(modEventBus);
        LootItemConditions.LOOT_ITEM_CONDITION_TYPES.register(modEventBus);
        GCAttributes.ATTRIBUTE_DEFERRED_REGISTER.register(modEventBus);
        ClothConfig.build(modContainer);
        modEventBus.addListener(this::FMLCommonSetupEvent);
        modEventBus.addListener(this::FMLClientSetupEvent);
        GeneChipClient.init();
    }

    public void FMLCommonSetupEvent(FMLCommonSetupEvent setupEvent) {
        ChipConfig.load();
    }

    public void FMLClientSetupEvent(FMLClientSetupEvent setupEvent) {
        GeneChipClient.setup();
    }

    public static ResourceLocation id(String id) {
        return ResourceLocation.fromNamespaceAndPath(MODID, id);
    }
}
