package com.chen1335.geneChip;

import com.chen1335.geneChip.API.object.*;
import com.chen1335.geneChip.client.GeneChipClient;
import com.chen1335.geneChip.config.GameplayConfig;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.Environment;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;
import org.slf4j.Logger;

import javax.script.ScriptEngine;
import java.nio.file.Path;

@Mod(GeneChip.MODID)
public class GeneChip {
    public static final String MODID = "gene_chip";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final ScriptEngine JS_ENGINE = new NashornScriptEngineFactory().getScriptEngine("--global-per-engine");

    public static final Path CHIP_CONFIG = FMLPaths.CONFIGDIR.get().resolve("gene_chip").resolve("chip_config.json");
    public static final Path GAMEPLAY_CONFIG = FMLPaths.CONFIGDIR.get().resolve("gene_chip").resolve("gameplay_config.json");

    public GeneChip(IEventBus modEventBus, ModContainer modContainer) throws NoSuchFieldException, IllegalAccessException {
        GCAttachmentTypes.ATTACHMENT_TYPES.register(modEventBus);
        ChipTypes.CHIPS.register(modEventBus);
        GCItems.ITEMS.register(modEventBus);
        LootItemConditions.LOOT_ITEM_CONDITION_TYPES.register(modEventBus);
        GCAttributes.ATTRIBUTE_DEFERRED_REGISTER.register(modEventBus);
        // 全部配置合并为 server 配置：config/gene_chip/server.toml（世界 serverconfig 可覆盖），
        // 由服务端加载并同步客户端，文件修改时通过 ModConfigEvent.Reloading 自动重载。
        GameplayConfig.prepareLegacyMigration();
        modContainer.registerConfig(ModConfig.Type.SERVER, GameplayConfig.SPEC, GameplayConfig.CONFIG_FILE_NAME);
        modEventBus.addListener(GameplayConfig::onConfigLoading);
        modEventBus.addListener(GameplayConfig::onConfigReloading);
        // 客户端专用初始化（Cloth Config 界面、player animation 注册）只在客户端执行；
        // 专用服务器不会加载 ClientBootstrap 引用的客户端类。
        if (Environment.get().getDist().isClient()) {
            com.chen1335.geneChip.client.ClientBootstrap.init(modContainer);
        }
        modEventBus.addListener(this::FMLClientSetupEvent);
    }

    public void FMLClientSetupEvent(FMLClientSetupEvent setupEvent) {
        GeneChipClient.setup();
    }

    public static ResourceLocation id(String id) {
        return ResourceLocation.fromNamespaceAndPath(MODID, id);
    }
}
