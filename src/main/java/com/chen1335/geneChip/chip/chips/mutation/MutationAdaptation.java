package com.chen1335.geneChip.chip.chips.mutation;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;
import com.chen1335.geneChip.compat.worldfactor.WorldFactorSynergy;
import com.immunity.data.InfectionZoneManager;
import com.immunity.util.ImmunityServerUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class MutationAdaptation extends Chip {
    public final JsValueCalculator drainReduction = new JsValueCalculator("0.3", true);

    public MutationAdaptation() {
        super(makeTexture("mutation_adaptation"));
        registerConfigValue("drain_reduction", drainReduction);
    }

    @Override
    public ChipType getType() {
        return ChipType.MUTATION;
    }

    @Override
    public void tick(Player player, ChipInstance<?> instance) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        boolean inZone = isInInfectionZone(serverPlayer);
        float reduction = drainReduction.getValue(instance.getLvl());

        if (inZone) {
            float drainMult = 1 - reduction;

            // 感染溢出/免疫激增联动：减缓提升至50%
            if (WorldFactorSynergy.isInfectionOverflow() || WorldFactorSynergy.isImmunitySurge()) {
                drainMult = 0.5F;
            }

            ImmunityServerUtil.setDrainMultiplier(serverPlayer, drainMult);

            // 免疫激增联动：免疫自然恢复速度+50%
            if (WorldFactorSynergy.isImmunitySurge()) {
                ImmunityServerUtil.setRecoveryMultiplier(serverPlayer, 1.5F);
            }
        } else {
            ImmunityServerUtil.setDrainMultiplier(serverPlayer, 1.0F);
            ImmunityServerUtil.setRecoveryMultiplier(serverPlayer, 1.0F);
        }
    }

    @Override
    public void onUnEquipped(Player player, ChipInstance<?> instance) {
        if (player instanceof ServerPlayer serverPlayer) {
            ImmunityServerUtil.setDrainMultiplier(serverPlayer, 1.0F);
            ImmunityServerUtil.setRecoveryMultiplier(serverPlayer, 1.0F);
        }
    }

    private boolean isInInfectionZone(ServerPlayer player) {
        InfectionZoneManager zoneManager = InfectionZoneManager.get(player.getServer());
        if (zoneManager == null) return false;
        return zoneManager.getDrain(player.serverLevel(), player.blockPosition()) > 0;
    }
}
