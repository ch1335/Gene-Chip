package com.chen1335.geneChip;

import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.API.object.GCAttachmentTypes;
import com.chen1335.geneChip.API.object.LootItemConditions;
import com.chen1335.geneChip.chip.chipConfig.ChipConfig;
import com.chen1335.geneChip.config.ClothConfig;
import com.mojang.logging.LogUtils;
import net.mcbbs.uid1525632.hungerreworkedreforged.common.init.HRRAttachmentTypes;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.lang.reflect.Field;
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
        ClothConfig.build(modContainer);
        modEventBus.addListener(this::FMLCommonSetupEvent);
        Field declaredField = HRRAttachmentTypes.class.getDeclaredField("REGISTER");
        declaredField.setAccessible(true);
        DeferredRegister<AttachmentType<?>> register = (DeferredRegister<AttachmentType<?>>) declaredField.get(null);
        register.register(modEventBus);
    }

    public void FMLCommonSetupEvent(FMLCommonSetupEvent setupEvent){
        ChipConfig.load();
    }

    public static ResourceLocation id(String id) {
        return ResourceLocation.fromNamespaceAndPath(MODID, id);
    }
}
