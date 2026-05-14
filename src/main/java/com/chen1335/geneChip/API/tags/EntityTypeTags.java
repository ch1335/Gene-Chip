package com.chen1335.geneChip.API.tags;

import com.chen1335.geneChip.GeneChip;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public interface EntityTypeTags {
    TagKey<EntityType<?>> HEAD_SHOT_HUNTER_TARGET = createTag("head_shot_hunter_target");

    private static TagKey<EntityType<?>> createTag(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, GeneChip.id(name));
    }
}
