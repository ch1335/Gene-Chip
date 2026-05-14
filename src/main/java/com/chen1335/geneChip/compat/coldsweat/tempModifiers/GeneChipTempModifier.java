package com.chen1335.geneChip.compat.coldsweat.tempModifiers;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.chips.survival.PermafrostWalkers;
import com.momosoftworks.coldsweat.api.temperature.modifier.TempModifier;
import com.momosoftworks.coldsweat.api.util.Temperature;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;
import java.util.function.Function;

public class GeneChipTempModifier extends TempModifier {
    @Override
    protected Function<Double, Double> calculate(LivingEntity entity, Temperature.Trait trait) {
        if (entity instanceof Player player) {
            return input -> {
                if (input > 0) {
                    return input;
                }

                Optional<ChipInstance<PermafrostWalkers>> playerEquippedChip = GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.PERMAFROST_WALKERS);
                if (playerEquippedChip.isPresent()) {
                    ChipInstance<PermafrostWalkers> chipInstance = playerEquippedChip.get();
                    float coldResistance = chipInstance.getChip().coldResistance.getValue(chipInstance.getLvl());
                    return input * (-coldResistance / 20 + 1);
                }
                return input;

            };

        }
        return Double::doubleValue;
    }
}
