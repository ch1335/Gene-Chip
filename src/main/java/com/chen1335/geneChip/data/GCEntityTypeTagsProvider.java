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
    }
}
