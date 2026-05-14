package com.chen1335.geneChip.API.object;

import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.chip.Chip;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class RegisterTypes {
    public static final ResourceKey<Registry<Chip>> CHIP_KEY = ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(GeneChip.MODID, "chip"));
    public static final Registry<Chip> CHIP = new RegistryBuilder<>(CHIP_KEY)
            .sync(true)
            .create();
}
