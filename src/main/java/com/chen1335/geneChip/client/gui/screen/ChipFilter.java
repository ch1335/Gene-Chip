package com.chen1335.geneChip.client.gui.screen;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.client.GeneChipClient;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.function.BiPredicate;

public class ChipFilter {
    private Set<FilterRule> filterRules = Set.of(FilterRule.UNLOCKED);

    public List<Chip> getAvailableChips(Player player, EnumMap<ChipType, List<Chip>> map) {
        List<Chip> chips = new ArrayList<>();
        for (List<Chip> value : map.values()) {
            for (Chip chip : value) {
                for (FilterRule filterRule : filterRules) {
                    if (!filterRule.predicate.test(player, chip)) {
                        break;
                    }
                    chips.add(chip);
                }
            }
        }
        return chips;
    }


    public enum FilterRule {
        ALL((player, chip) -> true),
        UNLOCKED((player, chip) -> {
            return GeneChipClient.getPlayerChipData().getChipInfos().getChips().getOrDefault(chip.getType(), Map.of()).containsKey(chip);
        });

        private final BiPredicate<Player, Chip> predicate;

        FilterRule(BiPredicate<Player, Chip> predicate) {
            this.predicate = predicate;
        }

        public BiPredicate<Player, Chip> getPredicate() {
            return predicate;
        }
    }
}
