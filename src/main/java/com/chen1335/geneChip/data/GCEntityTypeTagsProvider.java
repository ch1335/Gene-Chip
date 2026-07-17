package com.chen1335.geneChip.data;

import com.chen1335.geneChip.API.tags.EntityTypeTags;
import com.chen1335.geneChip.GeneChip;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EntityTypeTagsProvider;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class GCEntityTypeTagsProvider extends EntityTypeTagsProvider {

    public GCEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, provider, GeneChip.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        tag(EntityTypeTags.HEAD_SHOT_HUNTER_TARGET)
                .add(EntityType.ZOMBIE);

        tag(EntityTypeTags.ANIMALS).add(
                // 陆地被动动物
                EntityType.PIG,
                EntityType.COW,
                EntityType.MOOSHROOM,
                EntityType.SHEEP,
                EntityType.CHICKEN,
                EntityType.RABBIT,
                EntityType.CAT,
                EntityType.OCELOT,
                EntityType.WOLF,
                EntityType.FOX,
                EntityType.PANDA,
                EntityType.POLAR_BEAR,
                EntityType.GOAT,
                EntityType.CAMEL,
                EntityType.SNIFFER,
                EntityType.ARMADILLO,
                // 马科
                EntityType.HORSE,
                EntityType.DONKEY,
                EntityType.MULE,
                EntityType.LLAMA,
                EntityType.TRADER_LLAMA,
                EntityType.SKELETON_HORSE,
                EntityType.ZOMBIE_HORSE,
                // 飞行动物
                EntityType.BEE,
                EntityType.PARROT,
                EntityType.BAT,
                EntityType.ALLAY,
                // 水生动物
                EntityType.SQUID,
                EntityType.GLOW_SQUID,
                EntityType.DOLPHIN,
                EntityType.TURTLE,
                EntityType.COD,
                EntityType.SALMON,
                EntityType.PUFFERFISH,
                EntityType.TROPICAL_FISH,
                EntityType.AXOLOTL,
                EntityType.TADPOLE,
                EntityType.FROG,
                // 下界
                EntityType.STRIDER
        );
    }
}
