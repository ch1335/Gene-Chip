package com.chen1335.geneChip.chip.chips.survival;
import net.minecraft.network.chat.Component;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.attachmentData.PlayerRunTimeData;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipExperience;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;
import com.chen1335.geneChip.compat.hunger.HrrStomachCompat;
import com.chen1335.geneChip.compat.worldfactor.WorldFactorSynergy;
import com.wu_meng.hungerreworkedreforged.common.init.HRRAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

public class BigEater extends Chip {
    public final JsValueCalculator eatExp = expValue("eat_exp", "3");

    private static final ResourceLocation STOMACH_ID = GeneChip.id("big_eater_stomach");
    private static final ResourceLocation DIGESTION_RATE_ID = GeneChip.id("big_eater_stomach");
    public final JsValueCalculator stomachBonus = new JsValueCalculator("5");
    public final JsValueCalculator digestionBoost = new JsValueCalculator("0.1", true);

    public BigEater() {
        super(makeTexture("big_eater"));
        registerConfigValue("stomach_bonus", stomachBonus);
        registerConfigValue("digestion_boost", digestionBoost);
    }

    @Override
    public ChipType getType() {
        return ChipType.SURVIVAL;
    }

    @Override
    public void tick(Player player, ChipInstance<?> instance) {
        // 升级或配置变化后需要重新应用属性，否则要卸下再装上才生效
        if (player.level().isClientSide) return;
        refreshModifiers(player, instance.getLvl());

        // 大胃王经验：严格只认 HRR 的消化——
        // 胃内容变少（正在消化）且饥饿条因此上升时，才按上升的点数结算。
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        PlayerRunTimeData runtime = GeneChipAPI.getPlayerRunTimeData(player);
        int currentFood = player.getFoodData().getFoodLevel();
        int currentStomach = HrrStomachCompat.totalFood(player);
        int lastFood = runtime.bigEaterLastFoodLevel;
        int lastStomach = runtime.bigEaterLastStomach;
        runtime.bigEaterLastFoodLevel = currentFood;
        runtime.bigEaterLastStomach = currentStomach;
        if (lastFood < 0 || lastStomach < 0 || currentStomach < 0) {
            return;
        }
        boolean digesting = currentStomach < lastStomach;
        int gained = currentFood - lastFood;
        if (digesting && gained > 0) {
            ChipExperience.grant(serverPlayer, this, "eat_exp", gained);
        }
    }

    @Override
    public void onEquipped(Player player, ChipInstance<?> instance) {
        refreshModifiers(player, instance.getLvl());
        // 初始化基线，避免装备瞬间把当前的饥饿值/胃内容当作「刚消化出来的」结算经验
        if (!player.level().isClientSide) {
            PlayerRunTimeData runtime = GeneChipAPI.getPlayerRunTimeData(player);
            runtime.bigEaterLastFoodLevel = player.getFoodData().getFoodLevel();
            runtime.bigEaterLastStomach = HrrStomachCompat.totalFood(player);
        }
    }

    @Override
    public void onDayChange(Player player, ChipInstance<?> instance) {
        // 世界因子变化时重新应用消化速度
        refreshModifiers(player, instance.getLvl());
    }

    @Override
    public void onUnEquipped(Player player, ChipInstance<?> instance) {
        AttributeInstance stomachAttr = player.getAttribute(HRRAttributes.EXTRA_STOMACH);
        if (stomachAttr != null) {
            stomachAttr.removeModifier(STOMACH_ID);
        }
        AttributeInstance digestionRate = player.getAttribute(HRRAttributes.DIGESTION_RATE);
        if (digestionRate != null) {
            digestionRate.removeModifier(DIGESTION_RATE_ID);
        }
        GeneChipAPI.getPlayerRunTimeData(player).bigEaterLastFoodLevel = -1;
        GeneChipAPI.getPlayerRunTimeData(player).bigEaterLastStomach = -1;
    }

    /** 按当前等级与配置刷新「胃容量」和「消化速度」修饰符（值没变就不动，避免每 tick 抖动）。 */
    private void refreshModifiers(Player player, int lvl) {
        AttributeInstance stomachAttr = player.getAttribute(HRRAttributes.EXTRA_STOMACH);
        if (stomachAttr != null) {
            float bonus = stomachBonus.getValue(lvl);
            AttributeModifier existing = stomachAttr.getModifier(STOMACH_ID);
            if (existing == null || Math.abs(existing.amount() - bonus) > 1.0E-4) {
                stomachAttr.removeModifier(STOMACH_ID);
                stomachAttr.addTransientModifier(new AttributeModifier(
                        STOMACH_ID, bonus, AttributeModifier.Operation.ADD_VALUE
                ));
            }
        }

        AttributeInstance digestionRate = player.getAttribute(HRRAttributes.DIGESTION_RATE);
        if (digestionRate != null) {
            float bonus = digestionBoost.getValue(lvl);
            // 饥荒前兆联动：消化速度+30%
            if (WorldFactorSynergy.isSignsOfFamine()) {
                bonus += 0.3F;
            }
            AttributeModifier existing = digestionRate.getModifier(DIGESTION_RATE_ID);
            if (existing == null || Math.abs(existing.amount() - bonus) > 1.0E-4) {
                digestionRate.removeModifier(DIGESTION_RATE_ID);
                digestionRate.addTransientModifier(new AttributeModifier(
                        DIGESTION_RATE_ID, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                ));
            }
        }
    }

    @Override
    public Component detailDesc(int lvl) {
        return detailedDesc(stomachBonus.getArgValue(lvl), digestionBoost.getArgValue(lvl));
    }
}
