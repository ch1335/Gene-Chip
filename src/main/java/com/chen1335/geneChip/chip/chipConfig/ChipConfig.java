package com.chen1335.geneChip.chip.chipConfig;

import com.chen1335.geneChip.API.object.RegisterTypes;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.chip.Chip;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resources.ResourceKey;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ChipConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void load() {
        Path configPath = GeneChip.CHIP_CONFIG;
        if (!Files.exists(configPath)) {
            save();
            return;
        }

        Map<String, Map<String, String>> configData;
        try {
            String json = Files.readString(configPath);
            configData = GSON.fromJson(json, new TypeToken<Map<String, Map<String, String>>>() {}.getType());
        } catch (IOException e) {
            GeneChip.LOGGER.error("Failed to read chip config", e);
            return;
        }

        if (configData == null) return;

        for (Map.Entry<ResourceKey<Chip>, Chip> entry : RegisterTypes.CHIP.entrySet()) {
            String chipId = entry.getKey().location().getPath();
            Map<String, String> chipConfig = configData.get(chipId);
            if (chipConfig != null) {
                entry.getValue().applyConfig(chipConfig);
            }
        }
    }

    public static void save() {
        Map<String, Map<String, String>> configData = new HashMap<>();

        for (Map.Entry<ResourceKey<Chip>, Chip> entry : RegisterTypes.CHIP.entrySet()) {
            String chipId = entry.getKey().location().getPath();
            Map<String, String> chipConfig = entry.getValue().collectConfig();
            if (!chipConfig.isEmpty()) {
                configData.put(chipId, chipConfig);
            }
        }

        try {
            Path configPath = GeneChip.CHIP_CONFIG;
            Files.createDirectories(configPath.getParent());
            Files.writeString(configPath, GSON.toJson(configData));
        } catch (IOException e) {
            GeneChip.LOGGER.error("Failed to save chip config", e);
        }
    }
}
