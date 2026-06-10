package com.chen1335.geneChip.mixins.clavis;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.chip.chips.special.LocksmithIntuition;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import it.hurts.shatterbyte.clavis.common.data.Lock;
import it.hurts.shatterbyte.clavis.common.data.LootUtils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LootUtils.class)
public abstract class ClavisLootMixin {

    private static final ResourceLocation BONUS_LOOT_TABLE = GeneChip.id("gameplay/locksmith_intuition_bonus");

    @Inject(method = "unlockWithQuality", at = @At(value = "INVOKE", target = "Lit/hurts/shatterbyte/clavis/common/mixin/LootTableAccessor;invokeShuffleAndSplitItems(Lit/unimi/dsi/fastutil/objects/ObjectArrayList;ILnet/minecraft/util/RandomSource;)V"))
    private static void onUnlockWithQuality(ServerLevel level, ServerPlayer player, BlockPos blockPos, Lock lock, float quality, CallbackInfo ci, @Local LocalRef<ObjectArrayList<ItemStack>> mainList) {
        GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.LOCKSMITH_INTUITION).ifPresent(chipInstance -> {
            LocksmithIntuition chip = chipInstance.getChip();
            float threshold = chip.qualityThreshold.getValue(chipInstance.getLvl());

            if (quality >= threshold) {
                ResourceKey<LootTable> lootTableKey = ResourceKey.create(
                        Registries.LOOT_TABLE,
                        BONUS_LOOT_TABLE
                );
                LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(lootTableKey);

                LootParams params = new LootParams.Builder(level)
                        .withParameter(LootContextParams.THIS_ENTITY, player)
                        .withParameter(LootContextParams.ORIGIN, player.position())
                        .create(LootContextParamSets.GIFT);

                List<ItemStack> loot = lootTable.getRandomItems(params);
                ObjectArrayList<ItemStack> itemStacks = mainList.get();
                itemStacks.addAll(loot);
                mainList.set(itemStacks);
            }
        });
    }
}
