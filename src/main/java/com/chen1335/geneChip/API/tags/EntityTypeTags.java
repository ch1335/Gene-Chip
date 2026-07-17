package com.chen1335.geneChip.API.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public interface EntityTypeTags {
    TagKey<EntityType<?>> HEAD_SHOT_HUNTER_TARGET = createTag("head_shot_hunter_target");

    TagKey<EntityType<?>> ANIMALS = createTag("animals");

    private static TagKey<EntityType<?>> createTag(String name) {
        return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath("c", name));
    }
}
