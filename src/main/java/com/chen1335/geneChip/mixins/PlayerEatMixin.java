package com.chen1335.geneChip.mixins;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.chip.ChipExperience;
import com.chen1335.geneChip.chip.chips.survival.NutrientExtraction;
import com.chen1335.geneChip.common.CardHudSyncService;
import com.chen1335.geneChip.compat.hunger.HrrStomachCompat;
import com.chen1335.geneChip.compat.worldfactor.WorldFactorSynergy;
import com.chen1335.geneChip.network.CardFeedbackPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 进食相关逻辑：挂在 {@link Player#eat(Level, ItemStack, FoodProperties)} 上，
 * 直接拿 {@code this} 当玩家，不依赖 FoodData 的 tick 缓存玩家引用。
 * <p>
 * 大胃王的经验按「消化恢复的饥饿值」在 {@code BigEater.tick} 里结算；
 * 这里的经验属于养分萃取（按这次进食实际进入胃里的点数）。
 */
@Mixin(Player.class)
public abstract class PlayerEatMixin {
    /** 进食前的原版饥饿值。 */
    @Unique
    private int gc$foodLevelBefore;
    /** 进食前的 HRR 胃内容总量（-1 表示没有 HRR）。 */
    @Unique
    private int gc$stomachBefore;

    @Inject(method = "eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/food/FoodProperties;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("HEAD"))
    private void geneChip$beforeEat(Level level, ItemStack food, FoodProperties properties,
                                    CallbackInfoReturnable<ItemStack> cir) {
        Player player = (Player) (Object) this;
        gc$foodLevelBefore = player.getFoodData().getFoodLevel();
        gc$stomachBefore = HrrStomachCompat.totalFood(player);
    }

    @Inject(method = "eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/food/FoodProperties;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"))
    private void geneChip$afterEat(Level level, ItemStack food, FoodProperties properties,
                                   CallbackInfoReturnable<ItemStack> cir) {
        Player player = (Player) (Object) this;
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        // 养分萃取经验：按这次进食实际进入胃里的点数结算。
        // 装了 HRR 时食物进的是胃内容（PlayerStomach），原版饥饿条不变；胃满溢出时才走原版饥饿条。
        int hungerDelta = Math.max(0, player.getFoodData().getFoodLevel() - gc$foodLevelBefore);
        int stomachAfter = HrrStomachCompat.totalFood(player);
        int stomachDelta = gc$stomachBefore >= 0 && stomachAfter >= 0
                ? Math.max(0, stomachAfter - gc$stomachBefore)
                : 0;
        int stored = hungerDelta + stomachDelta;
        if (stored > 0) {
            ChipExperience.grant(serverPlayer, ChipTypes.NUTRIENT_EXTRACTION.get(), "eat_exp", stored);
        }

        GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.NUTRIENT_EXTRACTION).ifPresent(chipInstance -> {
            NutrientExtraction chip = chipInstance.getChip();
            player.getFoodData().eat((int) chip.foodAdd.getValue(chipInstance.getLvl()), 0F);
            float healAmount = chip.heal.getValue(chipInstance.getLvl());
            // 饥荒前兆联动：额外恢复生命值+1
            if (WorldFactorSynergy.isSignsOfFamine()) {
                healAmount += 1;
            }
            player.heal(healAmount);
            // 养分萃取经验：额外恢复实际生效 +2
            ChipExperience.grant(serverPlayer, ChipTypes.NUTRIENT_EXTRACTION.get(), "extra_exp");
            // 进食收益提升时反馈：玩家附近白色烟花粒子 + HUD icon
            serverPlayer.serverLevel().sendParticles(ParticleTypes.FIREWORK,
                    player.getX(), player.getY() + 1.0, player.getZ(), 8, 0.4, 0.5, 0.4, 0.04);
            CardHudSyncService.feedback(serverPlayer, CardFeedbackPacket.FeedbackType.NUTRIENT_EXTRACTED, 0);
        });
    }
}
