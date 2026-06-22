package com.chen1335.geneChip.mixins.lootr;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.chip.chips.survival.ScrapCollector;
import com.chen1335.geneChip.compat.worldfactor.WorldFactorSynergy;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import noobanidus.mods.lootr.common.api.data.DefaultLootFiller;
import noobanidus.mods.lootr.common.api.data.ILootrInfoProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DefaultLootFiller.class)
public class DefaultLootFillerMixin {
    @Inject(method = "unpackLootTable", at = @At("TAIL"))
    public void onUnpackLootTable(ILootrInfoProvider provider, Player player, Container inventory, CallbackInfo ci) {
        if (player == null || !(inventory instanceof net.minecraft.world.Container container)) return;

        GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.SCRAP_COLLECTOR).ifPresent(chipInstance -> {
            ScrapCollector chip = chipInstance.getChip();
            float chance = chip.extraLootChance.getValue(chipInstance.getLvl());
            // 物资空投联动：概率提升至40%
            if (WorldFactorSynergy.isAirdrop()) {
                chance = 0.4F;
            }
            if (player.getRandom().nextFloat() <= chance) return;

            ServerLevel serverLevel = (ServerLevel) player.level();
            ResourceKey<LootTable> bonusKey = ResourceKey.create(Registries.LOOT_TABLE, ScrapCollector.BONUS_LOOT_TABLE);
            LootTable bonusTable = serverLevel.getServer().reloadableRegistries().getLootTable(bonusKey);

            LootParams params = new LootParams.Builder(serverLevel)
                    .withParameter(LootContextParams.ORIGIN, provider.getInfoVec())
                    .withParameter(LootContextParams.THIS_ENTITY, player)
                    .withLuck(player.getLuck())
                    .create(LootContextParamSets.CHEST);

            bonusTable.fill(container, params, player.getRandom().nextLong());
        });
    }
}
