package com.chen1335.geneChip.lootFunction;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.SimpleMapCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.NotNull;

public class FeedbackFunction implements LootItemFunction {
    public static FeedbackFunction INSTANCE = new FeedbackFunction();
    public static final MapCodec<FeedbackFunction> CODEC = SimpleMapCodec.unit(INSTANCE);
    public static final LootItemFunctionType<FeedbackFunction> FEEDBACK_FUNCTION_TYPE = new LootItemFunctionType<>(CODEC);

    @Override
    public @NotNull LootItemFunctionType<? extends LootItemFunction> getType() {
        return FEEDBACK_FUNCTION_TYPE;
    }

    @Override
    public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
        return itemStack;
    }

    public static LootItemFunction.Builder builder() {
        return () -> INSTANCE;
    }
}
