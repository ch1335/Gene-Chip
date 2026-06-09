package com.chen1335.geneChip.lootConditions;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.chips.survival.WildHunter;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.SimpleMapCodec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class WildHunterCondition implements LootItemCondition {
    public static WildHunterCondition INSTANCE = new WildHunterCondition();
    public static final MapCodec<WildHunterCondition> CODEC = SimpleMapCodec.unit(INSTANCE);


    public static final LootItemConditionType LOOT_CONDITION_TYPE = new LootItemConditionType(CODEC);

    @Override
    public @NotNull LootItemConditionType getType() {
        return LOOT_CONDITION_TYPE;
    }

    @Override
    public boolean test(LootContext context) {
        Entity param = context.getParamOrNull(LootContextParams.ATTACKING_ENTITY);
        if (param instanceof Player player) {
            Optional<ChipInstance<WildHunter>> optional = GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.WILD_HUNTER);
            return optional.isPresent();
        }
        return false;
    }

    public static LootItemCondition.Builder builder() {
        return () -> INSTANCE;
    }
}
