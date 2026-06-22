package com.chen1335.geneChip.chip.chips.survival;

import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;
import com.chen1335.geneChip.compat.worldfactor.WorldFactorSynergy;
import net.mcbbs.uid1525632.hungerreworkedreforged.common.attachment.PlayerStomach;
import net.mcbbs.uid1525632.hungerreworkedreforged.common.init.HRRAttachmentTypes;
import net.mcbbs.uid1525632.hungerreworkedreforged.common.init.HRRAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

public class BigEater extends Chip {
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
    public void onEquipped(Player player, ChipInstance<?> instance) {
        // 增加胃条上限
        AttributeInstance stomachAttr = player.getAttribute(HRRAttributes.EXTRA_STOMACH);
        if (stomachAttr != null) {
            float bonus = stomachBonus.getValue(instance.getLvl());
            stomachAttr.addTransientModifier(new AttributeModifier(
                    STOMACH_ID, bonus, AttributeModifier.Operation.ADD_VALUE
            ));
        }

        AttributeInstance digestionRate = player.getAttribute(HRRAttributes.DIGESTION_RATE);
        if (digestionRate != null) {
            float bonus = digestionBoost.getValue(instance.getLvl());
            // 饥荒前兆联动：消化速度+30%
            if (WorldFactorSynergy.isSignsOfFamine()) {
                bonus += 0.3F;
            }
            digestionRate.addTransientModifier(new AttributeModifier(
                    DIGESTION_RATE_ID, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
    }

    @Override
    public void onDayChange(Player player, ChipInstance<?> instance) {
        // 世界因子变化时重新应用消化速度modifier
        AttributeInstance digestionRate = player.getAttribute(HRRAttributes.DIGESTION_RATE);
        if (digestionRate != null) {
            digestionRate.removeModifier(DIGESTION_RATE_ID);
            float bonus = digestionBoost.getValue(instance.getLvl());
            if (WorldFactorSynergy.isSignsOfFamine()) {
                bonus += 0.3F;
            }
            digestionRate.addTransientModifier(new AttributeModifier(
                    DIGESTION_RATE_ID, bonus, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
        }
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
    }
}
