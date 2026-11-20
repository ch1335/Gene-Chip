package com.chen1335.geneChip.chip.chips.survival;
import net.minecraft.network.chat.Component;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipExperience;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;
import com.chen1335.geneChip.compat.worldfactor.WorldFactorSynergy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;

public class PermafrostWalkers extends Chip {
    public final JsValueCalculator passiveExp = expValue("passive_exp", "12");
    public final JsValueCalculator snowWalkExp = expValue("snow_walk_exp", "3");

    public final JsValueCalculator coldResistance = new JsValueCalculator("25");

    public PermafrostWalkers(ResourceLocation texture) {
        super(makeTexture("permafrost_walkers"));
        registerConfigValue("cold_resistance", coldResistance);
    }

    @Override
    public ChipType getType() {
        return ChipType.SURVIVAL;
    }

    @Override
    public void tick(Player player, ChipInstance<?> instance) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        // 寒潮联动：额外获得抗性I
        if (WorldFactorSynergy.isColdWave()) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 39, 0, false, false));
        }

        // 冻土行者经验：低温环境每分钟 +12
        boolean cold = player.level().getBiome(player.blockPosition()).value().getBaseTemperature() <= 0.15F
                || player.getTicksFrozen() > 0;
        if (cold) {
            ChipExperience.grantPassive(serverPlayer, this, "passive_exp");
        }
        // 冻土行者经验：在细雪上行走每 10 秒 +3
        if (player.getBlockStateOn().is(Blocks.POWDER_SNOW)) {
            ChipExperience.grant(serverPlayer, this, "snow_walk_exp", "snow-walk", 200);
        }
    }

    @Override
    public Component detailDesc(int lvl) {
        return detailedDesc();
    }
}
